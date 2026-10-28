package br.com.igor.mycontacts.exceptions;

public class ContatoNaoEncontradoException extends RuntimeException {

    public ContatoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
