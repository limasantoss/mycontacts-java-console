package br.com.igor.mycontacts.app;

import br.com.igor.mycontacts.controller.Agenda;
import br.com.igor.mycontacts.controller.AgendaController;
import br.com.igor.mycontacts.view.Mensagens;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AgendaApp extends Application {
    public static final String RECURSOS = "/br/com/igor/mycontacts/";

    @Override
    public void start(Stage stage) {
        try {
            Agenda agenda = new Agenda();
            FXMLLoader loader = new FXMLLoader(AgendaApp.class.getResource(RECURSOS + "view/agenda.fxml"));
            Parent raiz = loader.load();
            AgendaController controller = loader.getController();
            controller.configurar(agenda);
            stage.setTitle("MyContacts • Agenda de contatos");
            stage.setScene(new Scene(raiz));
            stage.setMinWidth(800);
            stage.setMinHeight(500);
            stage.show();
        } catch (IOException | RuntimeException e) {
            Mensagens.erro(null, "Não foi possível iniciar a agenda",
                    "A agenda não pôde ser aberta. Verifique os recursos da aplicação e o acesso ao banco. "
                    + "Os detalhes estão no terminal.", e);
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
