package ru.avtotor.handlers.user.validate;

import org.json.JSONObject;
import ru.avtotor.database.mssql.MSSQL;
import ru.avtotor.exceptions.NotFoundException;
import ru.avtotor.handlers.user.FindUser;
import ru.avtotor.logging.ActivityLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Validate {

    private final JSONObject request;

    public Validate(JSONObject request) {
        this.request = request;
    }

    public static JSONObject validateByID(Connection connection, JSONObject request) throws SQLException {
        JSONObject response = new JSONObject();
        int userID = request.getInt("userID");
        int perms;

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT perms FROM userSecure WHERE userID = ?")) {
            statement.setInt(1, userID);

            try (ResultSet rs = statement.executeQuery()) {
                perms = rs.next() ? rs.getInt(1) : 0;
            }
        }

        ActivityLogger.logHistory(connection, userID, "Валидация пользователя " + userID);
        response.put("code", 200);
        response.put("userID", userID);
        response.put("correct", true);
        response.put("valid", (request.getInt("perms") & perms) == request.getInt("perms"));
        return response;
    }

    public static JSONObject validate(Connection connection, JSONObject request) throws SQLException, NotFoundException, ClassNotFoundException {
        if (!request.isNull("login")) {
            request.put("userID", FindUser.idByLogin(request.getString("login")));
        }
        if (!request.isNull("token")) {
            request.put("userID", FindUser.idByToken(request.getString("token")));
        }
        return validateByID(connection, request);
    }

    public JSONObject validate() throws SQLException, NotFoundException, ClassNotFoundException {
        try (Connection connection = MSSQL.getConnection()) {
            if (!request.isNull("login")) {
                request.put("userID", FindUser.idByLogin(request.getString("login")));
            }
            if (!request.isNull("token")) {
                request.put("userID", FindUser.idByToken(request.getString("token")));
            }
            return validateByID(connection, request);
        }
    }
}
