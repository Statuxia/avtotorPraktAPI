package ru.avtotor.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import ru.avtotor.database.mssql.ColumnLengths;
import ru.avtotor.exceptions.InvalidDataLengthException;
import ru.avtotor.exceptions.NoDataFoundException;
import ru.avtotor.logging.LoggerSystem;
import ru.avtotor.request.method.HttpMethod;
import ru.avtotor.request.params.Params;
import ru.avtotor.utils.RequestUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public abstract class DefaultHandler implements HttpHandler {

    protected static final byte INDENT_FACTOR = 4;
    protected final String defaultEndpoint;
    protected final byte defaultMethod;
    protected final Params defaultParams;

    public DefaultHandler(String endpoint, byte method, Params params) {
        defaultEndpoint = endpoint;
        defaultMethod = method;
        defaultParams = params;
    }

    protected static void sendResponse(HttpExchange exchange, short code, byte[] responseData) throws IOException {
        exchange.sendResponseHeaders(code, responseData.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(responseData);
        stream.flush();
        stream.close();
    }

    protected JSONObject getRequestData(HttpExchange exchange) throws IOException {
        JSONObject request;
        String s = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (s.isEmpty()) {
            request = RequestUtils.query(exchange.getRequestURI().getQuery());
        } else {
            request = new JSONObject(s);
        }
        return request;
    }


    protected boolean isAvailableMethod(HttpExchange exchange) {
        byte method = HttpMethod.valueOf(exchange.getRequestMethod()).getValue();
        return (method & defaultMethod) == method;
    }

    protected boolean isAvailableMethod(HttpExchange exchange, boolean sendResponse) throws IOException {
        byte method = HttpMethod.valueOf(exchange.getRequestMethod()).getValue();

        if (!((method & defaultMethod) == method)) {
            JSONObject response = new JSONObject();
            short code = 405;

            response.put("code", code);
            response.put("error-message", "%1$s метод не разрешен.".formatted(exchange.getRequestMethod()));
            if (sendResponse) {
                sendResponse(exchange, code, response.toString(INDENT_FACTOR).getBytes(StandardCharsets.UTF_8));
            }
            return false;
        }
        return true;
    }

    protected boolean isEqualsPath(HttpExchange exchange) {
        return exchange.getRequestURI().getPath().equals(defaultEndpoint);
    }

    protected boolean isEqualsPath(HttpExchange exchange, boolean sendResponse) throws IOException {
        if (exchange.getRequestURI().getPath().equals(defaultEndpoint)) {
            return true;
        }
        if (sendResponse) {
            UnknownHandler.sendUnknownResponse(exchange);
        }
        return false;
    }

    protected void logRequest(HttpExchange exchange) {
        LoggerSystem.logRequest(exchange, this.getClass());
    }

    protected void logError(Exception ex) {
        LoggerSystem.logError(ex, this.getClass());
    }

    protected void logDebug(Object message) {
        LoggerSystem.logDebug(message, this.getClass());
    }

    protected void logInfo(Object message) {
        LoggerSystem.logInfo(message, this.getClass());
    }

    protected void validateData(JSONObject request) throws NoDataFoundException {
        StringBuilder exceptionString = new StringBuilder();

        for (String key : defaultParams.required()) {
            if (!request.has(key)) {
                exceptionString.append(key).append(", ");
            }
        }

        if (exceptionString.length() > 2) {
            throw new NoDataFoundException("Отсутствуют данные: " + exceptionString.substring(0, exceptionString.length() - 2));
        }

        boolean expected = false;
        for (String keys : defaultParams.variable()) {
            for (String key : keys.split(", ")) {
                if (!request.has(key)) {
                    expected = false;
                    request.put(key, JSONObject.NULL);
                    break;
                }
                expected = true;
            }
            if (!expected) {
                exceptionString.append(keys).append(" или ");
                continue;
            }
            break;
        }

        if (!expected && exceptionString.length() > 6) {
            throw new NoDataFoundException("Отсутствуют данные: " + exceptionString.substring(0, exceptionString.length() - 5));
        }

        for (String key : defaultParams.optional()) {
            if (!request.has(key)) {
                request.put(key, JSONObject.NULL);
            }
        }
    }

    protected void deepValidateData(JSONObject request) throws InvalidDataLengthException {
        Map<String, Integer[]> columnLengths = ColumnLengths.getUserColumns();
        StringBuilder exceptionString = new StringBuilder();

        for (String key : columnLengths.keySet()) {
            if (!request.has(key) || request.isNull(key)) {
                continue;
            }

            String value = request.getString(key);
            Integer[] keyValues = columnLengths.get(key);

            if (value.length() < keyValues[0] || value.length() > keyValues[1]) {
                exceptionString.append(key).append(" - ").append(Arrays.toString(keyValues)).append(", ");
            }
        }
        if (exceptionString.length() > 2) {
            throw new InvalidDataLengthException("Некорректная длина данных: " +
                    exceptionString.substring(0, exceptionString.length() - 2));
        }
    }

    protected void sendError(HttpExchange exchange, Exception ex) throws IOException {
        short code = 500;
        JSONObject response = new JSONObject();

        response.put("code", code);
        response.put("error-message", "Ошибка сервера. Обратитесь к администратору.");
        logError(ex);
        sendResponse(exchange, code, response.toString(INDENT_FACTOR).getBytes(StandardCharsets.UTF_8));
    }

    protected void sendCustomError(HttpExchange exchange, Exception ex) throws IOException {
        short code = 400;
        JSONObject response = new JSONObject();

        response.put("code", code);
        response.put("error-message", ex.getMessage());
        logError(ex);
        sendResponse(exchange, code, response.toString(INDENT_FACTOR).getBytes(StandardCharsets.UTF_8));
    }

    protected void sendJSONError(HttpExchange exchange, Exception ex) throws IOException {
        short code = 400;
        JSONObject response = new JSONObject();

        String message = ex.getMessage();
        message = message.replace("JSONObject[\"", "Error data type: '");
        message = message.replace("\"]", "'");
        message = message.substring(0, message.indexOf("(") - 1);


        response.put("code", code);
        response.put("error-message", message);
        logError(ex);
        sendResponse(exchange, code, response.toString(INDENT_FACTOR).getBytes(StandardCharsets.UTF_8));
    }
}
