package com.hudhud.utils;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class UtilService {

    private final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String[] STARTS = {
            "kr", "ca", "ra", "mro", "sme", "clo", "bla", "gra",
            "tri", "pra", "sto", "spra", "cro", "pla", "cla", "fla", "sla"
    };
    private static final String[] MIDDLES = {
            "in", "ar", "an", "al", "er", "on", "un", "at",
            "et", "it", "ot", "ut", "ect", "ort", "ent", "int", "ant"
    };
    private static final String[] ENDS = {
            "do", "so", "ty", "lo", "mi", "poo", "ron", "mo",
            "no", "bo", "to", "ro", "co", "jo", "fo", "go", "ho"
    };

    private Random random = new Random();

    public String generateRandomUsername() {
        String start = STARTS[random.nextInt(STARTS.length)];
        String middle = MIDDLES[random.nextInt(MIDDLES.length)];
        String end = ENDS[random.nextInt(ENDS.length)];
        return start + middle + end;
    }
//    public String generateRandomUsername(int length) {
//        if (length <= 0) {
//            throw new IllegalArgumentException("Length must be greater than zero");
//        }
//
//        Random random = new Random();
//        StringBuilder username = new StringBuilder(length);
//
//        for (int i = 0; i < length; i++) {
//            int randomIndex = random.nextInt(ALPHABET.length());
//            char randomChar = ALPHABET.charAt(randomIndex);
//            username.append(randomChar);
//        }
//
//        return username.toString();
//    }

    public String generatePassword() {
        String characters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        int length = 10;

        StringBuilder otp = new StringBuilder();

        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());

            otp.append(characters.charAt(index));
        }

        return otp.toString();
    }
}
