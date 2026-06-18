package br.com.igor.mycontacts.utils;

public final class ValidadorEmail {

    private ValidadorEmail() {
    }

    public static boolean validar(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        return email.contains("@") && email.contains(".");
    }
}
