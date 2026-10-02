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
public class MessagesController implements Initializable {

    @FXML
    private ListView<Request> listMessages;
    @FXML
    private Label lblNoMessages;

    private final RequestService requestService = new RequestService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        listMessages.setCellFactory(lista -> createMessageCell());
        loadMessages();
    }

    private void loadMessages() {
        if (!Session.isEmployee()) {
            return;
        }
        Employee employee = Session.getCurrentEmployee();

        List<Request> rejectedRequests = requestService.getMyRequests(employee).stream()
                .filter(request -> "RECHAZADA".equals(request.getStatus()))
                .toList();

        listMessages.setItems(FXCollections.observableArrayList(rejectedRequests));

        boolean empty = rejectedRequests.isEmpty();
        lblNoMessages.setVisible(empty);
        lblNoMessages.setManaged(empty);
        listMessages.setVisible(!empty);
        listMessages.setManaged(!empty);
    }

    private ListCell<Request> createMessageCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Request request, boolean empty) {
                super.updateItem(request, empty);
                if (empty || request == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label title = new Label("SOLICITUD RECHAZADA");
                title.setStyle("-fx-text-fill: #FF3B3B; -fx-font-weight: bold; -fx-font-size: 14px;");

                Label action = new Label(request.getAction());
                action.setStyle("-fx-text-fill: #FF9A3C; -fx-font-weight: bold;");
                action.setWrapText(true);

                String reason = request.getResponseReason() == null
                        ? "(el Dueño no dejó una razón)"
                        : request.getResponseReason();
                Label responseReason = new Label("Razón: " + reason);
                responseReason.setStyle("-fx-text-fill: #E8E8E8;");
                responseReason.setWrapText(true);

                Label footer = new Label("Lo pediste porque: \"" + request.getReason() + "\"   |   "
                        + "Respondió: " + getApproverName(request)
                        + "   |   " + request.getResponseDate());
                footer.setStyle("-fx-text-fill: #9A9A9A; -fx-font-size: 11px;");
                footer.setWrapText(true);

                VBox block = new VBox(4.0, title, action, responseReason, footer);
                block.setPadding(new Insets(10.0));
                setText(null);
                setGraphic(block);
            }
        };
    }

    private String getApproverName(Request request) {
        if (request.getApproverFirstName() == null) {
            return "—";
        }
        return request.getApproverFirstName() + " " + request.getApproverLastName();
    }

    @FXML
    public void onRefresh(MouseEvent event) {
        loadMessages();
    }

    @FXML
    public void onBack(MouseEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
