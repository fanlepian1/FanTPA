package cn.fancraft.fantpa.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志工具类
 */
public class LoggerUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger("FanTPA");
    private static final String PREFIX = "[FanTPA] ";

    /**
     * 输出info级别日志
     */
    public static void info(String message) {
        LOGGER.info(PREFIX + message);
    }

    /**
     * 输出error级别日志
     */
    public static void error(String message) {
        LOGGER.error(PREFIX + message);
    }

    /**
     * 输出error级别日志（带异常）
     */
    public static void error(String message, Throwable throwable) {
        LOGGER.error(PREFIX + message, throwable);
    }

    /**
     * 输出warn级别日志
     */
    public static void warn(String message) {
        LOGGER.warn(PREFIX + message);
    }

    /**
     * 输出warn级别日志（带异常）
     */
    public static void warn(String message, Throwable throwable) {
        LOGGER.warn(PREFIX + message, throwable);
    }

    /**
     * 输出debug级别日志
     */
    public static void debug(String message) {
        LOGGER.debug(PREFIX + message);
    }
}

