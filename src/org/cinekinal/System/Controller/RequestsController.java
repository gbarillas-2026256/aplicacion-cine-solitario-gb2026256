package org.cinekinal.system.controller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import org.cinekinal.system.model.Employee;
import org.cinekinal.system.model.Request;
import org.cinekinal.system.service.RequestService;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

/**
 * Controller for managing pending employee requests by owner.
 */
public class RequestsController implements Initializable {

    @FXML
    private TableView<Request> tableRequests;
    @FXML
    private TableColumn<Request, String> colRequester;
    @FXML
    private TableColumn<Request, String> colAction;
    @FXML
    private TableColumn<Request, String> colReason;
    @FXML
    private TableColumn<Request, String> colDate;

    private final RequestService requestService = new RequestService();
    private final AlertInformation alertInfo = new AlertInformation();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!Session.isEmployee() || Session.getCurrentEmployee().getHierarchyLevel() != 1) {
            alertInfo.viewAlert("ERROR", "SOLO EL DUEÑO", "Acceso restringido",
                    "Esta sección es solo para el Dueño.");
            new ViewFactory().viewMainMenu();
            return;
        }

        colRequester.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getRequesterFirstName() + " " + d.getValue().getRequesterLastName()));
        colAction.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAction()));
        colReason.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReason()));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getRequestDate())));

        loadTable();
    }

    private void loadTable() {
        tableRequests.setItems(FXCollections.observableArrayList(requestService.getPending()));
    }

    private Request getSelectedRequest() {
        Request selected = tableRequests.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alertInfo.viewAlert("WARNING", "SIN SELECCIÓN", "Ninguna solicitud seleccionada",
                    "Selecciona una solicitud de la tabla primero.");
        }
        return selected;
    }

    @FXML
    public void onApprove(MouseEvent event) {
        Request selected = getSelectedRequest();
        if (selected == null) {
            return;
        }
        Employee owner = Session.getCurrentEmployee();
        requestService.approve(selected, owner);
        alertInfo.viewAlert("INFORMATION", "SOLICITUD APROBADA", "Listo",
                "Se aprobó \"" + selected.getAction() + "\". Recuerda que debes realizar tú "
                + "mismo esa acción desde tu sesión -- aprobar no la ejecuta automáticamente.");
        loadTable();
    }

    @FXML
    public void onReject(MouseEvent event) {
        Request selected = getSelectedRequest();
        if (selected == null) {
            return;
        }

        Optional<String> reasonPrompt = promptRejectionReason(selected);
        if (reasonPrompt.isEmpty()) {
            return;
        }

        Employee owner = Session.getCurrentEmployee();
        requestService.reject(selected, owner, reasonPrompt.get());
        alertInfo.viewAlert("INFORMATION", "SOLICITUD RECHAZADA", "Listo",
                "Se rechazó la solicitud de \"" + selected.getAction() + "\".");
        loadTable();
    }

    private Optional<String> promptRejectionReason(Request request) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("MOTIVO DEL RECHAZO");
        dialog.setHeaderText("Vas a rechazar \"" + request.getAction() + "\"");
        dialog.setContentText("Explica por qué la rechazas (el empleado lo va a ver):");

        Optional<String> response = dialog.showAndWait();
        if (response.isEmpty()) {
            return Optional.empty();
        }
        String reason = response.get().trim();
        if (reason.isEmpty()) {
            alertInfo.viewAlert("WARNING", "MOTIVO REQUERIDO", "Explica el motivo",
                    "Debes escribir una razón antes de rechazar la solicitud.");
            return Optional.empty();
        }
        return Optional.of(reason);
    }

    @FXML
    public void onRefresh(MouseEvent event) {
        loadTable();
    }

    @FXML
    public void onBack(MouseEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
