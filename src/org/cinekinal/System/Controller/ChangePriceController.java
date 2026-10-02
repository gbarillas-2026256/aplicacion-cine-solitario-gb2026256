package org.cinekinal.system.controller;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import org.cinekinal.system.model.Employee;
import org.cinekinal.system.model.Showtime;
import org.cinekinal.system.repository.RequestRepository;
import org.cinekinal.system.repository.ShowtimeRepository;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

public class ChangePriceController implements Initializable {

    @FXML
    private TableView<Showtime> tableShowtimes;
    @FXML
    private TableColumn<Showtime, String> colMovie;
    @FXML
    private TableColumn<Showtime, String> colTheater;
    @FXML
    private TableColumn<Showtime, String> colDate;
    @FXML
    private TableColumn<Showtime, String> colTime;
    @FXML
    private TableColumn<Showtime, String> colCurrentPrice;

    @FXML
    private Label lblSelectedMovie;
    @FXML
    private Label lblSelectedSchedule;
    @FXML
    private Label lblSelectedCurrentPrice;
    @FXML
    private TextField txtNewPrice;
    @FXML
    private Label lblPermissionNote;
    @FXML
    private Button btnApplyPrice;

    private final ShowtimeRepository showtimeRepo = new ShowtimeRepository();
    private final RequestRepository requestRepo = new RequestRepository();
    private final AlertInformation alertInfo = new AlertInformation();
    private Showtime selectedShowtime = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colMovie.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMovieTitle()));
        colTheater.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTheaterName()));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDate() != null ? d.getValue().getDate().toString() : "—"));
        colTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTime() != null ? d.getValue().getTime().toString() : "—"));
        colCurrentPrice.setCellValueFactory(d -> new SimpleStringProperty("Q " + d.getValue().getBasePrice()));

        tableShowtimes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedShowtime = newVal;
                lblSelectedMovie.setText("Película: " + newVal.getMovieTitle());
                lblSelectedSchedule.setText("Horario: " + newVal.getDate() + " " + newVal.getTime() + " (" + newVal.getTheaterName() + ")");
                lblSelectedCurrentPrice.setText("Precio actual: Q " + newVal.getBasePrice());
                txtNewPrice.setText(newVal.getBasePrice().toString());
                btnApplyPrice.setDisable(false);
            } else {
                selectedShowtime = null;
                lblSelectedMovie.setText("Película: —");
                lblSelectedSchedule.setText("Horario: —");
                lblSelectedCurrentPrice.setText("Precio actual: —");
                txtNewPrice.clear();
                btnApplyPrice.setDisable(true);
            }
        });

        Employee emp = Session.getCurrentEmployee();
        if (emp != null && emp.getHierarchyLevel() == 1) {
            lblPermissionNote.setText("Rol: Dueño (nivel 1). Los cambios de precio se aplican inmediatamente.");
            btnApplyPrice.setText("APLICAR PRECIO DIRECTO");
        } else {
            lblPermissionNote.setText("Rol: Gerente (nivel 2). Este cambio generará una solicitud de aprobación para el Dueño.");
            btnApplyPrice.setText("ENVIAR SOLICITUD DE CAMBIO");
        }

        loadTable();
    }

    private void loadTable() {
        tableShowtimes.setItems(FXCollections.observableArrayList(showtimeRepo.getBillboard()));
    }

    @FXML
    public void onApplyPrice(ActionEvent event) {
        if (selectedShowtime == null) {
            return;
        }

        String priceStr = txtNewPrice.getText().trim();
        BigDecimal newPrice;
        try {
            newPrice = new BigDecimal(priceStr);
            if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            alertInfo.viewAlert("WARNING", "PRECIO INVÁLIDO", "ERROR DE PRECIO",
                    "Ingresa una cantidad válida mayor a 0 (ej. 35.00 o 50.00).");
            return;
        }

        Employee emp = Session.getCurrentEmployee();
        if (emp != null && emp.getHierarchyLevel() == 1) {
            String movieTitle = selectedShowtime.getMovieTitle();
            boolean ok = showtimeRepo.updateBasePrice(selectedShowtime.getIdShowtime(), newPrice);
            if (ok) {
                loadTable();
                alertInfo.viewAlert("INFORMATION", "PRECIO ACTUALIZADO", "CAMBIO APLICADO",
                        "El precio de \"" + movieTitle + "\" ha sido fijado en Q " + newPrice + ".");
            } else {
                alertInfo.viewAlert("ERROR", "ERROR AL ACTUALIZAR", "FALLO", "No se pudo actualizar el precio en la base de datos.");
            }
        } else if (emp != null) {
            String action = "CAMBIO DE PRECIO: " + selectedShowtime.getMovieTitle()
                    + " (" + selectedShowtime.getDate() + " " + selectedShowtime.getTime() + ")"
                    + " de Q" + selectedShowtime.getBasePrice() + " a Q" + newPrice;

            Optional<String> reasonPrompt = promptReason();
            if (reasonPrompt.isEmpty()) {
                return;
            }
            String reason = reasonPrompt.get();

            requestRepo.create(emp.getIdEmployee(), action, reason);
            alertInfo.viewAlert("INFORMATION", "SOLICITUD ENVIADA", "PENDIENTE DE APROBACIÓN",
                    "Tu solicitud para cambiar el precio a Q " + newPrice
                            + " ha sido enviada al Dueño para su revisión.");
        }
    }

    private Optional<String> promptReason() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("EXPLICA TU SOLICITUD");
        dialog.setHeaderText("El cambio de precio necesita aprobación del Dueño");
        dialog.setContentText("¿Por qué necesitas hacer este cambio?");

        Optional<String> response = dialog.showAndWait();
        if (response.isEmpty()) {
            return Optional.empty();
        }
        String reason = response.get().trim();
        if (reason.isEmpty()) {
            alertInfo.viewAlert("WARNING", "MOTIVO REQUERIDO", "Explica el motivo",
                    "Escribe una breve razón antes de enviar la solicitud.");
            return Optional.empty();
        }
        return Optional.of(reason);
    }

    @FXML
    public void onBack(ActionEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
