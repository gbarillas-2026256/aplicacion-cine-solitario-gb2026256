package org.cinekinal.system.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import org.cinekinal.system.model.Empleado;
import org.cinekinal.system.model.Solicitud;
import org.cinekinal.system.service.SolicitudService;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

/**
 * Pantalla donde CUALQUIER empleado (no solo el Dueño) revisa el
 * historial completo de las solicitudes que el mismo ha hecho, y si
 * ya fueron aprobadas, rechazadas, o siguen pendientes. Es la forma
 * de "quedarse en espera de la respuesta" sin bloquear la pantalla:
 * el empleado sigue usando la app normalmente y viene aqui cuando
 * quiera revisar si ya le contestaron.
 */
public class MisSolicitudesController implements Initializable {

    @FXML
    private TableView<Solicitud> tablaMisSolicitudes;
    @FXML
    private TableColumn<Solicitud, String> colAccion;
    @FXML
    private TableColumn<Solicitud, String> colMotivo;
    @FXML
    private TableColumn<Solicitud, String> colEstado;
    @FXML
    private TableColumn<Solicitud, String> colFechaSolicitud;
    @FXML
    private TableColumn<Solicitud, String> colRespondidoPor;

    private final SolicitudService solicitudService = new SolicitudService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colAccion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAccion()));
        colMotivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMotivo()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado()));
        colEstado.setCellFactory(columna -> celdaConColorDeEstado());
        colFechaSolicitud.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getFechaSolicitud())));
        colRespondidoPor.setCellValueFactory(d -> {
            Solicitud solicitud = d.getValue();
            if (solicitud.getAprobadorNombres() == null) {
                return new SimpleStringProperty("—");
            }
            return new SimpleStringProperty(solicitud.getAprobadorNombres() + " " + solicitud.getAprobadorApellidos());
        });

        cargarTabla();
    }

    private void cargarTabla() {
        if (!Session.esEmpleado()) {
            return;
        }
        Empleado empleado = Session.getEmpleadoActual();
        tablaMisSolicitudes.setItems(FXCollections.observableArrayList(solicitudService.obtenerMisSolicitudes(empleado)));
    }

    /** Pinta PENDIENTE en amarillo, APROBADA en verde y RECHAZADA en rojo, igual que el resto de la UI. */
    private TableCell<Solicitud, String> celdaConColorDeEstado() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean vacio) {
                super.updateItem(estado, vacio);
                if (vacio || estado == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(estado);
                switch (estado) {
                    case "APROBADA" -> setStyle("-fx-text-fill: #39FF6A; -fx-font-weight: bold;");
                    case "RECHAZADA" -> setStyle("-fx-text-fill: #FF3B3B; -fx-font-weight: bold;");
                    default -> setStyle("-fx-text-fill: #FFD500; -fx-font-weight: bold;");
                }
            }
        };
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
