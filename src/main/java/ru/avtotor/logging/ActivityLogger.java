package ru.avtotor.logging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class ActivityLogger {

    public static void logHistory(Connection connection, int userID, String message) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO activityHistory (userID, activity, datetime) VALUES (?, ?, ?)")) {
            statement.setInt(1, userID);
            statement.setString(2, message);
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            statement.executeUpdate();
        }
    }
}
