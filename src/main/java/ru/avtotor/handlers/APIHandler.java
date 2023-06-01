package ru.avtotor.handlers;

import com.sun.net.httpserver.HttpExchange;
import ru.avtotor.request.params.Params;

import java.io.IOException;

public class APIHandler extends DefaultHandler {

    public APIHandler(String endpoint, byte method, Params params) {
        super(endpoint, method, params);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!isEqualsPath(exchange, true)) {
            return;
        }
        exchange.getResponseHeaders().add("Location", "https://documenter.getpostman.com/view/26879421/2s93mBxKVa");
        sendResponse(exchange, (short) 302, new byte[0]);
    }
}
