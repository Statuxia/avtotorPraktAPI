package ru.avtotor.logging;

import com.sun.net.httpserver.HttpExchange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class LoggerSystem {

    public static <T> void logRequest(HttpExchange exchange, Class<T> clazz) {
        Logger logger = LogManager.getLogger(clazz);
        JSONObject logData = new JSONObject();

        logData.put("user", exchange.getRemoteAddress().getHostName());
        logData.put("endpoint", exchange.getRequestURI().getPath());
        logData.put("method", exchange.getRequestMethod());

        logger.info("\n{}", logData.toString(2));
    }

    public static <T> void logError(Exception ex, Class<T> clazz) {
        Logger logger = LogManager.getLogger(clazz);
        logger.error(ex, ex);
    }

    public static <T> void logInfo(Object message, Class<T> clazz) {
        Logger logger = LogManager.getLogger(clazz);
        logger.info(message);
    }

    public static <T> void logDebug(Object message, Class<T> clazz) {
        Logger logger = LogManager.getLogger(clazz);
        logger.debug(message);
    }
}
