package ru.avtotor.handlers.user;

import ru.avtotor.database.mssql.MSSQL;
import ru.avtotor.exceptions.NotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FindUser {

    public static int idByToken(String token) throws SQLException, ClassNotFoundException, NotFoundException {
        try (Connection connection = MSSQL.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT userID from userSecure where token = ?")) {
                statement.setString(1, token);
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        throw new NotFoundException("Пользователь с токеном '" + token + "' не найден");
                    }
                    return rs.getInt(1);
                }
            }
        }
    }

    public static int idByLogin(String login) throws SQLException, ClassNotFoundException, NotFoundException {
        try (Connection connection = MSSQL.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT userID from userSecure where login = ?")) {
                statement.setString(1, login);
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        throw new NotFoundException("Пользователь с логином '" + login + "' не найден");
                    }
                    return rs.getInt(1);
                }
            }
        }
    }

    public static int loginByID(int userID) throws SQLException, ClassNotFoundException, NotFoundException {
        try (Connection connection = MSSQL.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT login from userSecure where userID = ?")) {
                statement.setInt(1, userID);
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        throw new NotFoundException("Пользователь с userID '" + userID + "' не найден");
                    }
                    return rs.getInt(1);
                }
            }
        }
    }

    public static int loginByToken(String token) throws SQLException, ClassNotFoundException, NotFoundException {
        try (Connection connection = MSSQL.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT login from userSecure where token = ?")) {
                statement.setString(1, token);
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        throw new NotFoundException("Пользователь с токеном '" + token + "' не найден");
                    }
                    return rs.getInt(1);
                }
            }
        }
    }

    public static int tokenByID(int userID) throws SQLException, ClassNotFoundException, NotFoundException {
        try (Connection connection = MSSQL.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT token from userSecure where userID = ?")) {
                statement.setInt(1, userID);
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        throw new NotFoundException("Пользователь с userID '" + userID + "' не найден");
                    }
                    return rs.getInt(1);
                }
            }
        }
    }

    public static int tokenByLogin(String login) throws SQLException, ClassNotFoundException, NotFoundException {
        try (Connection connection = MSSQL.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT token from userSecure where login = ?")) {
                statement.setString(1, login);
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        throw new NotFoundException("Пользователь с логином '" + login + "' не найден");
                    }
                    return rs.getInt(1);
                }
            }
        }
    }
}
