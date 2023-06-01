package ru.avtotor.handlers.auth.login;

import org.json.JSONObject;
import org.mindrot.bcrypt.BCrypt;
import ru.avtotor.database.mssql.MSSQL;
import ru.avtotor.exceptions.NotFoundException;
import ru.avtotor.handlers.block.lockCheck.LockCheck;
import ru.avtotor.logging.ActivityLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login {

    private final JSONObject request;

    public Login(JSONObject request) {
        this.request = request;
    }

    public JSONObject login() throws SQLException, ClassNotFoundException, NotFoundException {
        JSONObject response = new JSONObject();

        try (Connection connection = MSSQL.getConnection()) {
            JSONObject lockCheck = new LockCheck(request).check();
            if (lockCheck.getBoolean("locked")) {
                response.put("code", 403);
                response.put("error-code", "Пользователь заблокирован");
                return response;
            }

            JSONObject userSecure = new JSONObject();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT password, token, userID FROM userSecure WHERE login = ?")) {
                statement.setString(1, request.getString("login"));

                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        response.put("code", 400);
                        response.put("error-code", "Пользователь '" + request.getString("login") + "' не существует");
                        return response;
                    }
                    userSecure.put("password", rs.getString(1));
                    userSecure.put("token", rs.getString(2));
                    userSecure.put("userID", rs.getInt(3));
                }
            }

            if (!BCrypt.checkpw(request.getString("password"), userSecure.getString("password"))) {
                response.put("code", 400);
                response.put("error-code", "Некорректный пароль.");
                return response;
            }

            response.put("code", 200);
            response.put("token", userSecure.getString("token"));
            ActivityLogger.logHistory(connection, userSecure.getInt("userID"), "Логин пользователя " + userSecure.getInt("userID"));
            return response;
        }
    }
}
