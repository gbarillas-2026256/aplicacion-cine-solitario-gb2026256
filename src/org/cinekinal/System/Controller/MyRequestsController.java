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
public class MyRequestsController implements Initializable {

    @FXML
    private TableView<Request> tableMyRequests;
    @FXML
    private TableColumn<Request, String> colAction;
    @FXML
    private TableColumn<Request, String> colReason;
    @FXML
    private TableColumn<Request, String> colStatus;
    @FXML
    private TableColumn<Request, String> colResponseReason;
    @FXML
    private TableColumn<Request, String> colRequestDate;
    @FXML
    private TableColumn<Request, String> colAnsweredBy;

    private final RequestService requestService = new RequestService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colAction.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAction()));
        colReason.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReason()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        colStatus.setCellFactory(columna -> createStatusColoredCell());
        colResponseReason.setCellValueFactory(d -> {
            String respuesta = d.getValue().getResponseReason();
            return new SimpleStringProperty(respuesta == null ? "—" : respuesta);
        });
        colRequestDate.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getRequestDate())));
        colAnsweredBy.setCellValueFactory(d -> {
            Request request = d.getValue();
            if (request.getApproverFirstName() == null) {
                return new SimpleStringProperty("—");
            }
            return new SimpleStringProperty(request.getApproverFirstName() + " " + request.getApproverLastName());
        });

        loadTable();
    }

    private void loadTable() {
        if (!Session.isEmployee()) {
            return;
        }
        Employee employee = Session.getCurrentEmployee();
        tableMyRequests.setItems(FXCollections.observableArrayList(requestService.getMyRequests(employee)));
    }

    private TableCell<Request, String> createStatusColoredCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(status);
                switch (status) {
                    case "APROBADA" -> setStyle("-fx-text-fill: #39FF6A; -fx-font-weight: bold;");
                    case "RECHAZADA" -> setStyle("-fx-text-fill: #FF3B3B; -fx-font-weight: bold;");
                    default -> setStyle("-fx-text-fill: #FFD500; -fx-font-weight: bold;");
                }
            }
        };
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
