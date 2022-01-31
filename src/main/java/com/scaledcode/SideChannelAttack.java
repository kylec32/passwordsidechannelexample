package com.scaledcode;

import java.util.Arrays;

public class SideChannelAttack {
    private static final char[] possibleCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQURSTUWXYZ1234567890".toCharArray();

    private static boolean verifyPassword(String realPassword, String providedPassword, VerificationMethod method) {
        if (realPassword.length() != providedPassword.length()) {
            return false;
        }

        switch (method) {
            case NAIVE:
                return verifyPasswordNaive(realPassword, providedPassword);
            case BITWISE:
                return verifyPasswordBitwise(realPassword, providedPassword);
            case SAFE_READABLE:
                return verifyPasswordSafeReadable(realPassword, providedPassword);
            default:
                throw new RuntimeException("Unknown method: " + method);
        }
    }

    private static boolean verifyPasswordNaive(String realPassword, String providedPassword) {
        for (int i = 0; i < realPassword.length(); i++) {
            if (realPassword.charAt(i) != providedPassword.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    private static boolean verifyPasswordBitwise(String realPassword, String providedPassword) {
        int result = 0;
        for (int i = 0; i < realPassword.length(); i++) {
            result |= realPassword.charAt(i) ^ providedPassword.charAt(i);
        }

        return result == 0;
    }

    private static boolean verifyPasswordSafeReadable(String realPassword, String providedPassword) {
        boolean mismatch = false;
        for (int i = 0; i < realPassword.length(); i++) {
            mismatch = (realPassword.charAt(i) != providedPassword.charAt(i)) || mismatch;
        }

        return !mismatch;
    }

    private static void crackPassword(String realPassword, VerificationMethod method) {
        int passwordSize = guessSize(realPassword, method);

        System.out.println("Size: " + passwordSize);
        System.out.println("Final result: " + crackPassword(realPassword, "", passwordSize, passwordSize, method));
    }

    private static String crackPassword(String realPassword, String prefix, int remainingCharacters, int fullSize, VerificationMethod method) {
        if (remainingCharacters == 0) {
            return prefix;
        }

        var sizeTimers = new long[possibleCharacters.length];

        for (int i=0; i<sizeTimers.length; i++) {
            String testPassword = padding(prefix + possibleCharacters[i], fullSize);
            var result = new long[10001];

            for (int j=0; j < result.length; j++) {
                long start = System.nanoTime();

                verifyPassword(realPassword, testPassword, method);
                result[j] = System.nanoTime() - start;
            }

            Arrays.sort(result);

            sizeTimers[i] =  result[5000];
        }

        var currentGuess = prefix + possibleCharacters[largestIndex(sizeTimers)];

        System.out.println("Current known: " + currentGuess);

        return crackPassword(realPassword, currentGuess, remainingCharacters -1, fullSize, method);
    }

    private  static int guessSize(String realPassword, VerificationMethod method) {
        var sizeTimers = new long[18];

        for (int i=0; i<sizeTimers.length; i++) {
            String testStringOfLength = stringOfSize(i+1);
            long start = System.nanoTime();
            for (int j=0; j<1001; j++) {

                verifyPassword(realPassword, testStringOfLength, method);
            }
            sizeTimers[i] =  System.nanoTime() - start;;
        }

        return largestIndex(sizeTimers) + 1;
    }

    private static int largestIndex(long[] timings) {
        int largestIndex = -1;
        long largestValue = -1;
        for (int i=0; i<timings.length; i++) {
            if (timings[i] > largestValue) {
                largestIndex = i;
                largestValue = timings[i];
            }
        }

        return largestIndex;
    }

    private static String padding(String current, int size) {
        return current + "a".repeat(Math.max(0, size - current.length()));
    }

    private static String stringOfSize(int size) {
        return "A".repeat(Math.max(0, size));
    }

    public static void main(String[] args) {
        VerificationMethod method = VerificationMethod.NAIVE;
        if (args.length > 1) {
            method = VerificationMethod.valueOf(args[1]);
        }
        crackPassword(args[0], method);
    }

    private enum VerificationMethod {
        NAIVE,
        BITWISE,
        SAFE_READABLE
    }
}
