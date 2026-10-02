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
public class SolicitudesController implements Initializable {

    @FXML
    private TableView<Request> tablaSolicitudes;
    @FXML
    private TableColumn<Request, String> colSolicitante;
    @FXML
    private TableColumn<Request, String> colAccion;
    @FXML
    private TableColumn<Request, String> colMotivo;
    @FXML
    private TableColumn<Request, String> colFecha;

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

        colSolicitante.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getRequesterFirstName() + " " + d.getValue().getRequesterLastName()));
        colAccion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAction()));
        colMotivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReason()));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getRequestDate())));

        cargarTabla();
    }

    private void cargarTabla() {
        tablaSolicitudes.setItems(FXCollections.observableArrayList(requestService.getPending()));
    }

    private Request obtenerSeleccionada() {
        Request seleccionada = tablaSolicitudes.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            alertInfo.viewAlert("WARNING", "SIN SELECCIÓN", "Ninguna solicitud seleccionada",
                    "Selecciona una solicitud de la tabla primero.");
        }
        return seleccionada;
    }

    @FXML
    public void onAprobar(MouseEvent event) {
        Request seleccionada = obtenerSeleccionada();
        if (seleccionada == null) {
            return;
        }
        Employee owner = Session.getCurrentEmployee();
        requestService.approve(seleccionada, owner);
        alertInfo.viewAlert("INFORMATION", "SOLICITUD APROBADA", "Listo",
                "Se aprobó \"" + seleccionada.getAction() + "\". Recuerda que debes realizar tú "
                + "mismo esa acción desde tu sesión -- aprobar no la ejecuta automáticamente.");
        cargarTabla();
    }

    @FXML
    public void onRechazar(MouseEvent event) {
        Request seleccionada = obtenerSeleccionada();
        if (seleccionada == null) {
            return;
        }

        Optional<String> motivoIngresado = pedirMotivoRechazo(seleccionada);
        if (motivoIngresado.isEmpty()) {
            return;
        }

        Employee owner = Session.getCurrentEmployee();
        requestService.reject(seleccionada, owner, motivoIngresado.get());
        alertInfo.viewAlert("INFORMATION", "SOLICITUD RECHAZADA", "Listo",
                "Se rechazó la solicitud de \"" + seleccionada.getAction() + "\".");
        cargarTabla();
    }

    private Optional<String> pedirMotivoRechazo(Request solicitud) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("MOTIVO DEL RECHAZO");
        dialog.setHeaderText("Vas a rechazar \"" + solicitud.getAction() + "\"");
        dialog.setContentText("Explica por qué la rechazas (el empleado lo va a ver):");

        Optional<String> respuesta = dialog.showAndWait();
        if (respuesta.isEmpty()) {
            return Optional.empty();
        }
        String motivo = respuesta.get().trim();
        if (motivo.isEmpty()) {
            alertInfo.viewAlert("WARNING", "MOTIVO REQUERIDO", "Explica el motivo",
                    "Debes escribir una razón antes de rechazar la solicitud.");
            return Optional.empty();
        }
        return Optional.of(motivo);
    }

    @FXML
    public void onRefrescar(MouseEvent event) {
        cargarTabla();
    }

    @FXML
    public void onVolver(MouseEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
