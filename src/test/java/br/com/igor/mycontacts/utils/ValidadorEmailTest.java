package br.com.igor.mycontacts.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class ValidadorEmailTest {
    @ParameterizedTest
    @ValueSource(strings = {"igor@exemplo.com", "nome.sobrenome+curso@empresa.com.br", "USER_1@sub.exemplo.org"})
    void aceitaFormatosValidos(String email) {
        assertTrue(ValidadorEmail.validar(email));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "invalido", "a@b", "a.b@", "@exemplo.com", "a@@exemplo.com",
            "a b@exemplo.com", "a@.com", "a@-exemplo.com", "a@exemplo..com", ".a@exemplo.com",
            "a..b@exemplo.com", "a@exemplo.com "})
    void rejeitaFormatosInvalidos(String email) {
        assertFalse(ValidadorEmail.validar(email));
    }

    @Test
    void rejeitaEnderecoMuitoLongo() {
        assertFalse(ValidadorEmail.validar("a".repeat(250) + "@exemplo.com"));
    }
}
