package br.com.igor.mycontacts.exceptions;

public class ContatoNaoEncontradoException extends Exception {

    public ContatoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
