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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mattylase on 9/18/2016.
 */
public class Bot {

    private static final String TOKEN_FILE_PATH = "token.rupert";
    private static final String USER_FILE_PATH = "rupert_users";

    private static final String PREAMBLE = "If you need a friend, than you can depend on ";
    private static final String FINISH = "!! Ready to pown yall?";
    private static final String NO_FRIENDS = "@everyone could use a friend, including %s. Want to play a game?";

    static class Keys {
        static final String SUBSCRIBED_TO_VOICE_EVENTS = "SUBSCRIBED_TO_VOICE_EVENTS";
        static final String TEST = "BUTTS";
    }

    private static IDiscordClient client;
    private static volatile Map<String, File> userFileMap;
    private static Set<String> subscribedToVoiceEventSet;

    public static void main(String[] args) {
        try {
            BufferedReader tokenReader = new BufferedReader(new FileReader(new File(TOKEN_FILE_PATH)));
            String token = tokenReader.readLine();
            tokenReader.close();


            // Cache existing user files on startup
            File userDirectory = new File(USER_FILE_PATH);
            userDirectory.mkdir();
            userFileMap = new HashMap<>();
            if (userDirectory.exists()) {
                File[] userFiles = userDirectory.listFiles();
                if (userFiles != null) {
                    Arrays.stream(userFiles).forEach(f -> userFileMap.put(f.getName(), f));
                }
            }

            subscribedToVoiceEventSet = preCacheData(Keys.SUBSCRIBED_TO_VOICE_EVENTS);

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

    static void say(String words, IChannel channel) {
        try {
            new MessageBuilder(client).withChannel(channel).withContent(words).send();
        } catch (DiscordException | RateLimitException | MissingPermissionsException e) {
            e.printStackTrace();
        }
    }

    static void pingVoiceMembers(IVoiceChannel voiceChannel, IChannel textChannel) {
        List<IUser> users = voiceChannel.getConnectedUsers();
        String message;
        if (users.size() == 1) {
            message = String.format(NO_FRIENDS, users.get(0));
        } else {
            message = PREAMBLE;
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
            message += FINISH;
        }
        say(message, textChannel);
    }

    private static Set<String> preCacheData(String attribute) throws IOException {
        Set<String> resultSet = new HashSet<>();
        if (attribute.equals(Keys.SUBSCRIBED_TO_VOICE_EVENTS)) {
            for (String userName : userFileMap.keySet()) {
                BufferedReader reader = new BufferedReader(new FileReader(userFileMap.get(userName)));
                if (reader.readLine().contains(Keys.SUBSCRIBED_TO_VOICE_EVENTS)) {
                    resultSet.add(userName);
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

    private static File createUserFile(String fileName) {
        try {
            File file = new File(USER_FILE_PATH + "/" + fileName);
            if (file.createNewFile()) {
                return file;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static synchronized void modifyUserAttribute(String userId, String attribute, boolean addAttribute) {
        if (!userFileMap.containsKey(userId)) {
            File file = createUserFile(userId);
            if (file != null) {
                userFileMap.put(userId, file);
            }
        }

        modifyUserFileAttribute(userId, attribute, addAttribute);

        switch (attribute) {
            case Keys.SUBSCRIBED_TO_VOICE_EVENTS:
                if (addAttribute) subscribedToVoiceEventSet.add(userId);
                else subscribedToVoiceEventSet.remove(userId);
        }
    }

    static void pmSubs(String joinedUserName, String channelName) {
        try {
            for (String userId : subscribedToVoiceEventSet) {
                IUser user = client.getUserByID(userId);
                if (!user.getName().equals(joinedUserName)) {
                    user.getOrCreatePMChannel().sendMessage("Hey boy-o, just a heads up, " + joinedUserName
                            + " just hopped in " + channelName);
                }
            }
        } catch (MissingPermissionsException | RateLimitException | DiscordException e) {
            e.printStackTrace();
        }
    }

    private static synchronized void modifyUserFileAttribute(String userName, String attribute, boolean addAttribute) {
        File file = userFileMap.get(userName);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            StringBuilder output = new StringBuilder();
            reader.close();
            if (line != null) {
                if (line.contains(",")) {
                    List<String> list = new ArrayList<>(Arrays.asList(line.split(",")));
                    if (addAttribute && !list.contains(attribute)) {
                        list.add(attribute);
                        list.forEach(s -> output.append(s).append(','));
                    } else {
                        list.remove(attribute);
                        list.forEach(s -> output.append(s).append(','));
                    }
                } else if (!line.equals(attribute) && addAttribute){
                    output.append(line).append(',').append(attribute);
                } else if (addAttribute) {
                    output.append(line);
                }
            } else if (addAttribute) {
                output.append(attribute);
            }

            String outString = output.toString();
            BufferedWriter writer = new BufferedWriter(new FileWriter(userFileMap.get(userName)));
            if (outString.length() > 1 && outString.charAt(outString.length() - 1) == ',') {
                outString = outString.substring(0, outString.length() - 1);
            }
            writer.write(outString);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
