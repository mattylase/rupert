package util;

/**
 * Constants for unchanging values
 */
public final class Constants {

    public final class Channels {
        public static final String GFNERAL = "general";
    }

    public final class Commands {
        public static final String READY_UP_1 = "!rup";
        public static final String READY_UP_2 = "!rupert";
        public static final String SUBSCRIBE_1 = "!sub";
        public static final String SUBSCRIBE_2 = "!subscribe";
        public static final String UNSUBSCRIBE_1 = "!unsub";
        public static final String UNSUBSCRIBE_2 = "!unsubscribe";
        public static final String HELP = "help me rupert";
    }

    public final class Responses {
        public static final String NOT_IN_VOICE = "You'll need to join a voice channel if you want me to let people know that you're ready to GAME!";
        public static final String HELP = "Hey there! I'm Rupert the :bear:. I am really good at a few things, mostly making sure that you're READY to GAME!! "
                + "You can reach me with the following commands:\n\n"
                + "**" + Commands.READY_UP_1 + "** or **" + Commands.READY_UP_2 + "** \n\t\t:small_orange_diamond: _mention everyone in your voice channel, encouraging them to **r**eady **up**! If nobody is around, I will put out a call to the server, asking for friends!_\n\n"
                + "**"+ Commands.SUBSCRIBE_1 + "** or **" + Commands.SUBSCRIBE_2 + "** \n\t\t:small_orange_diamond: _subscribe to updates on voice channel activity (I'll DM you when someone enters or moves channels!)_\n\n"
                + "**" + Commands.UNSUBSCRIBE_1 + "** or **" + Commands.UNSUBSCRIBE_2 + "** \n\t\t:small_orange_diamond: _unsubscribe from updates on voice channel activity_ \n\n"
                + "**" + Commands.HELP + "** \n\t\t:small_orange_diamond:_view this help message again! _";
    }
}
