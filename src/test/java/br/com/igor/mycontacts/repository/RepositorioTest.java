package br.com.igor.mycontacts.repository;

import br.com.igor.mycontacts.BancoEmMemoriaTest;
import br.com.igor.mycontacts.model.Contato;
import br.com.igor.mycontacts.model.ContatoComercial;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RepositorioTest extends BancoEmMemoriaTest {
    @Test
    void contratoGenericoExecutaCrudNoDaoReal() {
        Repositorio<Contato> repositorio = dao;
        Contato contato = repositorio.salvar(new Contato("Ana", "85", "a@exemplo.com"));
        assertTrue(repositorio.buscarPorId(contato.getId()).isPresent());
        contato.setNome("Bia");
        repositorio.salvar(contato);
        assertEquals("Bia", repositorio.listar().get(0).getNome());
        assertTrue(repositorio.remover(contato.getId()));
        assertTrue(repositorio.listar().isEmpty());
    }

    @Test
    void extendsAceitaOrigemDeSubtipoESuperAceitaDestinoAmplo() {
        List<ContatoComercial> comerciais = List.of(new ContatoComercial("Ana", "85", "a@exemplo.com", "Loja"));
        List<Contato> contatos = new ArrayList<>();
        Repositorio.adicionarTodos(comerciais, contatos);
        contatos.forEach(dao::salvar);
        List<Object> destino = new ArrayList<>();
        dao.copiarPara(destino);
        assertEquals(1, Repositorio.quantidade(destino));
        assertInstanceOf(ContatoComercial.class, destino.get(0));
        assertEquals("Loja", ((ContatoComercial) destino.get(0)).getEmpresa());
    }

    @Test
    void filtroAceitaPredicadoDeSupertipo() {
        dao.salvar(new Contato("Ana", "85", "a@exemplo.com"));
        Predicate<Object> naoNulo = objeto -> objeto != null;
        assertEquals(1, dao.buscar(naoNulo).size());
        assertEquals(0, Repositorio.quantidade(List.of()));
        assertEquals(2, Repositorio.quantidade(List.of("texto", "outro")));
    }
}
