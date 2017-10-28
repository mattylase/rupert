package core;

import core.Bot;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.impl.obj.VoiceState;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.IVoiceState;
import sx.blah.discord.util.cache.LongMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mlase on 9/18/2016.
 */
public class Listener {

    private static final String RUPERT = "Rupert";

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getMessage().getAuthor().getName().equals(RUPERT)) {
            String message = event.getMessage().getContent();
            if (message.equals("/rup")
                    || event.getMessage().getContent().equals("/rupert")) {
                List<IVoiceChannel> channels = new ArrayList<>();

                event.getMessage().getAuthor()
                        .getVoiceStates().forEach((a, b) -> {
                    if (b.getChannel() != null) {
                        channels.add(b.getChannel());
                    }
                });

                if (channels.size() == 0) {
                    Bot.say("You're not in voice, dummy", event.getMessage().getChannel());
                } else {
                    IVoiceChannel voiceChannel = channels.get(0);
                    Bot.pingVoiceMembers(voiceChannel, event.getMessage().getChannel());
                }
            } else if (message.equals("/sub")) {
                Bot.modifyUserAttribute(event.getMessage().getAuthor().getStringID(), Bot.Keys.SUBSCRIBED_TO_VOICE_EVENTS, true);
            } else if (message.equals("/unsub")) {
                Bot.modifyUserAttribute(event.getMessage().getAuthor().getStringID(), Bot.Keys.SUBSCRIBED_TO_VOICE_EVENTS, false);
            } else if (message.equals("help me rupert")) {
                Bot.say("Hey there! I'm Rupert the Bear. I am really good at a few things, mostly making sure that you're READY to GAME!! "
                        + "You can reach me with the following commands:\n\n"
                        + "\t **/rupert** or **/rup** - mention everyone in your voice channel, encouraging them to **r**eady **up**! If nobody is around, I will put out a call to the server, asking for friends!\n\n"
                        + "\t **/sub** - subscribe to updates on voice channel activity (I'll DM you when someone enters or moves channels!)\n\n"
                        + "\t **/unsub** - unsubscribe from updates on voice channel activity (I'll be lonely without you to talk to :cold_sweat:)", event.getMessage().getChannel());
            }
        }
    }

    @EventSubscriber
    public void onUserJoinedVoice(UserVoiceChannelJoinEvent event) {
        Bot.pmSubs(event.getUser().getName(), event.getVoiceChannel().getName());
    }

    @EventSubscriber
    public void onUserMovedVoice(UserVoiceChannelMoveEvent event) {
        Bot.pmSubs(event.getUser().getName(), event.getNewChannel().getName());
    }
}
