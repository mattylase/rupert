import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mattylase on 9/18/2016.
 */
public class Bot {

    private static String tokenFile = "token.rupert";
    private static String preamble = "If you need a friend, than you can depend on ";
    private static String finish = "!! Ready to pown yall?";
    private static String noFriends = " has no friends. Anyone @here care to join them?";
    private static IDiscordClient client;

    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(tokenFile)));
            String token = reader.readLine();
            reader.close();

            client = getClient(token, true);
            client.getDispatcher().registerListener(new Listener());
        } catch (IOException | DiscordException e) {
            Logger.getGlobal().log(Level.ALL, "There was a problem initializing the Discord client!");
        } finally {
            if (client == null) {
                System.exit(10);
            }
        }
    }

    public static void say(String words, IChannel channel) {
        try {
            new MessageBuilder(client).withChannel(channel).withContent(words).send();
        } catch (RateLimitException e) {
            e.printStackTrace();
        } catch (DiscordException e) {
            e.printStackTrace();
        } catch (MissingPermissionsException e) {
            e.printStackTrace();
        }
    }

    public static void pingVoiceMembers(IVoiceChannel voiceChannel, IChannel textChannel) {
        List<IUser> users = voiceChannel.getConnectedUsers();
        String message;
        if (users.size() == 1) {
            message = users.get(0).mention(true) + noFriends;
        } else {
            message = preamble;
            for (int i = 0; i < users.size(); i++) {
                String mention;
                if (i != users.size() - 1) {
                    mention = users.get(i).mention(true);
                    mention += ", ";
                } else {
                    mention = "and ";
                    mention += users.get(i).mention(true);
                }
                message += mention;
            }
            message += finish;
        }
        say(message, textChannel);
    }

    public static IDiscordClient getClient(String token, boolean login) throws DiscordException { // Returns an instance of the Discord client
        ClientBuilder clientBuilder = new ClientBuilder(); // Creates the ClientBuilder instance
        clientBuilder.withToken(token); // Adds the login info to the builder
        if (login) {
            return clientBuilder.login(); // Creates the client instance and logs the client in
        } else {
            return clientBuilder.build(); // Creates the client instance but it doesn't log the client in yet, you would have to call client.login() yourself
        }
    }

}
