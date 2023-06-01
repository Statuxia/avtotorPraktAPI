package ru.avtotor.utils;

import org.mindrot.bcrypt.BCrypt;

public class PasswordEncoder {

    public static String encode(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }
}