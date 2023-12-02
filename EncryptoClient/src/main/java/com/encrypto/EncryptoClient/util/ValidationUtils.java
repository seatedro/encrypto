package com.encrypto.EncryptoClient.util;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidComplexPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(new String(password)).matches();
    }

    public static String getPasswordValidationErrors(char[] password) {
        if (password == null || password.length == 0) {
            return "Password cannot be empty.";
        }
        if (password.length < 8) {
            return "Password must be at least 8 characters.";
        }
        if (!String.valueOf(password).matches(".*[0-9].*")) {
            return "Password must contain at least one digit.";
        }
        if (!String.valueOf(password).matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter.";
        }
        if (!String.valueOf(password).matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter.";
        }
        if (!String.valueOf(password).matches(".*[@#$%^&+=].*")) {
            return "Password must contain at least one special character (@#$%^&+=).";
        }
        // All checks passed.
        return null;
    }
}
