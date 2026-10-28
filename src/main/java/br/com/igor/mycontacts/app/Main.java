package br.com.igor.mycontacts.app;

import br.com.igor.mycontacts.controller.Agenda;
import br.com.igor.mycontacts.exceptions.ContatoNaoEncontradoException;
import br.com.igor.mycontacts.exceptions.NomeDuplicadoException;
import br.com.igor.mycontacts.exceptions.PersistenciaException;
import br.com.igor.mycontacts.model.Contato;
import br.com.igor.mycontacts.model.ContatoComercial;
import br.com.igor.mycontacts.utils.ValidadorEmail;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Agenda agenda = new Agenda();
            System.out.println("=== Bem-vindo ao MyContacts ===");
            boolean executando = true;
            while (executando) {
                exibirMenu();
                if (!scanner.hasNextLine()) {
                    break;
                }
                String opcao = scanner.nextLine().strip();
                try {
                    switch (opcao) {
                        case "1" -> salvarContato(scanner, agenda, null);
                        case "2" -> agenda.listarContatos();
                        case "3" -> {
                            System.out.print("Nome ou parte do nome: ");
                            List<Contato> encontrados = agenda.buscarContatosPorNome(scanner.nextLine());
                            if (encontrados.isEmpty()) {
                                System.out.println("Nenhum contato encontrado.");
                            } else {
                                encontrados.forEach(System.out::println);
                            }
                        }
                        case "4" -> removerContato(scanner, agenda);
                        case "5" -> executando = false;
                        case "6" -> salvarContato(scanner, agenda, lerId(scanner));
                        default -> System.out.println("Opção inválida. Tente novamente.");
                    }
                } catch (PersistenciaException e) {
                    mostrarFalha(e);
                } catch (IllegalArgumentException | ContatoNaoEncontradoException e) {
                    System.out.println(e.getMessage());
                }
            }
            System.out.println("Encerrando o sistema. Até logo!");
        } catch (PersistenciaException e) {
            mostrarFalha(e);
        } catch (NoSuchElementException e) {
            System.out.println("Entrada encerrada. Até logo!");
        }
    }

    private static void exibirMenu() {
        System.out.println("\n1 - Adicionar contato\n2 - Listar contatos\n3 - Buscar contato por nome"
                + "\n4 - Remover contato por ID\n5 - Sair\n6 - Editar contato por ID");
        System.out.print("Escolha uma opção: ");
    }

    private static void salvarContato(Scanner scanner, Agenda agenda, Long id) {
        if (id != null) {
            System.out.println("Editando: " + agenda.buscarContatoPorId(id));
            System.out.println("Informe todos os campos novamente.");
        }
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("Telefone: ");
        String telefone = scanner.nextLine();
        String email = lerEmailValido(scanner);
        System.out.print("O contato é comercial? (s/n): ");
        boolean comercial = scanner.nextLine().strip().equalsIgnoreCase("s");
        Contato contato;
        if (comercial) {
            System.out.print("Empresa (opcional): ");
            contato = new ContatoComercial(nome, telefone, email, scanner.nextLine());
        } else {
            contato = new Contato(nome, telefone, email);
        }
        contato.setId(id);
        try {
            agenda.salvar(contato, false);
        } catch (NomeDuplicadoException e) {
            System.out.print(e.getMessage() + " (s/n): ");
            if (!scanner.nextLine().strip().equalsIgnoreCase("s")) {
                System.out.println("Operação cancelada.");
                return;
            }
            agenda.salvar(contato, true);
        }
        System.out.println("Contato salvo com sucesso. ID: " + contato.getId());
    }

    private static String lerEmailValido(Scanner scanner) {
        while (true) {
            System.out.print("E-mail: ");
            String email = scanner.nextLine().strip();
            if (ValidadorEmail.validar(email)) {
                return email;
            }
            System.out.println("E-mail inválido. Digite novamente (ex.: nome@exemplo.com).");
        }
    }

    private static long lerId(Scanner scanner) {
        System.out.print("ID do contato: ");
        try {
            long id = Long.parseLong(scanner.nextLine().strip());
            if (id > 0) {
                return id;
            }
        } catch (NumberFormatException e) {
            // A mensagem abaixo também atende a entradas que não são números.
        }
        throw new IllegalArgumentException("Informe um ID numérico positivo.");
    }

    private static void removerContato(Scanner scanner, Agenda agenda) {
        long id = lerId(scanner);
        Contato contato = agenda.buscarContatoPorId(id);
        System.out.print("Remover " + contato + "? (s/n): ");
        if (scanner.nextLine().strip().equalsIgnoreCase("s")) {
            agenda.removerContatoPorId(id);
            System.out.println("Contato removido com sucesso.");
        } else {
            System.out.println("Exclusão cancelada.");
        }
    }

    private static void mostrarFalha(PersistenciaException e) {
        System.out.println(e.getMessage());
        LOG.log(Level.SEVERE, "Falha de persistência no console", e);
    }
}
