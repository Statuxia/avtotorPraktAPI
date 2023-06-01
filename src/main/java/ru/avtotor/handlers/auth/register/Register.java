package ru.avtotor.handlers.auth.register;

import org.json.JSONObject;
import ru.avtotor.database.mssql.MSSQL;
import ru.avtotor.handlers.token.TokenGenerator;
import ru.avtotor.handlers.token.TokenPermissions;
import ru.avtotor.logging.ActivityLogger;

import java.sql.*;

public class Register {

    private final JSONObject request;

    public Register(JSONObject request) {
        this.request = request;
    }

    public JSONObject register() throws SQLException, ClassNotFoundException {
        try (Connection connection = MSSQL.getConnection()) {
            JSONObject response = new JSONObject();
            if (isRegistered(connection)) {
                response.put("code", 400);
                response.put("error-code", "Пользователь '" + request.getString("login") + "' уже зарегистрирован");
                return response;
            }
            if (isPhoneUsed(connection)) {
                response.put("code", 400);
                response.put("error-code", "Переданный номер телефона уже используется");
                return response;
            }

            String token = TokenGenerator.generate();
            int userID;
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO userSecure (login, password, token, perms) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, request.getString("login"));
                statement.setString(2, request.getString("password"));
                statement.setString(3, token);
                statement.setInt(4, TokenPermissions.READ.getValue());
                statement.executeUpdate();

                try (ResultSet rs = statement.getGeneratedKeys()) {
                    if (!rs.next()) {
                        response.put("code", 409);
                        response.put("error-code", "Не удалось зарегистрировать пользователя");
                        return response;
                    }
                    userID = rs.getInt(1);
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO userData (userID, surname, name, middleName, sex) VALUES (?, ?, ?, ?, ?);" +
                            "INSERT INTO userContacts (userID, address, phone) VALUES (?, ?, ?);")) {
                statement.setInt(1, userID);
                statement.setInt(6, userID);
                statement.setString(2, request.getString("surname"));
                statement.setString(3, request.getString("name"));
                statement.setString(4, request.getString("middleName"));
                statement.setString(8, request.getString("phone"));

                if (request.isNull("sex")) {
                    statement.setNull(5, Types.NCHAR);
                } else {
                    statement.setString(5, request.getString("sex"));
                }
                if (request.isNull("address")) {
                    statement.setNull(7, Types.NCHAR);
                } else {
                    statement.setString(7, request.getString("address"));
                }
                statement.executeUpdate();
            }
            response.put("code", 200);
            response.put("token", token);
            ActivityLogger.logHistory(connection, userID, "Регистрация пользователя " + userID);
            return response;
        }
    }

    private boolean isRegistered(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM userSecure WHERE login = ?")) {
            statement.setString(1, request.getString("login"));
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isPhoneUsed(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM userContacts WHERE phone = ?")) {
            statement.setString(1, request.getString("phone"));
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }
}
