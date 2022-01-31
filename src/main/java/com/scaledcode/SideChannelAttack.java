package com.scaledcode;

import java.util.Arrays;

public class SideChannelAttack {
    private static char[] possibleCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQURSTUWXYZ1234567890".toCharArray();

    private static boolean verifyPassword(String realPassword, String providedPassword) {
        if (realPassword.length() != providedPassword.length()) {
            return false;
        }

        boolean mismatch = false;
        for (int i = 0; i < realPassword.length(); i++) {
            mismatch = (realPassword.charAt(i) != providedPassword.charAt(i)) || mismatch;
            //result |= realPassword.charAt(i) ^ providedPassword.charAt(i);
//            try {
//                Thread.sleep(1);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }

        return !mismatch;
    }

    private static void crackPassword(String realPassword) {
        int passwordSize = guessSize(realPassword);
        //int passwordSize = 4;
        System.out.println("Size: " + passwordSize);
        System.out.println("Final result: " + crackPassword(realPassword, "", passwordSize, passwordSize));

    }

    private static String crackPassword(String realPassword, String prefix, int remainingCharacters, int fullSize) {
        if (remainingCharacters == 0) {
            return prefix;
        }

        var sizeTimers = new long[possibleCharacters.length];

        for (int i=0; i<sizeTimers.length; i++) {
            String testPassword = padding(prefix + possibleCharacters[i], fullSize);
            var result = new long[10001];

            for (int j=0; j < result.length; j++) {
                long start = System.nanoTime();

                verifyPassword(realPassword, testPassword);
                result[j] = System.nanoTime() - start;
            }

            Arrays.sort(result);

            sizeTimers[i] =  result[5000];
        }

        var currentGuess = prefix + possibleCharacters[largestIndex(sizeTimers)];

        System.out.println("Current known: " + currentGuess);

        return crackPassword(realPassword, currentGuess, remainingCharacters -1, fullSize);
    }

    private  static int guessSize(String realPassword) {
        var sizeTimers = new long[18];

        for (int i=0; i<sizeTimers.length; i++) {
            String testStringOfLength = stringOfSize(i+1);
            long start = System.nanoTime();
            for (int j=0; j<1001; j++) {

                verifyPassword(realPassword, testStringOfLength);
            }
            sizeTimers[i] =  System.nanoTime() - start;;
        }

        return largestIndex(sizeTimers) + 1;
    }

    private static int largestIndex(long timings[]) {
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
        var padded = current;
        for (int i=current.length(); i<size; i++) {
            padded += 'a';
        }
        return padded;
    }


    private static String stringOfSize(int size) {
        StringBuilder builder = new StringBuilder(size);
        for (int i=0; i<size; i++) {
            builder.append('A');
        }

        return builder.toString();
    }

    public static void main(String[] args) {
        crackPassword(args[0]);
    }
}
