package com.classbuddy.app.util;


import java.security.SecureRandom;

public class CodeGenerator {

    private static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String NUMERIC = "0123456789";
    private static final SecureRandom random = new SecureRandom();

    public static String generateClassroomCode() {
        return generateCode(Constants.CLASSROOM_CODE_LENGTH, ALPHA_NUMERIC);
    }

    public static String generatePassword(int length) {
        return generateCode(length, NUMERIC);
    }

    public static String generateUniqueId() {
        return generateCode(16, ALPHA_NUMERIC);
    }

    private static String generateCode(int length, String characters) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

    public static String formatCodeForDisplay(String code) {
        if (code == null || code.length() <= 3) return code;

        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < code.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                formatted. append("-");
            }
            formatted.append(code.charAt(i));
        }
        return formatted.toString();
    }

    public static String removeFormatting(String formattedCode) {
        if (formattedCode == null) return null;
        return formattedCode. replace("-", "").replace(" ", "").toUpperCase();
    }
}
