package ru.avtotor.handlers.token;

import lombok.Getter;

public enum TokenPermissions {
    READ(1, "Чтение данных о пользователях"), // 1
    WRITE(1 << 1, "Запись/Изменение данных о пользователях"), // 2
    LOCK(1 << 2, "Блокировка пользователей"), // 4
    MANAGE(1 << 3, "Управление правами пользователей"), // 8

    ADMIN(1 << 30, "Администратор [Роль без прав]"), // 1073741824
    SUPER_ADMIN(Integer.MAX_VALUE, "Супер Администратор [Со всеми правами]"); // 2147483647

    @Getter
    private final int value;
    @Getter
    private final String descriptions;

    TokenPermissions(int value, String descriptions) {
        this.value = value;
        this.descriptions = descriptions;
    }
}
