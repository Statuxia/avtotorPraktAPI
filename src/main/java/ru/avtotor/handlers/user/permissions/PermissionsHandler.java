package ru.avtotor.handlers.user.permissions;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;
import ru.avtotor.handlers.DefaultHandler;
import ru.avtotor.handlers.token.TokenPermissions;
import ru.avtotor.request.params.Params;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PermissionsHandler extends DefaultHandler {

    public PermissionsHandler(String endpoint, byte method, Params params) {
        super(endpoint, method, params);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logRequest(exchange);

        if (!isAvailableMethod(exchange, true) || !isEqualsPath(exchange, true)) {
            return;
        }

        short code = 200;
        JSONObject response = new JSONObject();

        for (TokenPermissions value : TokenPermissions.values()) {
            JSONObject token = new JSONObject();
            token.put("value", value.getValue());
            token.put("descriptions", value.getDescriptions());
            response.put(value.toString(), token);
        }

        response.put("code", code);
        sendResponse(exchange, code, response.toString(INDENT_FACTOR).getBytes(StandardCharsets.UTF_8));
    }
}
