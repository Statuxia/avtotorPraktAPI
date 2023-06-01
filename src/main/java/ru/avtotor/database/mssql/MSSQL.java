package ru.avtotor.database.mssql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MSSQL {

    private static final String CONNECTION_STRING = "jdbc:sqlserver://SRV-API-TEST;encrypt=true;databaseName=api-test;" +
            "user=maslovsmTest;password=349192@pass;trustServerCertificate=true";

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        return DriverManager.getConnection(CONNECTION_STRING);
    }
}
