package ru.avtotor.handlers.auth.pass;

import org.json.JSONObject;
import ru.avtotor.database.mssql.MSSQL;
import ru.avtotor.exceptions.NotFoundException;
import ru.avtotor.handlers.block.lockCheck.LockCheck;
import ru.avtotor.handlers.token.TokenGenerator;
import ru.avtotor.handlers.token.TokenPermissions;
import ru.avtotor.handlers.user.FindUser;
import ru.avtotor.handlers.user.validate.Validate;
import ru.avtotor.logging.ActivityLogger;
import ru.avtotor.logging.LoggerSystem;
import ru.avtotor.utils.PasswordEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PassChanger {

    private final JSONObject request;

    public PassChanger(JSONObject request) {
        this.request = request;
    }

    public JSONObject change() throws SQLException, NotFoundException, ClassNotFoundException {
        try (Connection connection = MSSQL.getConnection()) {
            if (!request.isNull("login")) {
                request.put("userID", FindUser.idByLogin(request.getString("login")));
            }
            if (!request.isNull("token")) {
                request.put("userID", FindUser.idByToken(request.getString("token")));
            }
            return changeByID(connection);
        }
    }

    private JSONObject changeByID(Connection connection) throws SQLException, NotFoundException, ClassNotFoundException {
        JSONObject response = new JSONObject();
        String token = TokenGenerator.generate();

        LoggerSystem.logDebug("check", this.getClass());

        JSONObject request = new JSONObject();
        request.put("userID", this.request.getInt("userID"));
        JSONObject lockCheck = new LockCheck(request).check();
        if (lockCheck.getBoolean("locked")) {
            response.put("code", 403);
            response.put("error-code", "Пользователь заблокирован");
            return response;
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE userSecure set password = ?, token = ? where userID = ?")) {
            statement.setString(1, PasswordEncoder.encode(this.request.getString("newPassword")));
            statement.setString(2, token);
            statement.setInt(3, this.request.getInt("userID"));

            response.put("code", 200);
            response.put("token", token);
            response.put("updated", statement.executeUpdate() != 0);
        }

        ActivityLogger.logHistory(connection, this.request.getInt("userID"),
                "Изменение пароля пользователя " + this.request.getInt("userID"));
        return response;
    }
}
