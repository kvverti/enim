package kvverti.enim;

/** Static logging utility */
public final class Logger {

    private static org.apache.logging.log4j.Logger enimLog;

    private Logger() { }

    static void setLog(org.apache.logging.log4j.Logger log) {

        if(enimLog == null)
            enimLog = log;
        else
            throw new IllegalStateException("Mod log already set");
    }

    public static void info(String message, Object... args) {

        enimLog.info(String.format(message, args));
    }

    public static void info(Object obj) {

        enimLog.info(String.valueOf(obj));
    }

    public static void warn(String message, Object... args) {

        enimLog.warn(String.format(message, args));
    }

    public static void warn(Object obj) {

        enimLog.warn(String.valueOf(obj));
    }

    public static void error(String message, Object... args) {

        enimLog.error(String.format(message, args));
    }

    public static void error(Throwable error, String message, Object... args) {

        enimLog.error(String.format(message, args), error);
    }

    public static void error(Throwable error) {

        enimLog.error("", error);
    }

    public static void error(Object obj) {

        enimLog.error(String.valueOf(obj));
    }

    public static void debug(String message, Object... args) {

        enimLog.debug(String.format(message, args));
    }

    public static void debug(Object obj) {

        enimLog.debug(String.valueOf(obj));
    }

    public static void trace(String message, Object... args) {

        enimLog.trace(String.format(message, args));
    }

    public static void trace(Object obj) {

        enimLog.trace(String.valueOf(obj));
    }
}
