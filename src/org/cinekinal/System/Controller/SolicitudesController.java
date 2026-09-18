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
import org.cinekinal.system.model.Empleado;
import org.cinekinal.system.model.Solicitud;
import org.cinekinal.system.service.SolicitudService;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

/**
 * Pantalla donde el Dueño resuelve las Solicitudes pendientes creadas
 * por SolicitudService.intentar(...) cuando un empleado de menor
 * jerarquia intenta una accion que necesita permiso.
 *
 * IMPORTANTE (ver el comentario en SolicitudService): aprobar una
 * solicitud NO ejecuta la accion original sola -- el Dueño debe
 * realizarla el mismo despues desde su propia sesion.
 */
public class SolicitudesController implements Initializable {

    @FXML
    private TableView<Solicitud> tablaSolicitudes;
    @FXML
    private TableColumn<Solicitud, String> colSolicitante;
    @FXML
    private TableColumn<Solicitud, String> colAccion;
    @FXML
    private TableColumn<Solicitud, String> colMotivo;
    @FXML
    private TableColumn<Solicitud, String> colFecha;

    private final SolicitudService solicitudService = new SolicitudService();
    private final AlertInformation alertInfo = new AlertInformation();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!Session.esEmpleado() || Session.getEmpleadoActual().getNivelJerarquico() != 1) {
            alertInfo.viewAlert("ERROR", "SOLO EL DUEÑO", "Acceso restringido",
                    "Esta sección es solo para el Dueño.");
            new ViewFactory().viewMainMenu();
            return;
        }

        colSolicitante.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getSolicitanteNombres() + " " + d.getValue().getSolicitanteApellidos()));
        colAccion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAccion()));
        colMotivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMotivo()));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getFechaSolicitud())));

        cargarTabla();
    }

    private void cargarTabla() {
        tablaSolicitudes.setItems(FXCollections.observableArrayList(solicitudService.obtenerPendientes()));
    }

    private Solicitud obtenerSeleccionada() {
        Solicitud seleccionada = tablaSolicitudes.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            alertInfo.viewAlert("WARNING", "SIN SELECCIÓN", "Ninguna solicitud seleccionada",
                    "Selecciona una solicitud de la tabla primero.");
        }
        return seleccionada;
    }

    @FXML
    public void onAprobar(MouseEvent event) {
        Solicitud seleccionada = obtenerSeleccionada();
        if (seleccionada == null) {
            return;
        }
        Empleado dueño = Session.getEmpleadoActual();
        solicitudService.aprobar(seleccionada, dueño);
        alertInfo.viewAlert("INFORMATION", "SOLICITUD APROBADA", "Listo",
                "Se aprobó \"" + seleccionada.getAccion() + "\". Recuerda que debes realizar tú "
                + "mismo esa acción desde tu sesión -- aprobar no la ejecuta automáticamente.");
        cargarTabla();
    }

    @FXML
    public void onRechazar(MouseEvent event) {
        Solicitud seleccionada = obtenerSeleccionada();
        if (seleccionada == null) {
            return;
        }

        Optional<String> motivoIngresado = pedirMotivoRechazo(seleccionada);
        if (motivoIngresado.isEmpty()) {
            return; // el Dueño cancelo el cuadro de texto
        }

        Empleado dueño = Session.getEmpleadoActual();
        solicitudService.rechazar(seleccionada, dueño, motivoIngresado.get());
        alertInfo.viewAlert("INFORMATION", "SOLICITUD RECHAZADA", "Listo",
                "Se rechazó la solicitud de \"" + seleccionada.getAccion() + "\".");
        cargarTabla();
    }

    /**
     * El rechazo SIEMPRE necesita una razon (a diferencia de aprobar, que
     * no la pide): el Gerente la va a ver en su pantalla de Mensajes, asi
     * que dejarla vacia lo dejaria sin saber por que le negaron la accion.
     */
    private Optional<String> pedirMotivoRechazo(Solicitud solicitud) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("MOTIVO DEL RECHAZO");
        dialog.setHeaderText("Vas a rechazar \"" + solicitud.getAccion() + "\"");
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
