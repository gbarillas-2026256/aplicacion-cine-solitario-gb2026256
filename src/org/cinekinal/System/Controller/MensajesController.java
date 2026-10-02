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
import org.cinekinal.system.model.Employee;
import org.cinekinal.system.model.Request;
import org.cinekinal.system.service.RequestService;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

/**
 * Controller for viewing rejected request notifications/messages for employees.
 */
public class MensajesController implements Initializable {

    @FXML
    private ListView<Request> listaMensajes;
    @FXML
    private Label lblSinMensajes;

    private final RequestService requestService = new RequestService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        listaMensajes.setCellFactory(lista -> celdaDeMensaje());
        cargarMensajes();
    }

    private void cargarMensajes() {
        if (!Session.isEmployee()) {
            return;
        }
        Employee employee = Session.getCurrentEmployee();

        List<Request> rechazadas = requestService.getMyRequests(employee).stream()
                .filter(request -> "RECHAZADA".equals(request.getStatus()))
                .toList();

        listaMensajes.setItems(FXCollections.observableArrayList(rechazadas));

        boolean vacio = rechazadas.isEmpty();
        lblSinMensajes.setVisible(vacio);
        lblSinMensajes.setManaged(vacio);
        listaMensajes.setVisible(!vacio);
        listaMensajes.setManaged(!vacio);
    }

    /** Cada mensaje se pinta como un bloque: titulo en rojo, accion, razon y quien respondio. */
    private ListCell<Request> celdaDeMensaje() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Request solicitud, boolean vacio) {
                super.updateItem(solicitud, vacio);
                if (vacio || solicitud == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label titulo = new Label("SOLICITUD RECHAZADA");
                titulo.setStyle("-fx-text-fill: #FF3B3B; -fx-font-weight: bold; -fx-font-size: 14px;");

                Label accion = new Label(solicitud.getAction());
                accion.setStyle("-fx-text-fill: #FF9A3C; -fx-font-weight: bold;");
                accion.setWrapText(true);

                String razon = solicitud.getResponseReason() == null
                        ? "(el Dueño no dejó una razón)"
                        : solicitud.getResponseReason();
                Label motivoRespuesta = new Label("Razón: " + razon);
                motivoRespuesta.setStyle("-fx-text-fill: #E8E8E8;");
                motivoRespuesta.setWrapText(true);

                Label pie = new Label("Lo pediste porque: \"" + solicitud.getReason() + "\"   |   "
                        + "Respondió: " + nombreDelAprobador(solicitud)
                        + "   |   " + solicitud.getResponseDate());
                pie.setStyle("-fx-text-fill: #9A9A9A; -fx-font-size: 11px;");
                pie.setWrapText(true);

                VBox bloque = new VBox(4.0, titulo, accion, motivoRespuesta, pie);
                bloque.setPadding(new Insets(10.0));
                setText(null);
                setGraphic(bloque);
            }
        };
    }

    private String nombreDelAprobador(Request solicitud) {
        if (solicitud.getApproverFirstName() == null) {
            return "—";
        }
        return solicitud.getApproverFirstName() + " " + solicitud.getApproverLastName();
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
