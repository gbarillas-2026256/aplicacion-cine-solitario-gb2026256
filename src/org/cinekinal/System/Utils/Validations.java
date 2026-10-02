package org.cinekinal.system.utils;

import java.util.regex.Pattern;

/**
 * Generic form validations, reusable across registration and editing forms.
 */
public class Validations {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public Validations() {
    }

    public Boolean validateTextEmpty(String text) {
        return text == null || text.isBlank();
    }

    public Boolean validateTextLength(String text, int maxLength) {
        return text != null && text.length() <= maxLength;
    }

    public Boolean equalsText(String originalText, String textToCompare) {
        if (originalText == null) {
            return textToCompare == null;
        }
        return originalText.equals(textToCompare);
    }

    public Boolean validateEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
}
