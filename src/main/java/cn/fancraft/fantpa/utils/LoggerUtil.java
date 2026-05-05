package cn.fancraft.fantpa.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger("FanTPA");
    private static final String PREFIX = "[FanTPA] ";

    public static void info(String message) { LOGGER.info(PREFIX + message); }

    public static void error(String message) { LOGGER.error(PREFIX + message); }

    public static void error(String message, Throwable throwable) { LOGGER.error(PREFIX + message, throwable); }

    public static void warn(String message) { LOGGER.warn(PREFIX + message); }
}
