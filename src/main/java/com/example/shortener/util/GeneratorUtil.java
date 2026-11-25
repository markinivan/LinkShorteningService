package com.example.shortener.util;
import java.util.UUID;

public class GeneratorUtil {
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final java.security.SecureRandom RANDOM = new java.security.SecureRandom();

    public static String generateShortCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
