package com.bluebook.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordUtils {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public boolean validatePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 16) {
            return false;
        }

        int count = 0;
        if (Pattern.matches(".*\\d.*", password)) {
            count++;
        }
        if (Pattern.matches(".*[a-zA-Z].*", password)) {
            count++;
        }
        if (Pattern.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\\\"\\\\|,.<>\\/?].*", password)) {
            count++;
        }

        return count >= 2;
    }

    public String encodePassword(String password) {
        return encoder.encode(password);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}