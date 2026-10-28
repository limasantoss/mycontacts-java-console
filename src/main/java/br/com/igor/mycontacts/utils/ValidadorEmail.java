package br.com.igor.mycontacts.utils;

import java.util.regex.Pattern;

public final class ValidadorEmail {

    // Validação de formato para a agenda; não verifica a existência da caixa postal.
    private static final Pattern FORMATO = Pattern.compile(
            "^[A-Za-z0-9_%+\\-]+(?:\\.[A-Za-z0-9_%+\\-]+)*@"
            + "(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}$");

    private ValidadorEmail() {
    }

    public static boolean validar(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        return email.length() <= 254 && FORMATO.matcher(email).matches();
    }
}
