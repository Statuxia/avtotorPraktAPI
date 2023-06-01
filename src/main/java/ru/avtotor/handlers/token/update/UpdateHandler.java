package ru.avtotor.handlers.token.update;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;
import ru.avtotor.exceptions.InvalidDataLengthException;
import ru.avtotor.exceptions.NoDataFoundException;
import ru.avtotor.exceptions.NotFoundException;
import ru.avtotor.handlers.DefaultHandler;
import ru.avtotor.request.params.Params;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class UpdateHandler extends DefaultHandler {

    public UpdateHandler(String endpoint, byte method, Params params) {
        super(endpoint, method, params);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logRequest(exchange);

        if (!isAvailableMethod(exchange, true) || !isEqualsPath(exchange, true)) {
            return;
        }

        JSONObject request = getRequestData(exchange);
        try {
            validateData(request);
            deepValidateData(request);
            JSONObject response = new Update(request).update();
            sendResponse(exchange, (short) response.getInt("code"), response.toString(INDENT_FACTOR).getBytes(StandardCharsets.UTF_8));
        } catch (NoDataFoundException | NotFoundException | InvalidDataLengthException ex) {
            sendCustomError(exchange, ex);
        } catch (JSONException ex) {
            sendJSONError(exchange, ex);
        } catch (Exception ex) {
            sendError(exchange, ex);
        }
    }
}
