package ru.avtotor.request.method;

import lombok.Getter;

public enum HttpMethod {

    GET((byte) 1), // 1
    POST((byte) (1 << 1)), // 2
    PUT((byte) (1 << 2)), // 4
    DELETE((byte) (1 << 3)), // 8
    OPTIONS((byte) (1 << 4)), // 16
    CONNECT((byte) (1 << 5)), // 32
    HEAD((byte) (1 << 6)), // 64
    TRACE((byte) (1 << 7)), // 128
    ANY(Byte.MAX_VALUE); // 255

    @Getter
    public final byte value;

    HttpMethod(byte value) {
        this.value = value;
    }
}
