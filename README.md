# MyContacts

Projeto Java Maven desenvolvido para funcionar no console, simulando uma agenda de contatos simples chamada **MyContacts**.

## Objetivo

Permitir o cadastro e o gerenciamento de contatos em memória, usando apenas recursos básicos de Java e Programação Orientada a Objetos.

## Funcionalidades

- Adicionar contato comum
- Adicionar contato comercial
- Listar contatos
- Buscar contato por nome
- Remover contato por nome
- Sair do sistema

## Estrutura do projeto

```text
src/main/java/br/com/igor/mycontacts/
├── app/
│   └── Main.java
├── controller/
│   └── Agenda.java
├── exceptions/
│   └── ContatoNaoEncontradoException.java
├── model/
│   ├── Contato.java
│   └── ContatoComercial.java
└── utils/
    └── ValidadorEmail.java
```

## Conceitos aplicados

- Entrada de dados com `Scanner`
- Saída de dados com `System.out.println`
- Estruturas condicionais com `if/else`
- Controle de menu com `switch`
- Repetição com `while`
- Armazenamento em memória com `ArrayList`
- Busca usando `equalsIgnoreCase`
- Encapsulamento com atributos `private`
- Construtores
- Getters e setters
- Herança com `ContatoComercial extends Contato`
- Exceção personalizada com `ContatoNaoEncontradoException`
- Classe utilitária com `ValidadorEmail`
- Organização em pacotes

## Como executar

### Pré-requisitos

- Java 17 ou superior
- Maven instalado

### Passos

1. Abrir o terminal na pasta do projeto.
2. Compilar o projeto:

```bash
mvn compile
```

3. Executar a aplicação:

```bash
mvn exec:java -Dexec.mainClass="br.com.igor.mycontacts.app.Main"
```

Se preferir, também é possível executar pela IDE rodando a classe `Main`.

## Como o sistema funciona

Ao iniciar, o programa mostra um menu em loop no console. O usuário escolhe uma opção digitando um número. A agenda mantém os contatos em memória com `ArrayList`, sem usar banco de dados. Quando o nome informado não é encontrado na busca ou remoção, o sistema lança uma exceção personalizada e mostra a mensagem no console.

## Resumo rápido

O projeto implementa uma agenda simples no console com foco em praticar fundamentos de Java, especialmente orientação a objetos, coleção `ArrayList`, herança, exceções personalizadas e organização do código em pacotes.
