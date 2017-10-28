package core;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.obj.IVoiceChannel;
import util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Listener class reads the chat channels and responds appropriately
 */
/*package*/ class Listener {

    private static final String RUPERT = "Rupert";
    private List<IVoiceChannel> channels = new ArrayList<>();

    @EventSubscriber
    @SuppressWarnings("unused")
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getMessage().getAuthor().getName().equals(RUPERT)) {
            String message = event.getMessage().getContent();
            if (message.equalsIgnoreCase(Constants.Commands.READY_UP_1) ||
                    event.getMessage().getContent().equalsIgnoreCase(Constants.Commands.READY_UP_2)) {

                event.getMessage().getAuthor()
                        .getVoiceStates().forEach((a, b) -> {
                    if (b.getChannel() != null) {
                        channels.add(b.getChannel());
                    }
                });

                if (channels.size() == 0) {
                    Bot.say(Constants.Responses.NOT_IN_VOICE,
                            event.getMessage().getChannel(), event.getAuthor().getPresence().getPlayingText());
                } else {
                    IVoiceChannel voiceChannel = channels.get(0);
                    Bot.pingVoiceMembers(voiceChannel,
                            event.getMessage().getChannel(), event.getAuthor().getPresence().getPlayingText());
                }
            } else if (message.equalsIgnoreCase(Constants.Commands.SUBSCRIBE_1) ||
                    message.equalsIgnoreCase(Constants.Commands.SUBSCRIBE_2)) {
                Bot.modifyUserAttribute(event.getMessage().getAuthor().getStringID(),
                        Constants.Keys.SUBSCRIBED_TO_VOICE_EVENTS, true);
            } else if (message.equalsIgnoreCase(Constants.Commands.UNSUBSCRIBE_1) ||
                    message.equalsIgnoreCase(Constants.Commands.UNSUBSCRIBE_2)) {
                Bot.modifyUserAttribute(event.getMessage().getAuthor().getStringID(),
                        Constants.Keys.SUBSCRIBED_TO_VOICE_EVENTS, false);
            } else if (message.equalsIgnoreCase(Constants.Commands.HELP)) {
                Bot.say(Constants.Responses.HELP, event.getMessage().getChannel(), Optional.empty());
            }
        }
    }

    @EventSubscriber
    @SuppressWarnings("unused")
    public void onUserJoinedVoice(UserVoiceChannelJoinEvent event) {
        Bot.pmSubs(event.getUser().getName(), event.getVoiceChannel().getName(),
                event.getUser().getPresence().getPlayingText());
    }

    @EventSubscriber
    @SuppressWarnings("unused")
    public void onUserMovedVoice(UserVoiceChannelMoveEvent event) {
        Bot.pmSubs(event.getUser().getName(), event.getNewChannel().getName(),
                event.getUser().getPresence().getPlayingText());
    }
}
