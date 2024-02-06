package com.hudhud.utils;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class UtilService {

    private final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public String generateRandomUsername(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than zero");
        }

        Random random = new Random();
        StringBuilder username = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALPHABET.length());
            char randomChar = ALPHABET.charAt(randomIndex);
            username.append(randomChar);
        }

        return username.toString();
    }
}
