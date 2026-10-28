package br.com.igor.mycontacts.controller;

import br.com.igor.mycontacts.exceptions.ContatoNaoEncontradoException;
import br.com.igor.mycontacts.exceptions.NomeDuplicadoException;
import br.com.igor.mycontacts.exceptions.PersistenciaException;
import br.com.igor.mycontacts.model.Contato;
import br.com.igor.mycontacts.model.ContatoComercial;
import br.com.igor.mycontacts.view.Mensagens;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ContatoFormController {
    @FXML private Label tituloLabel;
    @FXML private Label erroLabel;
    @FXML private TextField nomeField;
    @FXML private TextField telefoneField;
    @FXML private TextField emailField;
    @FXML private TextField empresaField;
    @FXML private ComboBox<String> tipoCombo;

    private Agenda agenda;
    private Long id;
    private boolean salvo;

    @FXML
    private void initialize() {
        tipoCombo.getItems().setAll("Comum", "Comercial");
        tipoCombo.setValue("Comum");
        empresaField.disableProperty().bind(tipoCombo.valueProperty().isNotEqualTo("Comercial"));
        erroLabel.setVisible(false);
        // A área de erro fica reservada no FXML para não deslocar os botões.
        for (TextField campo : List.of(nomeField, telefoneField, emailField, empresaField)) {
            campo.textProperty().addListener((observavel, anterior, atual) -> erroLabel.setVisible(false));
        }
    }

    public void configurar(Agenda agenda, Contato contato) {
        this.agenda = agenda;
        if (contato != null) {
            id = contato.getId();
            tituloLabel.setText("Editar contato");
            nomeField.setText(contato.getNome());
            telefoneField.setText(contato.getTelefone());
            emailField.setText(contato.getEmail());
            if (contato instanceof ContatoComercial comercial) {
                tipoCombo.setValue("Comercial");
                empresaField.setText(comercial.getEmpresa());
            }
        }
    }

    @FXML
    private void salvar() {
        // Uma nova instância evita alterar a linha da tabela se a edição for cancelada.
        Contato contato = "Comercial".equals(tipoCombo.getValue())
                ? new ContatoComercial(nomeField.getText(), telefoneField.getText(), emailField.getText(), empresaField.getText())
                : new Contato(nomeField.getText(), telefoneField.getText(), emailField.getText());
        contato.setId(id);
        try {
            try {
                agenda.salvar(contato, false);
            } catch (NomeDuplicadoException e) {
                if (!Mensagens.confirmar(nomeField.getScene().getWindow(), "Nome já cadastrado", e.getMessage())) {
                    return;
                }
                agenda.salvar(contato, true);
            }
            salvo = true;
            fechar();
        } catch (IllegalArgumentException | ContatoNaoEncontradoException e) {
            erroLabel.setText(e.getMessage());
            erroLabel.setVisible(true);
        } catch (PersistenciaException e) {
            Mensagens.erro(nomeField.getScene().getWindow(), "Não foi possível salvar", e.getMessage(), e);
        }
    }

    @FXML
    private void cancelar() {
        fechar();
    }

    private void fechar() {
        ((Stage) nomeField.getScene().getWindow()).close();
    }

    public boolean isSalvo() {
        return salvo;
    }
}
