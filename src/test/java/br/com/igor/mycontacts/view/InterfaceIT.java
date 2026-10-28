package br.com.igor.mycontacts.view;

import br.com.igor.mycontacts.BancoEmMemoriaTest;
import br.com.igor.mycontacts.app.AgendaApp;
import br.com.igor.mycontacts.controller.AgendaController;
import br.com.igor.mycontacts.controller.ContatoFormController;
import br.com.igor.mycontacts.model.Contato;
import br.com.igor.mycontacts.model.ContatoComercial;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Execute explicitamente com mvn -Pverificar-interface test em uma sessão gráfica. */
class InterfaceIT extends BancoEmMemoriaTest {
    @BeforeAll
    static void iniciarJavaFX() throws Exception {
        CountDownLatch iniciado = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            iniciado.countDown();
        });
        assertTrue(iniciado.await(20, TimeUnit.SECONDS), "O JavaFX não iniciou no prazo.");
    }

    @AfterAll
    static void finalizarJavaFX() {
        Platform.exit();
    }

    private void naInterface(Acao acao) throws Exception {
        FutureTask<Void> tarefa = new FutureTask<>(() -> {
            acao.executar();
            return null;
        });
        Platform.runLater(tarefa);
        tarefa.get(20, TimeUnit.SECONDS);
    }

    @FunctionalInterface
    private interface Acao {
        void executar() throws Exception;
    }

    private FXMLLoader carregar(String nome) {
        return new FXMLLoader(AgendaApp.class.getResource(AgendaApp.RECURSOS + "view/" + nome));
    }

    private Stage mostrar(Parent raiz) {
        Stage stage = new Stage();
        stage.setScene(new Scene(raiz));
        stage.show();
        raiz.applyCss();
        raiz.layout();
        return stage;
    }

    private Button botao(Parent raiz, String texto) {
        return raiz.lookupAll(".button").stream().filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> texto.equals(b.getText())).findFirst().orElseThrow();
    }

    private void preencher(Parent raiz, String campo, String valor) {
        ((TextField) raiz.lookup("#" + campo)).setText(valor);
    }

    private void salvarImagem(Parent raiz, String nome) throws Exception {
        WritableImage imagem = raiz.snapshot(null, null);
        int largura = (int) imagem.getWidth();
        int altura = (int) imagem.getHeight();
        int[] pixels = new int[largura * altura];
        imagem.getPixelReader().getPixels(0, 0, largura, altura, PixelFormat.getIntArgbInstance(), pixels, 0, largura);
        BufferedImage png = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        png.setRGB(0, 0, largura, altura, pixels, 0, largura);
        Path pasta = Files.createDirectories(Path.of("target/verificacao-ui"));
        ImageIO.write(png, "png", pasta.resolve(nome).toFile());
    }

    private void responderDialogo(String resposta) {
        // O evento é processado pelo loop modal de Alert.showAndWait().
        Platform.runLater(() -> {
            Window janela = Window.getWindows().stream()
                    .filter(w -> w.isShowing() && w.getScene().getRoot() instanceof DialogPane)
                    .findFirst().orElseThrow();
            DialogPane painel = (DialogPane) janela.getScene().getRoot();
            painel.getButtonTypes().stream().filter(tipo -> resposta.equals(tipo.getText())).findFirst()
                    .map(painel::lookupButton).map(Button.class::cast).orElseThrow().fire();
        });
    }

    @Test
    void carregaTabelaCssBuscaESelecao() throws Exception {
        agenda.adicionarContato(new Contato("Ana", "85", "a@exemplo.com"));
        agenda.adicionarContato(new Contato("Bia", "11", "b@exemplo.com"));
        naInterface(() -> {
            FXMLLoader loader = carregar("agenda.fxml");
            Parent raiz = loader.load();
            Stage stage = mostrar(raiz);
            try {
                ((AgendaController) loader.getController()).configurar(agenda);
                TableView<?> tabela = (TableView<?>) raiz.lookup("#contatosTable");
                assertEquals(2, tabela.getItems().size());
                assertTrue(botao(raiz, "Editar").isDisabled());
                tabela.getSelectionModel().select(0);
                assertFalse(botao(raiz, "Editar").isDisabled());
                preencher(raiz, "buscaField", "bia");
                botao(raiz, "Buscar").fire();
                assertEquals(1, tabela.getItems().size());
                assertEquals("Bia", ((Contato) tabela.getItems().get(0)).getNome());
                botao(raiz, "Limpar").fire();
                assertEquals(2, tabela.getItems().size());
                assertFalse(raiz.getStylesheets().isEmpty());
                assertNotNull(AgendaApp.class.getResource(AgendaApp.RECURSOS + "css/agenda.css"));
                salvarImagem(raiz, "agenda.png");
            } finally {
                stage.close();
            }
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void formularioValidaECadastraComercialPeloEventoDoBotao() throws Exception {
        naInterface(() -> {
            FXMLLoader loader = carregar("contato-form.fxml");
            Parent raiz = loader.load();
            ContatoFormController controller = loader.getController();
            controller.configurar(agenda, null);
            Stage stage = mostrar(raiz);
            try {
                botao(raiz, "Salvar contato").fire();
                assertTrue(((Label) raiz.lookup("#erroLabel")).isVisible());
                assertFalse(controller.isSalvo());
                assertTrue(agenda.getContatos().isEmpty());
                preencher(raiz, "nomeField", "Ana");
                preencher(raiz, "telefoneField", "85");
                preencher(raiz, "emailField", "email-invalido");
                botao(raiz, "Salvar contato").fire();
                assertTrue(((Label) raiz.lookup("#erroLabel")).getText().contains("e-mail válido"));
                raiz.layout();
                Button salvar = botao(raiz, "Salvar contato");
                assertTrue(salvar.localToScene(salvar.getBoundsInLocal()).getMaxY() <= stage.getScene().getHeight(),
                        "A mensagem de erro não deve empurrar o botão Salvar para fora da janela.");
                preencher(raiz, "emailField", "a@exemplo.com");
                ((ComboBox<String>) raiz.lookup("#tipoCombo")).setValue("Comercial");
                assertFalse(raiz.lookup("#empresaField").isDisabled());
                preencher(raiz, "empresaField", "Loja");
                salvarImagem(raiz, "formulario.png");
                botao(raiz, "Salvar contato").fire();
                assertTrue(controller.isSalvo());
                assertFalse(stage.isShowing());
                assertEquals("Loja", assertInstanceOf(ContatoComercial.class, agenda.getContatos().get(0)).getEmpresa());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    void formularioEditaPorIdECancelarPreservaDados() throws Exception {
        Contato original = agenda.salvar(new ContatoComercial("Ana", "85", "a@exemplo.com", "Loja"), false);
        naInterface(() -> {
            FXMLLoader loader = carregar("contato-form.fxml");
            Parent raiz = loader.load();
            ContatoFormController controller = loader.getController();
            controller.configurar(agenda, original);
            Stage stage = mostrar(raiz);
            try {
                assertEquals("Loja", ((TextField) raiz.lookup("#empresaField")).getText());
                preencher(raiz, "telefoneField", "11");
                botao(raiz, "Salvar contato").fire();
                assertTrue(controller.isSalvo());
                assertEquals("11", agenda.buscarContatoPorId(original.getId()).getTelefone());
                assertEquals(1, agenda.getContatos().size());
            } finally {
                stage.close();
            }
            FXMLLoader cancelarLoader = carregar("contato-form.fxml");
            Parent cancelarRaiz = cancelarLoader.load();
            ((ContatoFormController) cancelarLoader.getController()).configurar(agenda, original);
            Stage cancelarStage = mostrar(cancelarRaiz);
            try {
                preencher(cancelarRaiz, "nomeField", "Não salvar");
                botao(cancelarRaiz, "Cancelar").fire();
                assertFalse(((ContatoFormController) cancelarLoader.getController()).isSalvo());
                assertEquals("Ana", agenda.buscarContatoPorId(original.getId()).getNome());
                assertEquals("Ana", original.getNome());
            } finally {
                cancelarStage.close();
            }
        });
    }

    @Test
    void confirmacoesPermitemCancelarOuSalvarDuplicadoERemoverPorId() throws Exception {
        Contato primeiro = agenda.salvar(new Contato("Ana", "85", "a@exemplo.com"), false);
        naInterface(() -> {
            FXMLLoader loader = carregar("contato-form.fxml");
            Parent raiz = loader.load();
            ContatoFormController controller = loader.getController();
            controller.configurar(agenda, null);
            Stage stage = mostrar(raiz);
            try {
                preencher(raiz, "nomeField", " ANA ");
                preencher(raiz, "telefoneField", "11");
                preencher(raiz, "emailField", "b@exemplo.com");
                responderDialogo("Cancelar");
                botao(raiz, "Salvar contato").fire();
                assertFalse(controller.isSalvo());
                assertEquals(1, agenda.getContatos().size());
                responderDialogo("Confirmar");
                botao(raiz, "Salvar contato").fire();
                assertTrue(controller.isSalvo());
                assertEquals(2, agenda.getContatos().size());
            } finally {
                stage.close();
            }
            FXMLLoader agendaLoader = carregar("agenda.fxml");
            Parent agendaRaiz = agendaLoader.load();
            Stage agendaStage = mostrar(agendaRaiz);
            try {
                ((AgendaController) agendaLoader.getController()).configurar(agenda);
                TableView<?> tabela = (TableView<?>) agendaRaiz.lookup("#contatosTable");
                for (int i = 0; i < tabela.getItems().size(); i++) {
                    if (!primeiro.getId().equals(((Contato) tabela.getItems().get(i)).getId())) {
                        tabela.getSelectionModel().select(i);
                        break;
                    }
                }
                responderDialogo("Cancelar");
                botao(agendaRaiz, "Remover").fire();
                assertEquals(2, agenda.getContatos().size());
                responderDialogo("Confirmar");
                botao(agendaRaiz, "Remover").fire();
                assertEquals(1, agenda.getContatos().size());
                assertTrue(dao.buscarPorId(primeiro.getId()).isPresent());
            } finally {
                agendaStage.close();
            }
        });
    }
}
