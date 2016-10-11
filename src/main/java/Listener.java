import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IVoiceChannel;

import java.util.List;

/**
 * Created by mlase on 9/18/2016.
 */
public class Listener {
    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContent().equals("/rup")
                || event.getMessage().getContent().equals("/rupert")) {
            List<IVoiceChannel> channels = event.getMessage().getAuthor().getConnectedVoiceChannels();
            if (channels.size() == 0) {
                Bot.say("You're not in voice, dummy", event.getMessage().getChannel());
            } else {
                IVoiceChannel voiceChannel = channels.get(0);
                Bot.pingVoiceMembers(voiceChannel, event.getMessage().getChannel());
            }
        }
    }
}
