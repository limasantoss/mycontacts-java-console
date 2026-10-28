package br.com.igor.mycontacts.exceptions;

public class NomeDuplicadoException extends RuntimeException {
    public NomeDuplicadoException() {
        super("Já existe outro contato com esse nome. Deseja salvar mesmo assim?");
    }
}
