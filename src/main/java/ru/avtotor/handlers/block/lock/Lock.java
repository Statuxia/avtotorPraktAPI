package ru.avtotor.handlers.block.lock;

import org.json.JSONObject;
import ru.avtotor.database.mssql.MSSQL;
import ru.avtotor.exceptions.NotFoundException;
import ru.avtotor.handlers.block.lockCheck.LockCheck;
import ru.avtotor.handlers.token.TokenGenerator;
import ru.avtotor.handlers.token.TokenPermissions;
import ru.avtotor.handlers.user.validate.Validate;
import ru.avtotor.handlers.user.FindUser;
import ru.avtotor.logging.ActivityLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Lock {

    private final JSONObject request;

    public Lock(JSONObject request) {
        Timestamp.valueOf(request.getString("expires"));
        this.request = request;
    }

    public JSONObject lock() throws SQLException, NotFoundException, ClassNotFoundException {
        try (Connection connection = MSSQL.getConnection()) {
            if (!request.isNull("login")) {
                request.put("userID", FindUser.idByLogin(request.getString("login")));
            }
            if (!request.isNull("token")) {
                request.put("userID", FindUser.idByToken(request.getString("token")));
            }
            return lockByID(connection);
        }
    }

    private JSONObject lockByID(Connection connection) throws SQLException, NotFoundException, ClassNotFoundException {
        JSONObject response = new JSONObject();

        JSONObject lockerRequest = new JSONObject();
        lockerRequest.put("token", this.request.getString("lockerToken"));
        lockerRequest.put("perms", TokenPermissions.LOCK.getValue());
        JSONObject lockerResponse = Validate.validate(connection, lockerRequest);
        int lockerID = FindUser.idByToken(this.request.getString("lockerToken"));

        if (!lockerResponse.getBoolean("valid")) {
            response.put("code", 403);
            response.put("error-code", "Недостаточно прав");
            return response;
        }

        lockerRequest.remove("perms");
        JSONObject lockCheck = new LockCheck(lockerRequest).check();
        if (lockCheck.getBoolean("locked")) {
            response.put("code", 403);
            response.put("error-code", "Управляющий блокировками заблокирован");
            return response;
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE userSecure set token = ? where userID = ?")) {
            statement.setString(1, TokenGenerator.generate());
            statement.setInt(2, this.request.getInt("userID"));
            statement.executeUpdate();
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO userLocks (userID, lockDate, endLockDate, reason) VALUES (?, ?, ?, ?)")) {
            statement.setInt(1, this.request.getInt("userID"));
            statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            statement.setTimestamp(3, Timestamp.valueOf(this.request.getString("expires")));
            statement.setString(4, this.request.getString("reason"));

            response.put("code", 200);
            response.put("locked", statement.executeUpdate() != 0);
        }

        ActivityLogger.logHistory(connection, lockerID,
                "Блокировка пользователя " + this.request.getInt("userID"));
        return response;
    }
}
