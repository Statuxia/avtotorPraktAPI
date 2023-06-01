package ru.avtotor.handlers.token;

import org.apache.commons.lang3.RandomStringUtils;

public class TokenGenerator {

    public static String generate() {
        int length = 32;
        boolean useLetters = true;
        boolean useNumbers = true;
        return RandomStringUtils.random(length, useLetters, useNumbers);
    }
}
