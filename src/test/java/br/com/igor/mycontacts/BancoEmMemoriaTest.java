package br.com.igor.mycontacts;

import br.com.igor.mycontacts.controller.Agenda;
import br.com.igor.mycontacts.dao.Conexao;
import br.com.igor.mycontacts.dao.ContatoDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/** Cada teste ganha um banco próprio; a conexão âncora mantém o banco vivo entre operações. */
public abstract class BancoEmMemoriaTest {
    protected Conexao conexao;
    protected Connection ancora;
    protected ContatoDAO dao;
    protected Agenda agenda;

    @BeforeEach
    void prepararBanco() {
        conexao = new Conexao("jdbc:sqlite:file:teste-" + UUID.randomUUID() + "?mode=memory&cache=shared");
        ancora = conexao.abrir();
        dao = new ContatoDAO(conexao);
        agenda = new Agenda(dao);
    }

    @AfterEach
    void fecharBanco() throws SQLException {
        if (ancora != null) {
            ancora.close();
        }
    }
}
