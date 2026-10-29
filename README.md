# MyContacts

Agenda de contatos pessoais e comerciais com **JavaFX/FXML/CSS**, versão console e persistência **SQLite via JDBC**. Permite cadastrar, listar, buscar, editar e remover por ID, com validação de e-mail e confirmação de duplicidade e exclusão.

## Requisitos e dependências

- **JDK 17 ou superior**, com compilador. O alvo de compilação do projeto é Java 17.
- **Maven 3.9 ou superior**; o Maven integrado ao IntelliJ (**Bundled**) pode ser utilizado.
- Internet no primeiro uso para baixar dependências; sessão gráfica para JavaFX e seus testes.

O [pom.xml](pom.xml) gerencia JavaFX Controls/FXML **17.0.21**, SQLite JDBC **3.53.4.0** e JUnit Jupiter **5.14.2**. Não é necessário instalar separadamente o SDK JavaFX nem um servidor de banco.

## Abrir no IntelliJ IDEA

1. Use **File > Open**, selecione o `pom.xml` e abra como projeto Maven. Aguarde a importação; se necessário, use **Reload All Maven Projects** na janela Maven.
2. Em **File > Project Structure > Project**, selecione um **JDK completo** em **SDK** e o nível de linguagem **17**. Para executar na versão alvo do curso, escolha um JDK 17. Em **Modules > Dependencies**, use o SDK do projeto.
3. Em **Settings > Build, Execution, Deployment > Build Tools > Maven**, selecione **Bundled** ou uma instalação Maven 3.9+. Confira **Importing > JDK for importer** e **Runner > JRE**, usando o JDK do projeto.
4. Confira `src/main/java` como **Sources**, `src/main/resources` como **Resources** e `src/test/java` como **Test Sources**. As bibliotecas JavaFX, SQLite e JUnit devem aparecer em **External Libraries**. Se algo faltar, confira a importação Maven.

O projeto não usa `module-info.java`. O JDK e o Maven devem ser selecionados localmente; as configurações compartilhadas não fixam caminhos de uma máquina.

## Executar e testar no IntelliJ

Selecione uma das quatro configurações compartilhadas em [.run/](.run/) e pressione **Run**:

| Configuração | O que executa |
| --- | --- |
| [MyContacts - JavaFX](.run/MyContacts-JavaFX.run.xml) | Maven `compile javafx:run`; inicia `br.com.igor.mycontacts.app.AgendaApp`. |
| [MyContacts - Console](.run/MyContacts-Console.run.xml) | Application com `br.com.igor.mycontacts.app.Main`; digite as opções na janela **Run**. |
| [MyContacts - Testes](.run/MyContacts-Testes.run.xml) | Maven `test`; 44 testes, sem abrir janelas. |
| [MyContacts - Verificar interface](.run/MyContacts-Verificar-interface.run.xml) | Maven `verify` com o perfil `verificar-interface`; 48 testes, incluindo quatro gráficos, e geração do JAR. |

As configurações usam `$PROJECT_DIR$` como diretório de trabalho e herdam o JDK/Maven selecionado. Se não aparecerem no seletor, confira **Run > Edit Configurations** e a importação do projeto. Os objetivos também podem ser executados pela janela **Maven**.

Para JavaFX, utilize a configuração Maven: o plugin fornece os módulos e as bibliotecas nativas necessários. Para o console, também é possível executar pelo ícone ao lado de `Main.main()`; a opção **5** encerra e a **6** edita por ID.

Os testes podem ser iniciados pelo ícone JUnit ao lado da classe ou do método. `InterfaceIT` sempre exige sessão gráfica; o perfil Maven apenas controla sua inclusão na execução Maven. O perfil gráfico abre e fecha janelas automaticamente.

## Executar pelo terminal

Na pasta do `pom.xml`, com `JAVA_HOME` apontando para o JDK e `java`, `javac` e `mvn` disponíveis no PATH:

```shell
mvn compile javafx:run          # Interface gráfica
mvn compile exec:java           # Console
mvn test                        # 44 testes sem interface
mvn verify                      # Testes sem interface e JAR
mvn -Pverificar-interface verify # 48 testes, incluindo a interface, e JAR
```

O Maven integrado pode funcionar pela IDE mesmo quando `mvn` não está no PATH do terminal. O JAR em `target/` não inclui as dependências; use os comandos ou configurações acima para iniciar a aplicação.

## Banco e regras principais

O banco padrão é **`dados/contatos.db`**, relativo à raiz do projeto nas execuções documentadas. A pasta e a tabela são criadas automaticamente. JavaFX e console compartilham os dados, que permanecem após fechar a aplicação.

Para usar outro banco pelo Maven:

```shell
mvn "-Dmycontacts.db=target/demo.db" compile javafx:run
mvn "-Dmycontacts.db=target/demo.db" compile exec:java
```

Na configuração **Console** do IntelliJ, acrescente `-Dmycontacts.db=target/demo.db` às **VM options**; na configuração **JavaFX**, acrescente a propriedade aos parâmetros Maven. Para backup, feche a aplicação e copie o banco. Os bancos e arquivos auxiliares ficam fora do Git.

Nome, telefone e e-mail são obrigatórios; empresa é opcional para contatos comerciais. Telefone é armazenado como texto. Nomes repetidos podem ser salvos após confirmação. A busca ignora caixa, preservando acentos; edição e remoção usam o ID para distinguir nomes iguais.

## Testes e verificações

Em **29/10/2026**, os **48 testes** passaram pelo Maven do **IntelliJ IDEA 2026.2.3**, com **0 falhas, 0 erros e 0 ignorados**, usando **JBR 25.0.4** e **Maven 3.9.16**. Os 44 testes sem interface passaram também pelo terminal; os quatro gráficos passaram separadamente pelo runner JUnit da IDE. O build/rebuild e a abertura/fechamento da aplicação JavaFX foram verificados na IDE.

O CRUD do console foi verificado pelo terminal com banco isolado e reabertura em novos processos. Na IDE, o runner Application foi verificado com listagem e saída por entrada redirecionada. Os testes DAO usam SQLite em memória, com isolamento por teste; há também teste de persistência em arquivo.

**A execução local foi no JBR 25.0.4. O alvo de compilação Java 17 não comprova execução em uma JVM 17**, que permanece pendente nesta revisão local. Evidências, comandos, avisos e comparação com o PDF estão no [relatório da revisão](docs/revisao-intellij.md). Os relatórios JUnit ficam em `target/surefire-reports/`.

## FXML e Scene Builder

As telas estão em `src/main/resources/br/com/igor/mycontacts/view/`, com CSS relativo e controllers separados. O Scene Builder é opcional para executar o projeto. O [registro de 28/10/2026](docs/verificacao-segunda-entrega.md) contém capturas da inspeção visual; as limitações dessa evidência estão no [relatório da revisão](docs/revisao-intellij.md).

## Entrega das fontes

Inclua `src/`, `pom.xml`, `README.md`, `.gitignore`, `.run/` e `docs/`. Exclua bancos, `.git/`, `.idea/`, `target/`, `out/`, caches, logs e ferramentas locais. O ZIP local da entrega anterior foi preservado e não contém os ajustes desta revisão.
