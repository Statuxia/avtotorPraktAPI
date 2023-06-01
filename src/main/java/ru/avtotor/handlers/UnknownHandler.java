package ru.avtotor.handlers;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;
import ru.avtotor.logging.LoggerSystem;
import ru.avtotor.request.params.Params;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class UnknownHandler extends DefaultHandler {


    public UnknownHandler(String endpoint, byte method, Params params) {
        super(endpoint, method, params);
    }

    public static void sendUnknownResponse(HttpExchange exchange) throws IOException {
        JSONObject unknownResponse = new JSONObject();

        short code = 404;
        unknownResponse.put("code", code);
        unknownResponse.put("error-message", "Запрашиваемый ресурс не найден.");
        sendResponse(exchange, code, unknownResponse.toString(INDENT_FACTOR).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logRequest(exchange);

        sendUnknownResponse(exchange);
    }
}
