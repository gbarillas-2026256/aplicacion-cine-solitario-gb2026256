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
import org.cinekinal.system.model.Employee;
import org.cinekinal.system.model.Request;
import org.cinekinal.system.service.RequestService;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

/**
 * Controller for viewing employee request history.
 */
public class MisSolicitudesController implements Initializable {

    @FXML
    private TableView<Request> tablaMisSolicitudes;
    @FXML
    private TableColumn<Request, String> colAccion;
    @FXML
    private TableColumn<Request, String> colMotivo;
    @FXML
    private TableColumn<Request, String> colEstado;
    @FXML
    private TableColumn<Request, String> colMotivoRespuesta;
    @FXML
    private TableColumn<Request, String> colFechaSolicitud;
    @FXML
    private TableColumn<Request, String> colRespondidoPor;

    private final RequestService requestService = new RequestService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colAccion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAction()));
        colMotivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReason()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        colEstado.setCellFactory(columna -> celdaConColorDeEstado());
        colMotivoRespuesta.setCellValueFactory(d -> {
            String respuesta = d.getValue().getResponseReason();
            return new SimpleStringProperty(respuesta == null ? "—" : respuesta);
        });
        colFechaSolicitud.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getRequestDate())));
        colRespondidoPor.setCellValueFactory(d -> {
            Request request = d.getValue();
            if (request.getApproverFirstName() == null) {
                return new SimpleStringProperty("—");
            }
            return new SimpleStringProperty(request.getApproverFirstName() + " " + request.getApproverLastName());
        });

        cargarTabla();
    }

    private void cargarTabla() {
        if (!Session.isEmployee()) {
            return;
        }
        Employee employee = Session.getCurrentEmployee();
        tablaMisSolicitudes.setItems(FXCollections.observableArrayList(requestService.getMyRequests(employee)));
    }

    /** Pinta PENDIENTE en amarillo, APROBADA en verde y RECHAZADA en rojo, igual que el resto de la UI. */
    private TableCell<Request, String> celdaConColorDeEstado() {
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
