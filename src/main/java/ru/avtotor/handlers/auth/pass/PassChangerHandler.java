package ru.avtotor.handlers.auth.pass;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;
import ru.avtotor.database.mssql.ColumnLengths;
import ru.avtotor.exceptions.InvalidDataLengthException;
import ru.avtotor.exceptions.NoDataFoundException;
import ru.avtotor.exceptions.NotFoundException;
import ru.avtotor.exceptions.WeakPasswordException;
import ru.avtotor.handlers.DefaultHandler;
import ru.avtotor.request.params.Params;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class PassChangerHandler extends DefaultHandler {

    public PassChangerHandler(String endpoint, byte method, Params params) {
        super(endpoint, method, params);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logRequest(exchange);

        if (!isAvailableMethod(exchange, true) || !isEqualsPath(exchange, true)) {
            return;
        }

        try {
            JSONObject request = getRequestData(exchange);
            validateData(request);
            deepValidateData2(request);
            JSONObject response = new PassChanger(request).change();
            sendResponse(exchange, (short) response.getInt("code"), response.toString(INDENT_FACTOR).getBytes(StandardCharsets.UTF_8));
        } catch (NoDataFoundException | InvalidDataLengthException | WeakPasswordException | NotFoundException ex) {
            sendCustomError(exchange, ex);
        } catch (JSONException ex) {
            sendJSONError(exchange, ex);
        } catch (Exception ex) {
            sendError(exchange, ex);
        }
    }

    public void deepValidateData2(JSONObject request) throws InvalidDataLengthException, JSONException, WeakPasswordException {
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

            if ("oldPassword".equals(key) || "newPassword".equals(key)) {
                String pat = "(?=.*[a-z])(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*()_+=\\-\"№;:?'|\\\\/<>,.`~]).{8,60}";
                if (!value.matches(pat)) {
                    throw new WeakPasswordException("Пароль должен содержать как минимум " +
                            "1 заглавную и строчную букву, цифру и спец-символ, а также должен состоять минимум из " +
                            "8 символов и максимум из 60 символов");
                }
                request.put(key, value);
            }
        }
        if (exceptionString.length() > 2) {
            throw new InvalidDataLengthException("Некорректная длина данных: " +
                    exceptionString.substring(0, exceptionString.length() - 2));
        }
    }
}
