package br.com.igor.mycontacts.app;

import br.com.igor.mycontacts.controller.Agenda;
import br.com.igor.mycontacts.exceptions.ContatoNaoEncontradoException;
import br.com.igor.mycontacts.model.Contato;
import br.com.igor.mycontacts.model.ContatoComercial;
import br.com.igor.mycontacts.utils.ValidadorEmail;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Agenda agenda = new Agenda();
        boolean executando = true;

        System.out.println("=== Bem-vindo ao MyContacts ===");

        while (executando) {
            exibirMenu();
            String opcao = scanner.nextLine();

            switch (opcao) {
                case "1":
                    adicionarContato(scanner, agenda);
                    break;
                case "2":
                    listarContatos(agenda);
                    break;
                case "3":
                    buscarContato(scanner, agenda);
                    break;
                case "4":
                    removerContato(scanner, agenda);
                    break;
                case "5":
                    executando = false;
                    System.out.println("Encerrando o sistema. Ate logo!");
                    break;
                default:
                    System.out.println("Opcao invalida. Tente novamente.");
                    break;
            }
        }

        scanner.close();
    }

    private static void exibirMenu() {
        System.out.println();
        System.out.println("1 - Adicionar contato");
        System.out.println("2 - Listar contatos");
        System.out.println("3 - Buscar contato por nome");
        System.out.println("4 - Remover contato");
        System.out.println("5 - Sair");
        System.out.print("Escolha uma opcao: ");
    }

    private static void adicionarContato(Scanner scanner, Agenda agenda) {
        System.out.print("Nome: ");
        String nome = scanner.nextLine();

        System.out.print("Telefone: ");
        String telefone = scanner.nextLine();

        String email = lerEmailValido(scanner);

        System.out.print("O contato e comercial? (s/n): ");
        String resposta = scanner.nextLine();

        Contato contato;

        if (resposta.equalsIgnoreCase("s")) {
            System.out.print("Empresa: ");
            String empresa = scanner.nextLine();
            contato = new ContatoComercial(nome, telefone, email, empresa);
        } else {
            contato = new Contato(nome, telefone, email);
        }

        agenda.adicionarContato(contato);
        System.out.println("Contato adicionado com sucesso.");
    }

    private static String lerEmailValido(Scanner scanner) {
        String email;

        while (true) {
            System.out.print("Email: ");
            email = scanner.nextLine();

            if (ValidadorEmail.validar(email)) {
                return email;
            } else {
                System.out.println("Email invalido. Digite novamente.");
            }
        }
    }

    private static void listarContatos(Agenda agenda) {
        System.out.println();
        System.out.println("=== Lista de Contatos ===");
        agenda.listarContatos();
    }

    private static void buscarContato(Scanner scanner, Agenda agenda) {
        System.out.print("Digite o nome do contato: ");
        String nome = scanner.nextLine();

        try {
            Contato contato = agenda.buscarContatoPorNome(nome);
            System.out.println("Contato encontrado:");
            System.out.println(contato);
        } catch (ContatoNaoEncontradoException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void removerContato(Scanner scanner, Agenda agenda) {
        System.out.print("Digite o nome do contato a remover: ");
        String nome = scanner.nextLine();

        try {
            agenda.removerContatoPorNome(nome);
            System.out.println("Contato removido com sucesso.");
        } catch (ContatoNaoEncontradoException e) {
            System.out.println(e.getMessage());
        }
    }
}
