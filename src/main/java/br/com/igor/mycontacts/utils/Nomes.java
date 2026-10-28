package br.com.igor.mycontacts.utils;

import java.util.Locale;

public final class Nomes {
    private Nomes() {
    }

    public static String normalizar(String nome) {
        return nome == null ? "" : nome.strip().toLowerCase(Locale.ROOT);
    }
}
