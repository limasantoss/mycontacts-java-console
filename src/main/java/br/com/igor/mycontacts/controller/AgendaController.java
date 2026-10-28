package br.com.igor.mycontacts.controller;

import br.com.igor.mycontacts.app.AgendaApp;
import br.com.igor.mycontacts.exceptions.ContatoNaoEncontradoException;
import br.com.igor.mycontacts.exceptions.PersistenciaException;
import br.com.igor.mycontacts.model.Contato;
import br.com.igor.mycontacts.model.ContatoComercial;
import br.com.igor.mycontacts.repository.Repositorio;
import br.com.igor.mycontacts.view.Mensagens;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AgendaController {
    @FXML private TextField buscaField;
    @FXML private TableView<Contato> contatosTable;
    @FXML private TableColumn<Contato, Long> idColumn;
    @FXML private TableColumn<Contato, String> nomeColumn;
    @FXML private TableColumn<Contato, String> telefoneColumn;
    @FXML private TableColumn<Contato, String> emailColumn;
    @FXML private TableColumn<Contato, String> tipoColumn;
    @FXML private TableColumn<Contato, String> empresaColumn;
    @FXML private Button editarButton;
    @FXML private Button removerButton;
    @FXML private Label statusLabel;

    private final ObservableList<Contato> contatos = FXCollections.observableArrayList();
    private Agenda agenda;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        nomeColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getNome()));
        telefoneColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getTelefone()));
        emailColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getEmail()));
        tipoColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(
                c.getValue() instanceof ContatoComercial ? "Comercial" : "Comum"));
        empresaColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(
                c.getValue() instanceof ContatoComercial comercial ? comercial.getEmpresa() : ""));
        contatosTable.setItems(contatos);
        contatosTable.setPlaceholder(new Label("Nenhum contato encontrado. Cadastre um contato ou ajuste a busca."));
        editarButton.disableProperty().bind(contatosTable.getSelectionModel().selectedItemProperty().isNull());
        removerButton.disableProperty().bind(contatosTable.getSelectionModel().selectedItemProperty().isNull());
    }

    public void configurar(Agenda agenda) {
        this.agenda = agenda;
        atualizar();
    }

    @FXML
    private void buscar() {
        atualizar();
    }

    @FXML
    private void limparBusca() {
        buscaField.clear();
        atualizar();
    }

    @FXML
    private void cadastrar() {
        abrirFormulario(null);
    }

    @FXML
    private void editar() {
        Contato selecionado = contatosTable.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            abrirFormulario(selecionado);
        }
    }

    @FXML
    private void remover() {
        Contato contato = contatosTable.getSelectionModel().getSelectedItem();
        if (contato == null || !Mensagens.confirmar(contatosTable.getScene().getWindow(), "Remover contato",
                "Remover o contato " + contato.getNome() + " (ID " + contato.getId() + ")?")) {
            return;
        }
        try {
            agenda.removerContatoPorId(contato.getId());
            atualizar();
            statusLabel.setText("Contato removido. " + Repositorio.quantidade(contatos) + " contato(s) na lista.");
        } catch (PersistenciaException | ContatoNaoEncontradoException e) {
            Mensagens.erro(contatosTable.getScene().getWindow(), "Não foi possível remover", e.getMessage(), e);
        }
    }

    @FXML
    private void atualizar() {
        if (agenda == null) {
            return;
        }
        try {
            // Só substitui a tabela depois da consulta: uma falha não apaga a lista visível.
            List<Contato> encontrados = new ArrayList<>();
            agenda.copiarContatosPara(buscaField.getText(), encontrados);
            contatos.setAll(encontrados);
            statusLabel.setText(Repositorio.quantidade(contatos) + " contato(s) na lista.");
        } catch (PersistenciaException e) {
            statusLabel.setText("Não foi possível atualizar. Use Atualizar para tentar novamente.");
            Mensagens.erro(contatosTable.getScene() == null ? null : contatosTable.getScene().getWindow(),
                    "Falha ao carregar contatos", e.getMessage(), e);
        }
    }

    private void abrirFormulario(Contato contato) {
        try {
            FXMLLoader loader = new FXMLLoader(AgendaApp.class.getResource(AgendaApp.RECURSOS + "view/contato-form.fxml"));
            Parent raiz = loader.load();
            ContatoFormController controller = loader.getController();
            controller.configurar(agenda, contato);
            Stage dialogo = new Stage();
            dialogo.initOwner(contatosTable.getScene().getWindow());
            dialogo.initModality(Modality.WINDOW_MODAL);
            dialogo.setTitle(contato == null ? "Cadastrar contato" : "Editar contato • ID " + contato.getId());
            dialogo.setScene(new Scene(raiz));
            dialogo.setResizable(false);
            dialogo.showAndWait();
            if (controller.isSalvo()) {
                atualizar();
            }
        } catch (IOException e) {
            Mensagens.erro(contatosTable.getScene().getWindow(), "Falha ao abrir formulário",
                    "Não foi possível carregar o formulário. Consulte os detalhes no terminal.", e);
        }
    }
}
