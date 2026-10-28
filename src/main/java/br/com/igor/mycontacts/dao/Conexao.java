package br.com.igor.mycontacts.dao;

import br.com.igor.mycontacts.exceptions.PersistenciaException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Abre uma conexão por operação. Quem abre deve fechá-la com try-with-resources. */
public class Conexao {
    private final String url;
    private final Path arquivo;

    public Conexao() {
        this(Path.of(System.getProperty("mycontacts.db", "dados/contatos.db")));
    }

    public Conexao(Path arquivo) {
        this.arquivo = arquivo.toAbsolutePath().normalize();
        this.url = "jdbc:sqlite:" + this.arquivo;
    }

    /** Permite um banco SQLite em memória compartilhado nos testes. */
    public Conexao(String url) {
        this.url = url;
        this.arquivo = null;
    }

    public Connection abrir() {
        try {
            if (arquivo != null) {
                Files.createDirectories(arquivo.getParent());
            }
            Connection conexao = DriverManager.getConnection(url);
            try (PreparedStatement comando = conexao.prepareStatement("PRAGMA busy_timeout = 5000")) {
                comando.execute();
                return conexao;
            } catch (SQLException e) {
                conexao.close();
                throw e;
            }
        } catch (SQLException | IOException e) {
            throw new PersistenciaException(
                    "Não foi possível abrir o banco de contatos. Verifique o caminho e a permissão de acesso.", e);
        }
    }
}
