package core;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import util.Constants;
import util.FileUtil;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * The main class that keeps Rupert chugging. I should probably make this whole thing more OOP, but meh.
 */
public class Bot {
    private static IDiscordClient client;
    private static volatile Map<String, File> userFileMap;
    private static Set<String> subscribedToVoiceEventSet;
    private static MessageBuilder messageBuilder;

    public static void main(String[] args) {
        try {
            BufferedReader tokenReader = new BufferedReader(
                    new FileReader(new File(Constants.Paths.TOKEN_FILE_PATH)));
            String token = tokenReader.readLine();
            tokenReader.close();


            // Cache existing user files on startup
            File userDirectory = new File(FileUtil.USER_FILE_PATH);
            userDirectory.mkdir();
            userFileMap = new HashMap<>();
            if (userDirectory.exists()) {
                File[] userFiles = userDirectory.listFiles();
                if (userFiles != null) {
                    Arrays.stream(userFiles).forEach(f -> userFileMap.put(f.getName(), f));
                }
            }

            subscribedToVoiceEventSet = preCacheData(Constants.Keys.SUBSCRIBED_TO_VOICE_EVENTS);

            client = getClient(token, true);
            client.getDispatcher().registerListener(new Listener());

            // Try to say hello on the default channel, if it exists
            client.getChannels().forEach(channel -> {
                if (channel.getName().equals(Constants.Channels.GFNERAL)) {
                    say(Constants.Responses.HELP, channel, Optional.empty());
                }
            });
        } catch (IOException | DiscordException e) {
            Logger.getGlobal().log(Level.ALL, "There was a problem initializing the Discord client!");
        } finally {
            if (client == null) {
                System.exit(10);
            }
        }
    }

    static void say(String words, IChannel channel, Optional<String> playing) {
        if (messageBuilder == null) {
            messageBuilder = new MessageBuilder(client);
        }
        try {
            messageBuilder.withChannel(channel).withContent(words).send();
        } catch (DiscordException | RateLimitException | MissingPermissionsException e) {
            e.printStackTrace();
        }
    }

    //TODO: String builder
    static void pingVoiceMembers(IVoiceChannel voiceChannel, IChannel textChannel, Optional<String> playing) {
        List<IUser> users = voiceChannel.getConnectedUsers();
        String message;
        if (users.size() == 1) {
            message = String.format(Constants.Responses.NO_FRIENDS, users.get(0));
            if (playing.isPresent()) {
                message += String.format(Constants.Responses.VOICE_PLAYING, playing.get());
            }
        } else {
            message = Constants.Responses.VOICE_PREAMBLE;
            for (int i = 0; i < users.size(); i++) {
                String mention;
                if (i != users.size() - 1) {
                    mention = users.get(i).mention(true);
                    mention += ", ";
                } else {
                    mention = "and ";
                    mention += users.get(i).mention(true);
                    mention += '.';
                }
                message += mention;
            }

            if (playing.isPresent()) {
                message += String.format(Constants.Responses.VOICE_PLAYING, playing.get());
            } else {
                message += Constants.Responses.VOICE_FINISH;
            }
        }
        say(message, textChannel, Optional.empty());
    }

    private static Set<String> preCacheData(String attribute) throws IOException {
        Set<String> resultSet = new HashSet<>();
        if (attribute.equals(Constants.Keys.SUBSCRIBED_TO_VOICE_EVENTS)) {
            for (String userId : userFileMap.keySet()) {
                if (FileUtil.instance().fileContainsAttribute(userFileMap.get(userId),
                        Constants.Keys.SUBSCRIBED_TO_VOICE_EVENTS)) {
                    resultSet.add(userId);
                }
            }
        }
        return resultSet;
    }

    private static IDiscordClient getClient(String token, boolean login) throws DiscordException { // Returns an instance of the Discord client
        ClientBuilder clientBuilder = new ClientBuilder();
        clientBuilder.withToken(token);
        if (login) {
            return clientBuilder.login();
        } else {
            return clientBuilder.build();
        }
    }

    static synchronized void modifyUserAttribute(String userId, String attribute, boolean addAttribute) {
        if (!userFileMap.containsKey(userId)) {
            File file = FileUtil.instance().createUserFile(userId);
            if (file != null) {
                userFileMap.put(userId, file);
            }
        }

        FileUtil.instance().modifyUserFileAttribute(userFileMap.get(userId), attribute, addAttribute);

        switch (attribute) {
            case Constants.Keys.SUBSCRIBED_TO_VOICE_EVENTS:
                if (addAttribute) subscribedToVoiceEventSet.add(userId);
                else subscribedToVoiceEventSet.remove(userId);
        }
    }

    static void pmSubs(String joinedUserName, String channelName, Optional<String> playing) {
        try {
            for (String userId : subscribedToVoiceEventSet) {
                IUser user = client.getUserByID(Long.valueOf(userId));
                if (!user.getName().equals(joinedUserName)) {
                    String response = String.format(Constants.Responses.PM_HEADS_UP, joinedUserName, channelName);
                    if (playing.isPresent()) {
                        response += String.format(Constants.Responses.VOICE_PLAYING, playing.get());
                    }
                    user.getOrCreatePMChannel().sendMessage(response);
                }
            }
        } catch (MissingPermissionsException | RateLimitException | DiscordException e) {
            e.printStackTrace();
        }
    }
}
