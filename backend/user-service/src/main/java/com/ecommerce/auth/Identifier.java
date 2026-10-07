package com.ecommerce.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * An email address or an Indian mobile number typed into a login form, in normalised form.
 */
public record Identifier(Type type, String value) {

    public enum Type { EMAIL, MOBILE }

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
    private static final Pattern INDIAN_MOBILE = Pattern.compile("^[6-9]\\d{9}$");

    public static Identifier parse(String raw) {
        String input = raw == null ? "" : raw.trim();
        if (input.contains("@")) {
            String email = input.toLowerCase(Locale.ROOT);
            if (EMAIL.matcher(email).matches()) {
                return new Identifier(Type.EMAIL, email);
            }
        } else {
            String digits = input.replaceAll("[\\s()+-]", "");
            if (digits.length() == 12 && digits.startsWith("91")) {
                digits = digits.substring(2);
            } else if (digits.length() == 11 && digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            if (INDIAN_MOBILE.matcher(digits).matches()) {
                return new Identifier(Type.MOBILE, digits);
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Enter a valid email address or 10-digit mobile number");
    }

    public boolean isEmail() {
        return type == Type.EMAIL;
    }

    /** Key used to store the pending OTP for this identifier. */
    public String key() {
        return type.name().toLowerCase(Locale.ROOT) + ":" + value;
    }

    /** Safe to show back to the user, e.g. "ra****@gmail.com" or "+91 ******3210". */
    public String masked() {
        if (isEmail()) {
            int at = value.indexOf('@');
            String name = value.substring(0, at);
            String visible = name.length() <= 2 ? name.substring(0, 1) : name.substring(0, 2);
            return visible + "****" + value.substring(at);
        }
        return "+91 ******" + value.substring(6);
    }
}
