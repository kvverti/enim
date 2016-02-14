package kvverti.enim;

import net.minecraftforge.fml.common.FMLLog;

import org.apache.logging.log4j.Level;

public final class Logger {

	private Logger() { }

	public static void log(Level level, String message, Object... args) {

		FMLLog.log(Enim.NAME, level, message, args);
	}

	public static void info(String message, Object... args) {

		log(Level.INFO, message, args);
	}

	public static void info(Object obj) {

		log(Level.INFO, String.valueOf(obj));
	}

	public static void warn(String message, Object... args) {

		log(Level.WARN, message, args);
	}

	public static void warn(Object obj) {

		log(Level.WARN, String.valueOf(obj));
	}

	public static void error(String message, Object... args) {

		log(Level.ERROR, message, args);
	}

	public static void error(Throwable error, String message, Object... args) {

		FMLLog.log(Enim.NAME, Level.ERROR, error, message, args);
	}

	public static void error(Throwable error) {

		FMLLog.log(Enim.NAME, Level.ERROR, error, "");
	}

	public static void error(Object obj) {

		log(Level.ERROR, String.valueOf(obj));
	}

	public static void debug(String message, Object... args) {

		log(Level.DEBUG, message, args);
	}

	public static void debug(Object obj) {

		log(Level.DEBUG, String.valueOf(obj));
	}

	public static void trace(String message, Object... args) {

		log(Level.TRACE, message, args);
	}

	public static void trace(Object obj) {

		log(Level.TRACE, String.valueOf(obj));
	}
}