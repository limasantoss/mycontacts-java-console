package br.com.igor.mycontacts.repository;

import br.com.igor.mycontacts.model.Identificavel;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface Repositorio<T extends Identificavel> {
    T salvar(T entidade);
    List<T> listar();
    Optional<T> buscarPorId(long id);
    boolean remover(long id);

    default List<T> buscar(Predicate<? super T> filtro) {
        return listar().stream().filter(filtro).toList();
    }

    default void copiarPara(Collection<? super T> destino) {
        // O destino consome T: pode ser uma coleção de T ou de um supertipo.
        destino.addAll(listar());
    }

    static <T> void adicionarTodos(Collection<? extends T> origem, Collection<? super T> destino) {
        // A origem produz T: aceita também coleções de subtipos, como ContatoComercial.
        destino.addAll(origem);
    }

    static int quantidade(Collection<?> itens) {
        // O tipo é desconhecido: contar elementos não exige conhecer seu conteúdo.
        return itens.size();
    }
}
