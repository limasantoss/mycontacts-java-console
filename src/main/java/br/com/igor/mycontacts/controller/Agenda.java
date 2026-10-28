package br.com.igor.mycontacts.controller;

import br.com.igor.mycontacts.dao.Conexao;
import br.com.igor.mycontacts.dao.ContatoDAO;
import br.com.igor.mycontacts.exceptions.ContatoNaoEncontradoException;
import br.com.igor.mycontacts.exceptions.NomeDuplicadoException;
import br.com.igor.mycontacts.model.Contato;
import br.com.igor.mycontacts.repository.Repositorio;
import br.com.igor.mycontacts.utils.Nomes;
import br.com.igor.mycontacts.utils.ValidadorEmail;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/** Serviço compartilhado pelas interfaces: validação e regras da agenda. */
public class Agenda {
    private final Repositorio<Contato> repositorio;

    public Agenda() {
        this(new ContatoDAO(new Conexao()));
    }

    public Agenda(Repositorio<Contato> repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio);
    }

    public Contato salvar(Contato contato, boolean duplicidadeConfirmada) {
        validar(contato);
        Contato anterior = contato.getId() == null ? null : buscarContatoPorId(contato.getId());
        boolean nomeAlterado = anterior == null
                || !Nomes.normalizar(anterior.getNome()).equals(Nomes.normalizar(contato.getNome()));
        if (nomeAlterado && temNomeDuplicado(contato.getNome(), contato.getId()) && !duplicidadeConfirmada) {
            throw new NomeDuplicadoException();
        }
        contato.setNome(contato.getNome().strip());
        contato.setTelefone(contato.getTelefone().strip());
        contato.setEmail(contato.getEmail().strip());
        return repositorio.salvar(contato);
    }

    public void adicionarContato(Contato contato) {
        salvar(contato, false);
    }

    public void validar(Contato contato) {
        if (contato == null || contato.getNome() == null || contato.getNome().isBlank()) {
            throw new IllegalArgumentException("Informe o nome do contato.");
        }
        if (contato.getTelefone() == null || contato.getTelefone().isBlank()) {
            throw new IllegalArgumentException("Informe o telefone do contato.");
        }
        if (contato.getEmail() == null || !ValidadorEmail.validar(contato.getEmail().strip())) {
            throw new IllegalArgumentException("Informe um e-mail válido, como nome@exemplo.com.");
        }
    }

    public boolean temNomeDuplicado(String nome, Long idIgnorado) {
        String normalizado = Nomes.normalizar(nome);
        return !repositorio.buscar(contato -> !Objects.equals(contato.getId(), idIgnorado)
                && Nomes.normalizar(contato.getNome()).equals(normalizado)).isEmpty();
    }

    public List<Contato> buscarContatosPorNome(String nome) {
        String termo = Nomes.normalizar(nome);
        return repositorio.buscar(contato -> Nomes.normalizar(contato.getNome()).contains(termo));
    }

    public Contato buscarContatoPorId(long id) {
        return repositorio.buscarPorId(id).orElseThrow(() ->
                new ContatoNaoEncontradoException("Contato não encontrado com o ID: " + id));
    }

    public void removerContatoPorId(long id) {
        if (!repositorio.remover(id)) {
            throw new ContatoNaoEncontradoException("Contato não encontrado com o ID: " + id);
        }
    }

    public List<Contato> getContatos() {
        return repositorio.listar();
    }

    public void copiarContatosPara(String busca, Collection<? super Contato> destino) {
        if (Nomes.normalizar(busca).isEmpty()) {
            repositorio.copiarPara(destino);
        } else {
            Repositorio.adicionarTodos(buscarContatosPorNome(busca), destino);
        }
    }

    // Métodos da primeira entrega permanecem disponíveis para o console.
    public void listarContatos() {
        List<Contato> contatos = getContatos();
        if (contatos.isEmpty()) {
            System.out.println("Nenhum contato cadastrado.");
        } else {
            contatos.forEach(System.out::println);
        }
    }

    public Contato buscarContatoPorNome(String nome) {
        return repositorio.buscar(contato -> Nomes.normalizar(contato.getNome()).equals(Nomes.normalizar(nome)))
                .stream().findFirst().orElseThrow(() ->
                        new ContatoNaoEncontradoException("Contato não encontrado com o nome: " + nome));
    }

    public void removerContatoPorNome(String nome) {
        List<Contato> encontrados = repositorio.buscar(contato ->
                Nomes.normalizar(contato.getNome()).equals(Nomes.normalizar(nome)));
        if (encontrados.size() > 1) {
            throw new IllegalArgumentException("Há contatos com o mesmo nome. Use o ID para remover.");
        }
        removerContatoPorId(buscarContatoPorNome(nome).getId());
    }
}
