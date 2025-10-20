package com.usei.usei.util;

import java.util.regex.Pattern;

public final class PasswordPolicyUtil {
    private PasswordPolicyUtil(){}

    public static final int MIN_LENGTH = 12;     // longitud mínima
    public static final int COMPLEJIDAD = 4;     // mayús, minús, número, especial
    public static final int MAX_INTENTOS = 3;    // intentos permitidos
    public static final int EXPIRA_DIAS = 60;    // caducidad en días
    public static final int NO_REUSE_MESES = 12; // no reutilizar por 12 meses

    private static final Pattern UPPER   = Pattern.compile("[A-Z]");
    private static final Pattern LOWER   = Pattern.compile("[a-z]");
    private static final Pattern DIGIT   = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    public static boolean cumplePolitica(String plain) {
        if (plain == null || plain.length() < MIN_LENGTH) return false;
        if (!UPPER.matcher(plain).find()) return false;
        if (!LOWER.matcher(plain).find()) return false;
        if (!DIGIT.matcher(plain).find()) return false;
        if (!SPECIAL.matcher(plain).find()) return false;
        return true;
    }
}
