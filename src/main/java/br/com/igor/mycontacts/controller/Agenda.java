package br.com.igor.mycontacts.controller;

import br.com.igor.mycontacts.exceptions.ContatoNaoEncontradoException;
import br.com.igor.mycontacts.model.Contato;
import java.util.ArrayList;
import java.util.List;

public class Agenda {

    private List<Contato> contatos;

    public Agenda() {
        this.contatos = new ArrayList<>();
    }

    public void adicionarContato(Contato contato) {
        contatos.add(contato);
    }

    public void listarContatos() {
        if (contatos.isEmpty()) {
            System.out.println("Nenhum contato cadastrado.");
            return;
        }

        for (Contato contato : contatos) {
            System.out.println(contato);
        }
    }

    public Contato buscarContatoPorNome(String nome) throws ContatoNaoEncontradoException {
        for (Contato contato : contatos) {
            if (contato.getNome().equalsIgnoreCase(nome)) {
                return contato;
            }
        }

        throw new ContatoNaoEncontradoException("Contato nao encontrado com o nome: " + nome);
    }

    public void removerContatoPorNome(String nome) throws ContatoNaoEncontradoException {
        Contato contato = buscarContatoPorNome(nome);
        contatos.remove(contato);
    }

    public List<Contato> getContatos() {
        return contatos;
    }

    public void setContatos(List<Contato> contatos) {
        this.contatos = contatos;
    }
}
