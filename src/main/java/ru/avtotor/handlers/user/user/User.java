package ru.avtotor.handlers.user.user;

import org.json.JSONObject;
import ru.avtotor.database.mssql.MSSQL;
import ru.avtotor.exceptions.NotFoundException;
import ru.avtotor.handlers.block.lockCheck.LockCheck;
import ru.avtotor.handlers.token.TokenPermissions;
import ru.avtotor.handlers.user.FindUser;
import ru.avtotor.handlers.user.validate.Validate;
import ru.avtotor.logging.ActivityLogger;
import ru.avtotor.logging.LoggerSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User {

    private final JSONObject request;

    public User(JSONObject request) {
        this.request = request;
    }

    public JSONObject user() throws SQLException, NotFoundException, ClassNotFoundException {
        try (Connection connection = MSSQL.getConnection()) {
            if (!request.isNull("login")) {
                request.put("userID", FindUser.idByLogin(request.getString("login")));
            }
            if (!request.isNull("token")) {
                request.put("userID", FindUser.idByToken(request.getString("token")));
            }
            return userByID(connection);
        }
    }

    private JSONObject userByID(Connection connection) throws SQLException, NotFoundException, ClassNotFoundException {
        JSONObject response = new JSONObject();

        JSONObject request = new JSONObject();
        request.put("token", this.request.getString("readerToken"));
        request.put("perms", TokenPermissions.READ.getValue());
        JSONObject responseRead = Validate.validate(connection, request);
        int readerID = FindUser.idByToken(this.request.getString("readerToken"));

        if (!responseRead.getBoolean("valid")) {
            response.put("code", 403);
            response.put("error-code", "Недостаточно прав");
            return response;
        }

        request.remove("perms");
        JSONObject lockCheck = new LockCheck(request).check();
        if (lockCheck.getBoolean("locked")) {
            response.put("code", 403);
            response.put("error-code", "Пользователь заблокирован");
            return response;
        }

        response = getUserData(connection);
        if (response.isEmpty()) {
            response.put("code", 400);
            response.put("error-code", "Неизвестный пользователь");
            ActivityLogger.logHistory(connection, readerID, "Просмотр пользователя " + request.getInt("userID"));
            return response;
        }

        response.put("userID", request.getInt("userID"));
        response.put("locks", getLocks(connection));
        response.put("code", 200);
        LoggerSystem.logDebug(response, this.getClass());
        ActivityLogger.logHistory(connection, readerID, "Просмотр пользователя " + request.getInt("userID"));
        return response;
    }

    private JSONObject getUserData(Connection connection) throws SQLException {
        JSONObject user = new JSONObject();
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT userSecure.perms, userData.surname, userData.name, userData.middleName, userData.sex, userContacts.address,
                userContacts.phone from userSecure inner join userContacts on userSecure.userID = userContacts.userID
                inner join userData on userSecure.userID = userData.userID where userSecure.userID = ?""")) {
            statement.setInt(1, request.getInt("userID"));
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return user;
                }

                JSONObject data = new JSONObject();
                user.put("perms", rs.getInt(1));
                data.put("surname", rs.getString(2));
                data.put("name", rs.getString(3));
                data.put("middleName", rs.getString(4));
                data.put("sex", rs.getString(5));
                data.put("address", rs.getString(6));
                data.put("phone", rs.getString(7));
                user.put("data", data);
                return user;
            }
        }
    }

    private JSONObject getLocks(Connection connection) throws SQLException {
        JSONObject locks = new JSONObject();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT lockDate, endLockDate, reason FROM userLocks where userID = ?")) {
            statement.setInt(1, request.getInt("userID"));
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return locks;
                }
                locks.put("lockDate", rs.getString(1));
                locks.put("endLockDate", rs.getString(2));
                locks.put("reason", rs.getString(3));
                return locks;
            }
        }
    }
}
