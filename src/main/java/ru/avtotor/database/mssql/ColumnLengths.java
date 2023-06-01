package ru.avtotor.database.mssql;

import lombok.Getter;

import java.util.HashMap;

public class ColumnLengths {

    @Getter
    private static final HashMap<String, Integer[]> userColumns = new HashMap<>();

    static {
        userColumns.put("surname", new Integer[]{2, 50});
        userColumns.put("name", new Integer[]{2, 50});
        userColumns.put("middleName", new Integer[]{2, 50});
        userColumns.put("login", new Integer[]{2, 16});
        userColumns.put("password", new Integer[]{8, 60});
        userColumns.put("oldPassword", new Integer[]{8, 60});
        userColumns.put("newPassword", new Integer[]{8, 60});
        userColumns.put("address", new Integer[]{2, 255});
        userColumns.put("phone", new Integer[]{11, 11});
        userColumns.put("sex", new Integer[]{3, 3});
        userColumns.put("token", new Integer[]{32, 32});
        userColumns.put("lockerToken", new Integer[]{32, 32});
        userColumns.put("managerToken", new Integer[]{32, 32});
        userColumns.put("readerToken", new Integer[]{32, 32});
    }
}
