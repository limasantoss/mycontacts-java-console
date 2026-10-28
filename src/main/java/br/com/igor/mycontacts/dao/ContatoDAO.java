package br.com.igor.mycontacts.dao;

import br.com.igor.mycontacts.exceptions.ContatoNaoEncontradoException;
import br.com.igor.mycontacts.exceptions.PersistenciaException;
import br.com.igor.mycontacts.model.Contato;
import br.com.igor.mycontacts.model.ContatoComercial;
import br.com.igor.mycontacts.repository.Repositorio;
import br.com.igor.mycontacts.utils.Nomes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContatoDAO implements Repositorio<Contato> {
    private final Conexao conexao;

    public ContatoDAO(Conexao conexao) {
        this.conexao = conexao;
        criarTabela();
    }

    private void criarTabela() {
        String sql = """
                CREATE TABLE IF NOT EXISTS contatos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL,
                    telefone TEXT NOT NULL,
                    email TEXT NOT NULL,
                    empresa TEXT,
                    tipo TEXT NOT NULL CHECK (tipo IN ('COMUM', 'COMERCIAL'))
                )
                """;
        try (Connection c = conexao.abrir(); PreparedStatement comando = c.prepareStatement(sql)) {
            comando.executeUpdate();
        } catch (SQLException e) {
            throw falha("preparar", e);
        }
    }

    @Override
    public Contato salvar(Contato contato) {
        boolean novo = contato.getId() == null;
        String sql = novo
                ? "INSERT INTO contatos (nome, telefone, email, empresa, tipo) VALUES (?, ?, ?, ?, ?)"
                : "UPDATE contatos SET nome = ?, telefone = ?, email = ?, empresa = ?, tipo = ? WHERE id = ?";
        try (Connection c = conexao.abrir(); PreparedStatement comando = c.prepareStatement(sql)) {
            comando.setString(1, contato.getNome());
            comando.setString(2, contato.getTelefone());
            comando.setString(3, contato.getEmail());
            if (contato instanceof ContatoComercial comercial) {
                comando.setString(4, comercial.getEmpresa());
                comando.setString(5, "COMERCIAL");
            } else {
                comando.setNull(4, Types.VARCHAR);
                comando.setString(5, "COMUM");
            }
            if (!novo) {
                comando.setLong(6, contato.getId());
            }
            if (comando.executeUpdate() == 0) {
                throw new ContatoNaoEncontradoException("O contato não existe mais. Atualize a lista.");
            }
            if (novo) {
                // O ID pertence à mesma conexão que executou o INSERT.
                try (PreparedStatement id = c.prepareStatement("SELECT last_insert_rowid()");
                        ResultSet resultado = id.executeQuery()) {
                    if (resultado.next()) {
                        contato.setId(resultado.getLong(1));
                    }
                }
            }
            return contato;
        } catch (SQLException e) {
            throw falha("salvar", e);
        }
    }

    @Override
    public List<Contato> listar() {
        String sql = "SELECT id, nome, telefone, email, empresa, tipo FROM contatos ORDER BY nome COLLATE NOCASE, id";
        try (Connection c = conexao.abrir(); PreparedStatement comando = c.prepareStatement(sql);
                ResultSet resultado = comando.executeQuery()) {
            List<Contato> contatos = new ArrayList<>();
            while (resultado.next()) {
                contatos.add(lerContato(resultado));
            }
            return contatos;
        } catch (SQLException e) {
            throw falha("listar", e);
        }
    }

    public List<Contato> buscarPorNome(String nome) {
        String termo = Nomes.normalizar(nome);
        // A comparação Java também ignora caixa em nomes acentuados (LOWER do SQLite é limitado a ASCII).
        return buscar(contato -> Nomes.normalizar(contato.getNome()).contains(termo));
    }

    @Override
    public Optional<Contato> buscarPorId(long id) {
        String sql = "SELECT id, nome, telefone, email, empresa, tipo FROM contatos WHERE id = ?";
        try (Connection c = conexao.abrir(); PreparedStatement comando = c.prepareStatement(sql)) {
            comando.setLong(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(lerContato(resultado)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw falha("buscar", e);
        }
    }

    @Override
    public boolean remover(long id) {
        try (Connection c = conexao.abrir();
                PreparedStatement comando = c.prepareStatement("DELETE FROM contatos WHERE id = ?")) {
            comando.setLong(1, id);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            throw falha("remover", e);
        }
    }

    private Contato lerContato(ResultSet resultado) throws SQLException {
        Contato contato = "COMERCIAL".equals(resultado.getString("tipo"))
                ? new ContatoComercial(resultado.getString("nome"), resultado.getString("telefone"),
                        resultado.getString("email"), resultado.getString("empresa"))
                : new Contato(resultado.getString("nome"), resultado.getString("telefone"), resultado.getString("email"));
        contato.setId(resultado.getLong("id"));
        return contato;
    }

    private PersistenciaException falha(String acao, SQLException causa) {
        return new PersistenciaException("Não foi possível " + acao
                + " os contatos. Verifique o acesso ao banco e tente novamente.", causa);
    }
}
