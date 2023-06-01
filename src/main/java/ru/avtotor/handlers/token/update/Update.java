package ru.avtotor.handlers.token.update;

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

public class Update {

    private final JSONObject request;

    public Update(JSONObject request) {
        this.request = request;
    }

    public JSONObject update() throws SQLException, NotFoundException, ClassNotFoundException {
        try (Connection connection = MSSQL.getConnection()) {
            if (!request.isNull("login")) {
                request.put("userID", FindUser.idByLogin(request.getString("login")));
            }
            if (!request.isNull("token")) {
                request.put("userID", FindUser.idByToken(request.getString("token")));
            }
            return updateByID(connection);
        }
    }

    public JSONObject updateByID(Connection connection) throws NotFoundException, SQLException, ClassNotFoundException {
        JSONObject response = new JSONObject();

        JSONObject managerRequest = new JSONObject();
        managerRequest.put("token", request.getString("managerToken"));
        managerRequest.put("perms", TokenPermissions.ADMIN.getValue() + TokenPermissions.MANAGE.getValue());
        JSONObject managerResponse = Validate.validate(connection, managerRequest);
        int managerID = managerResponse.getInt("userID");
        int userID = request.getInt("userID");

        if (!managerResponse.getBoolean("valid")) {
            response.put("code", 403);
            response.put("error-code", "Недостаточно прав");
            return response;
        }

        managerRequest.remove("perms");
        JSONObject lockCheck = new LockCheck(managerRequest).check();
        if (lockCheck.getBoolean("locked")) {
            response.put("code", 403);
            response.put("error-code", "Менеджер пользователей заблокирован");
            return response;
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE userSecure set perms = ? where userID = ?")) {
            statement.setInt(1, request.getInt("perms"));
            statement.setInt(2, userID);

            if (statement.executeUpdate() != 0) {
                response.put("code", 200);
                response.put("updated", true);
            } else {
                response.put("code", 200);
                response.put("updated", false);
            }
        }

        ActivityLogger.logHistory(connection, managerID, "Изменение прав токена у пользователя" + userID);
        return response;
    }
}
