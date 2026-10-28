package br.com.igor.mycontacts.controller;

import br.com.igor.mycontacts.BancoEmMemoriaTest;
import br.com.igor.mycontacts.exceptions.ContatoNaoEncontradoException;
import br.com.igor.mycontacts.exceptions.NomeDuplicadoException;
import br.com.igor.mycontacts.model.Contato;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AgendaTest extends BancoEmMemoriaTest {
    private Contato contato(String nome) {
        return new Contato(nome, "85", "contato@exemplo.com");
    }

    @Test
    void cadastraBuscaEditaERemove() {
        Contato contato = contato(" Ana ");
        agenda.adicionarContato(contato);
        assertEquals("Ana", agenda.buscarContatoPorId(contato.getId()).getNome());
        assertEquals(1, agenda.buscarContatosPorNome(" AN ").size());
        contato.setTelefone("11");
        agenda.salvar(contato, false);
        assertEquals("11", agenda.buscarContatoPorId(contato.getId()).getTelefone());
        agenda.removerContatoPorId(contato.getId());
        assertTrue(agenda.getContatos().isEmpty());
        assertThrows(ContatoNaoEncontradoException.class, () -> agenda.buscarContatoPorId(contato.getId()));
        assertThrows(ContatoNaoEncontradoException.class, () -> agenda.removerContatoPorId(contato.getId()));
    }

    @Test
    void avisaDuplicidadeIgnorandoCaixaEEspacosSemSalvarAteConfirmar() {
        agenda.adicionarContato(contato("Ágata"));
        Contato duplicado = contato("  ÁGATA  ");
        assertTrue(agenda.temNomeDuplicado(" ágata ", null));
        assertThrows(NomeDuplicadoException.class, () -> agenda.salvar(duplicado, false));
        assertNull(duplicado.getId());
        assertEquals(1, agenda.getContatos().size());
        agenda.salvar(duplicado, true);
        assertEquals(2, agenda.getContatos().size());
    }

    @Test
    void edicaoIgnoraProprioId() {
        Contato ana = agenda.salvar(contato("Ana"), false);
        assertFalse(agenda.temNomeDuplicado(" ANA ", ana.getId()));
        ana.setNome(" ANA ");
        assertDoesNotThrow(() -> agenda.salvar(ana, false));
        assertEquals(1, agenda.getContatos().size());
    }

    @Test
    void edicaoDetectaOutroContatoEAceitaConfirmacao() {
        Contato ana = agenda.salvar(contato("Ana"), false);
        Contato bia = agenda.salvar(contato("Bia"), false);
        bia.setNome(" ANA ");
        assertTrue(agenda.temNomeDuplicado(bia.getNome(), bia.getId()));
        assertThrows(NomeDuplicadoException.class, () -> agenda.salvar(bia, false));
        assertEquals("Bia", agenda.buscarContatoPorId(bia.getId()).getNome());
        agenda.salvar(bia, true);
        assertEquals("ANA", agenda.buscarContatoPorId(bia.getId()).getNome());
        assertEquals("Ana", agenda.buscarContatoPorId(ana.getId()).getNome());
    }

    @Test
    void naoConsideraNomeParcialComoDuplicado() {
        agenda.adicionarContato(contato("Ana Maria"));
        assertFalse(agenda.temNomeDuplicado("Ana", null));
        assertDoesNotThrow(() -> agenda.adicionarContato(contato("Ana")));
    }

    @Test
    void editaTelefoneDeDuplicadoJaConfirmadoSemPedirNovaConfirmacao() {
        agenda.salvar(contato("Ana"), false);
        Contato duplicado = agenda.salvar(contato(" ANA "), true);
        duplicado.setTelefone("11");
        assertDoesNotThrow(() -> agenda.salvar(duplicado, false));
        assertEquals("11", agenda.buscarContatoPorId(duplicado.getId()).getTelefone());
    }

    @Test
    void validaCamposAntesDeGravarMesmoComConfirmacao() {
        assertThrows(IllegalArgumentException.class, () -> agenda.salvar(contato(" "), true));
        assertThrows(IllegalArgumentException.class, () -> agenda.salvar(null, true));
        assertThrows(IllegalArgumentException.class,
                () -> agenda.salvar(new Contato("Ana", "", "a@exemplo.com"), true));
        assertThrows(IllegalArgumentException.class,
                () -> agenda.salvar(new Contato("Ana", "85", "invalido"), true));
        assertTrue(agenda.getContatos().isEmpty());
    }

    @Test
    void normalizaEspacosDosCamposObrigatorios() {
        Contato contato = agenda.salvar(new Contato(" Ana ", " 85 ", " a@exemplo.com "), false);
        Contato salvo = agenda.buscarContatoPorId(contato.getId());
        assertEquals("Ana", salvo.getNome());
        assertEquals("85", salvo.getTelefone());
        assertEquals("a@exemplo.com", salvo.getEmail());
    }

    @Test
    void preencheDestinoGenericoComESemFiltro() {
        agenda.adicionarContato(contato("Ana"));
        agenda.adicionarContato(contato("Bia"));
        List<Object> todos = new ArrayList<>();
        agenda.copiarContatosPara("", todos);
        assertEquals(2, todos.size());
        List<Contato> filtrados = new ArrayList<>();
        agenda.copiarContatosPara("bia", filtrados);
        assertEquals("Bia", filtrados.get(0).getNome());
    }

    @Test
    void metodosAnterioresContinuamBuscandoSemRemoverNomeAmbiguo() {
        agenda.salvar(contato("Ana"), false);
        agenda.salvar(contato("Ana"), true);
        assertEquals("Ana", agenda.buscarContatoPorNome(" ANA ").getNome());
        assertThrows(IllegalArgumentException.class, () -> agenda.removerContatoPorNome("Ana"));
        assertEquals(2, agenda.getContatos().size());
        assertThrows(ContatoNaoEncontradoException.class, () -> agenda.buscarContatoPorNome("Inexistente"));
    }
}
