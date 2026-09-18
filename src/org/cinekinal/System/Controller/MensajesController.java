package org.cinekinal.system.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.cinekinal.system.model.Empleado;
import org.cinekinal.system.model.Solicitud;
import org.cinekinal.system.service.SolicitudService;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

/**
 * Bandeja de mensajes del empleado: por ahora muestra los avisos de las
 * solicitudes que el Dueño le RECHAZO, junto con la razon que dio.
 *
 * No necesita un procedimiento propio en la base de datos: reutiliza
 * sp_obtener_solicitudes_por_empleado (el mismo de "Mis solicitudes")
 * y se queda solo con las que tienen estado RECHAZADA.
 */
public class MensajesController implements Initializable {

    @FXML
    private ListView<Solicitud> listaMensajes;
    @FXML
    private Label lblSinMensajes;

    private final SolicitudService solicitudService = new SolicitudService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        listaMensajes.setCellFactory(lista -> celdaDeMensaje());
        cargarMensajes();
    }

    private void cargarMensajes() {
        if (!Session.esEmpleado()) {
            return;
        }
        Empleado empleado = Session.getEmpleadoActual();

        List<Solicitud> rechazadas = solicitudService.obtenerMisSolicitudes(empleado).stream()
                .filter(solicitud -> "RECHAZADA".equals(solicitud.getEstado()))
                .toList();

        listaMensajes.setItems(FXCollections.observableArrayList(rechazadas));

        boolean vacio = rechazadas.isEmpty();
        lblSinMensajes.setVisible(vacio);
        lblSinMensajes.setManaged(vacio);
        listaMensajes.setVisible(!vacio);
        listaMensajes.setManaged(!vacio);
    }

    /** Cada mensaje se pinta como un bloque: titulo en rojo, accion, razon y quien respondio. */
    private ListCell<Solicitud> celdaDeMensaje() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Solicitud solicitud, boolean vacio) {
                super.updateItem(solicitud, vacio);
                if (vacio || solicitud == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label titulo = new Label("SOLICITUD RECHAZADA");
                titulo.setStyle("-fx-text-fill: #FF3B3B; -fx-font-weight: bold; -fx-font-size: 14px;");

                Label accion = new Label(solicitud.getAccion());
                accion.setStyle("-fx-text-fill: #FF9A3C; -fx-font-weight: bold;");
                accion.setWrapText(true);

                String razon = solicitud.getMotivoRespuesta() == null
                        ? "(el Dueño no dejó una razón)"
                        : solicitud.getMotivoRespuesta();
                Label motivoRespuesta = new Label("Razón: " + razon);
                motivoRespuesta.setStyle("-fx-text-fill: #E8E8E8;");
                motivoRespuesta.setWrapText(true);

                Label pie = new Label("Lo pediste porque: \"" + solicitud.getMotivo() + "\"   |   "
                        + "Respondió: " + nombreDelAprobador(solicitud)
                        + "   |   " + solicitud.getFechaRespuesta());
                pie.setStyle("-fx-text-fill: #9A9A9A; -fx-font-size: 11px;");
                pie.setWrapText(true);

                VBox bloque = new VBox(4.0, titulo, accion, motivoRespuesta, pie);
                bloque.setPadding(new Insets(10.0));
                setText(null);
                setGraphic(bloque);
            }
        };
    }

    private String nombreDelAprobador(Solicitud solicitud) {
        if (solicitud.getAprobadorNombres() == null) {
            return "—";
        }
        return solicitud.getAprobadorNombres() + " " + solicitud.getAprobadorApellidos();
    }

    @FXML
    public void onRefrescar(MouseEvent event) {
        cargarMensajes();
    }

    @FXML
    public void onVolver(MouseEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
