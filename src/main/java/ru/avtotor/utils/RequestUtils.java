package ru.avtotor.utils;

import com.google.gson.Gson;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RequestUtils {

    public static JSONObject query(String query) {
        if (query == null) {
            return new JSONObject();
        }
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }

        Gson gson = new Gson();
        return new JSONObject(gson.toJson(result));
    }
}
