package io.v4guard.plugin.core.constants;

public class ListenersConstants {

    //Event-related
    public static final String EVENT_CONNECT = "connect";
    public static final String EVENT_CONNECT_ERROR = "connect_error";
    public static final String EVENT_CHECK = "check";
    public static final String EVENT_AUTH = "auth";
    public static final String EVENT_SETTINGS = "settings";
    public static final String EVENT_SETTING = "setting";
    public static final String EVENT_CONSOLE = "console";
    public static final String EVENT_MESSAGE  = "message";
    public static final String EVENT_KICK  = "kick";
    public static final String EVENT_CLEAN_CACHE  = "cleancache";
    public static final String EVENT_FIND  = "find";
    public static final String EVENT_RECONNECT  = "reconnect";

    //Auth
    public static final String AUTH_CODE  = "code";
    public static final String AUTH_STATUS  = "status";

    //IPSET
    public static final String IPSET_ACTION = "action";
    public static final String IPSET_ACTION_ADD = "add";
    public static final String IPSET_ACTION_FLUSH = "flush";
    public static final String IPSET_IP = "ip";
    public static final String IPSET_TIMEOUT = "timeout";

    //Messager
    public static final String ALL_PLAYERS_PERMISSION = "*";
    public static final String NO_PERMISSION_NEEDED = "no-permission";

}
