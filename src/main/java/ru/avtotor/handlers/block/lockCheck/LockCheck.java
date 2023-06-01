package ru.avtotor.handlers.block.lockCheck;

import org.json.JSONObject;
import ru.avtotor.database.mssql.MSSQL;
import ru.avtotor.exceptions.NotFoundException;
import ru.avtotor.handlers.user.FindUser;

import java.sql.*;
import java.time.LocalDateTime;

public class LockCheck {

    private final JSONObject request;

    public LockCheck(JSONObject request) {
        this.request = request;
    }

    public JSONObject check() throws SQLException, ClassNotFoundException, NotFoundException {
        try (Connection connection = MSSQL.getConnection()) {
            if (!request.isNull("login")) {
                request.put("userID", FindUser.idByLogin(request.getString("login")));
            }
            if (!request.isNull("token")) {
                request.put("userID", FindUser.idByToken(request.getString("token")));
            }

            return checkByID(connection);
        }
    }

    private JSONObject checkByID(Connection connection) throws SQLException {
        JSONObject answer = new JSONObject();

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT userID, lockDate, endLockDate, reason from userLocks where userID = ?")) {
            statement.setInt(1, request.getInt("userID"));

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp(3);
                    if (ts.toLocalDateTime().isAfter(LocalDateTime.now())) {
                        JSONObject info = new JSONObject();

                        info.put("userID", rs.getInt(1));
                        info.put("lockDate", rs.getTimestamp(2).toLocalDateTime());
                        info.put("endLockDate", rs.getTimestamp(3).toLocalDateTime());
                        info.put("reason", rs.getString(4));

                        answer.put("code", 200);
                        answer.put("locked", true);
                        answer.put("info", info);
                        return answer;
                    }
                }

                answer.put("code", 200);
                answer.put("locked", false);
                return answer;
            }
        }
    }
}
