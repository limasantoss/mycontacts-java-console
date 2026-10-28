package br.com.igor.mycontacts.dao;

import br.com.igor.mycontacts.BancoEmMemoriaTest;
import br.com.igor.mycontacts.exceptions.ContatoNaoEncontradoException;
import br.com.igor.mycontacts.exceptions.PersistenciaException;
import br.com.igor.mycontacts.model.Contato;
import br.com.igor.mycontacts.model.ContatoComercial;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class ContatoDAOTest extends BancoEmMemoriaTest {
    @TempDir Path temporario;

    @Test
    void cadastraListaEBuscaPorId() {
        Contato contato = dao.salvar(new Contato("Ana", "85999990000", "ana@exemplo.com"));
        assertNotNull(contato.getId());
        Contato recuperado = dao.buscarPorId(contato.getId()).orElseThrow();
        assertEquals(Contato.class, recuperado.getClass());
        assertEquals("Ana", recuperado.getNome());
        assertEquals("85999990000", recuperado.getTelefone());
        assertEquals("ana@exemplo.com", recuperado.getEmail());
        assertEquals(1, dao.listar().size());
        assertTrue(dao.buscarPorId(9999).isEmpty());
    }

    @Test
    void buscaParcialIgnoraCaixaEspacosETrataAcentos() {
        dao.salvar(new Contato("Ágata Silva", "85", "a@exemplo.com"));
        dao.salvar(new Contato("Bruno", "11", "b@exemplo.com"));
        assertEquals(1, dao.buscarPorNome(" ÁGATA ").size());
        assertEquals(1, dao.buscarPorNome("silva").size());
        assertTrue(dao.buscarPorNome("inexistente").isEmpty());
        assertEquals(2, dao.buscarPorNome("").size());
    }

    @Test
    void atualizaTodosOsCamposSemMudarId() {
        Contato contato = dao.salvar(new Contato("Ana", "85", "ana@exemplo.com"));
        Long id = contato.getId();
        contato.setNome("Bia");
        contato.setTelefone("11");
        contato.setEmail("bia@exemplo.com");
        dao.salvar(contato);
        Contato recuperado = dao.buscarPorId(id).orElseThrow();
        assertEquals(id, recuperado.getId());
        assertEquals("Bia", recuperado.getNome());
        assertEquals("11", recuperado.getTelefone());
        assertEquals("bia@exemplo.com", recuperado.getEmail());
        assertEquals(1, dao.listar().size());
    }

    @Test
    void removeSomenteIdEscolhidoMesmoComNomesIguais() {
        Contato primeiro = dao.salvar(new Contato("Ana", "85", "a@exemplo.com"));
        Contato segundo = dao.salvar(new Contato("Ana", "11", "b@exemplo.com"));
        assertNotEquals(primeiro.getId(), segundo.getId());
        assertTrue(dao.remover(primeiro.getId()));
        assertTrue(dao.buscarPorId(primeiro.getId()).isEmpty());
        assertTrue(dao.buscarPorId(segundo.getId()).isPresent());
        assertFalse(dao.remover(primeiro.getId()));
    }

    @Test
    void preservaComercialEAempresaAposAtualizacao() {
        ContatoComercial comercial = new ContatoComercial("Ana", "85", "a@exemplo.com", "Empresa A");
        dao.salvar(comercial);
        comercial.setEmpresa("Empresa B");
        dao.salvar(comercial);
        ContatoComercial recuperado = assertInstanceOf(ContatoComercial.class,
                dao.buscarPorId(comercial.getId()).orElseThrow());
        assertEquals("Empresa B", recuperado.getEmpresa());
    }

    @Test
    void preservaTipoComercialComEmpresaNulaEVazia() {
        for (String empresa : new String[]{null, ""}) {
            ContatoComercial contato = new ContatoComercial("Ana", "85", "a@exemplo.com", empresa);
            dao.salvar(contato);
            ContatoComercial recuperado = assertInstanceOf(ContatoComercial.class,
                    dao.buscarPorId(contato.getId()).orElseThrow());
            assertEquals(empresa, recuperado.getEmpresa());
        }
    }

    @Test
    void permiteTrocarTipoMantendoIdELimpaEmpresaDoComum() throws Exception {
        Contato original = dao.salvar(new Contato("Ana", "85", "a@exemplo.com"));
        ContatoComercial comercial = new ContatoComercial("Ana", "85", "a@exemplo.com", "Loja");
        comercial.setId(original.getId());
        dao.salvar(comercial);
        assertInstanceOf(ContatoComercial.class, dao.buscarPorId(original.getId()).orElseThrow());
        dao.salvar(original);
        assertEquals(Contato.class, dao.buscarPorId(original.getId()).orElseThrow().getClass());
        try (PreparedStatement comando = ancora.prepareStatement("SELECT empresa FROM contatos WHERE id = ?")) {
            comando.setLong(1, original.getId());
            try (ResultSet resultado = comando.executeQuery()) {
                assertTrue(resultado.next());
                assertNull(resultado.getString(1));
            }
        }
    }

    @Test
    void naoInsereQuandoIdDaEdicaoNaoExiste() {
        Contato contato = new Contato("Ana", "85", "a@exemplo.com");
        contato.setId(999L);
        assertThrows(ContatoNaoEncontradoException.class, () -> dao.salvar(contato));
        assertTrue(dao.listar().isEmpty());
    }

    @Test
    void preservaTextoComAspasECaracteresSql() {
        Contato contato = dao.salvar(new Contato("D'Ávila %_'; DROP TABLE contatos; --", "85", "a@exemplo.com"));
        assertEquals(contato.getNome(), dao.buscarPorId(contato.getId()).orElseThrow().getNome());
        assertEquals(1, dao.buscarPorNome("%_").size());
        assertTrue(dao.buscarPorNome("' OR 1=1 --").isEmpty());
    }

    @Test
    void reabreBancoEmArquivoEPreservaComumEComercial() throws Exception {
        Path arquivo = temporario.resolve("dados/agenda.db");
        ContatoDAO primeiraExecucao = new ContatoDAO(new Conexao(arquivo));
        Contato comum = primeiraExecucao.salvar(new Contato("Ana", "85", "a@exemplo.com"));
        Contato comercial = primeiraExecucao.salvar(new ContatoComercial("Bia", "11", "b@exemplo.com", "Loja"));
        // Todas as conexões da primeira execução já foram fechadas pelo DAO.
        ContatoDAO segundaExecucao = new ContatoDAO(new Conexao(arquivo));
        assertEquals(2, segundaExecucao.listar().size());
        assertEquals("Ana", segundaExecucao.buscarPorId(comum.getId()).orElseThrow().getNome());
        assertEquals("Loja", assertInstanceOf(ContatoComercial.class,
                segundaExecucao.buscarPorId(comercial.getId()).orElseThrow()).getEmpresa());
        try (Connection c = new Conexao(arquivo).abrir()) {
            assertFalse(c.isClosed());
        }
    }

    @Test
    void erroDeBancoMantemCausaTecnicaEMensagemAmigavel() throws Exception {
        try (PreparedStatement comando = ancora.prepareStatement("DROP TABLE contatos")) {
            comando.executeUpdate();
        }
        PersistenciaException erro = assertThrows(PersistenciaException.class, dao::listar);
        assertTrue(erro.getMessage().contains("Não foi possível"));
        assertInstanceOf(java.sql.SQLException.class, erro.getCause());
    }

    @Test
    void conexaoInvalidaPreservaDiagnostico() {
        PersistenciaException erro = assertThrows(PersistenciaException.class,
                () -> new Conexao("jdbc:driver-inexistente:teste").abrir());
        assertNotNull(erro.getCause());
    }
}
