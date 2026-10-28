package br.com.igor.mycontacts.view;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

public final class Mensagens {
    private static final Logger LOG = Logger.getLogger(Mensagens.class.getName());

    private Mensagens() {
    }

    public static boolean confirmar(Window dono, String titulo, String mensagem) {
        Alert alerta = criar(dono, Alert.AlertType.CONFIRMATION, titulo, mensagem);
        ButtonType confirmar = new ButtonType("Confirmar");
        ButtonType cancelar = new ButtonType("Cancelar", ButtonType.CANCEL.getButtonData());
        alerta.getButtonTypes().setAll(confirmar, cancelar);
        return alerta.showAndWait().filter(confirmar::equals).isPresent();
    }

    public static void erro(Window dono, String titulo, String mensagem, Throwable causa) {
        LOG.log(Level.SEVERE, titulo, causa);
        criar(dono, Alert.AlertType.ERROR, titulo, mensagem).showAndWait();
    }

    private static Alert criar(Window dono, Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        if (dono != null) {
            alerta.initOwner(dono);
        }
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        return alerta;
    }
}
