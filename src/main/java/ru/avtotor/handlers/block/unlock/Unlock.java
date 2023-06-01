package ru.avtotor.handlers.block.unlock;

import org.json.JSONObject;
import ru.avtotor.database.mssql.MSSQL;
import ru.avtotor.exceptions.NotFoundException;
import ru.avtotor.handlers.block.lockCheck.LockCheck;
import ru.avtotor.handlers.token.TokenPermissions;
import ru.avtotor.handlers.user.validate.Validate;
import ru.avtotor.handlers.user.FindUser;
import ru.avtotor.logging.ActivityLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Unlock {

    private final JSONObject request;

    public Unlock(JSONObject request) {
        this.request = request;
    }

    public JSONObject unlock() throws SQLException, NotFoundException, ClassNotFoundException {
        try (Connection connection = MSSQL.getConnection()) {
            if (!request.isNull("login")) {
                request.put("userID", FindUser.idByLogin(request.getString("login")));
            }
            if (!request.isNull("token")) {
                request.put("userID", FindUser.idByToken(request.getString("token")));
            }
            return unlockByID(connection);
        }
    }

    public JSONObject unlockByID(Connection connection) throws ClassNotFoundException, SQLException, NotFoundException {
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

        String statementString;
        String optionalMessage;
        if (!request.isNull("all")) {
            statementString = "DELETE FROM userLocks WHERE userID = ?";
            optionalMessage = "Разблокировка пользователя ";
        } else {
            statementString = "DELETE TOP(1) FROM userLocks WHERE userID = ? and userID = " +
                    "(SELECT TOP(1) userID FROM userLocks WHERE userID = ? ORDER BY lockDate);";
            optionalMessage = "Полная разблокировка пользователя ";
        }

        try (PreparedStatement statement = connection.prepareStatement(statementString)) {
            statement.setInt(1, request.getInt("userID"));
            if (request.isNull("all")) {
                statement.setInt(2, request.getInt("userID"));
            }

            response.put("code", 200);
            response.put("unlocked", statement.executeUpdate() != 0);
        }

        ActivityLogger.logHistory(connection, lockerID,
                optionalMessage + this.request.getInt("userID"));
        return response;
    }
}
