package org.cinekinal.system.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.cinekinal.system.model.Ticket;
import org.cinekinal.system.repository.TicketRepository;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.ViewFactory;

public class VerifyEntryController implements Initializable {

    @FXML
    private TextField txtTicketId;
    @FXML
    private Label lblAccessStatus;
    @FXML
    private Label lblMovie;
    @FXML
    private Label lblTheater;
    @FXML
    private Label lblDate;
    @FXML
    private Label lblTime;
    @FXML
    private Label lblSeat;
    @FXML
    private Label lblPrice;
    @FXML
    private Label lblCustomer;
    @FXML
    private Label lblPurchaseDate;
    @FXML
    private Button btnValidateEntry;

    private final TicketRepository ticketRepo = new TicketRepository();
    private final AlertInformation alertInfo = new AlertInformation();
    private Ticket currentTicket = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnValidateEntry.setDisable(true);
        clearDetails();
    }

    @FXML
    public void onSearchTicket(ActionEvent event) {
        String ticketId = txtTicketId.getText().trim();
        if (ticketId.isEmpty()) {
            alertInfo.viewAlert("WARNING", "CAMPO VACÍO",
                    "INGRESA UN CÓDIGO", "Por favor ingresa o escanea el ID del boleto.");
            return;
        }

        // Si se escaneó el contenido completo del QR, extraer automáticamente el UUID
        if (ticketId.contains("ID Boleto:")) {
            ticketId = ticketId.substring(ticketId.indexOf("ID Boleto:") + "ID Boleto:".length()).trim();
            if (ticketId.contains("\n")) {
                ticketId = ticketId.substring(0, ticketId.indexOf("\n")).trim();
            }
            txtTicketId.setText(ticketId);
        } else if (ticketId.contains("Boleto:")) {
            ticketId = ticketId.substring(ticketId.indexOf("Boleto:") + "Boleto:".length()).trim();
            if (ticketId.contains("\n")) {
                ticketId = ticketId.substring(0, ticketId.indexOf("\n")).trim();
            }
            txtTicketId.setText(ticketId);
        }

        currentTicket = ticketRepo.findTicketById(ticketId);
        if (currentTicket == null) {
            clearDetails();
            lblAccessStatus.setText("❌ BOLETO NO ENCONTRADO / INVÁLIDO");
            lblAccessStatus.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 2; -fx-background-color: #7A0C14; -fx-text-fill: #ECEDE9;");
            btnValidateEntry.setDisable(true);
            alertInfo.viewAlert("ERROR", "BOLETO INVÁLIDO",
                    "NO REGISTRADO", "No existe ningún boleto emitido con el identificador ingresado.");
            return;
        }

        // Mostrar detalles
        lblMovie.setText(currentTicket.getMovieTitle());
        lblTheater.setText(currentTicket.getTheaterName());
        lblDate.setText(currentTicket.getDate() != null ? currentTicket.getDate().toString() : "—");
        lblTime.setText(currentTicket.getTime() != null ? currentTicket.getTime().toString() : "—");
        lblSeat.setText("Fila " + currentTicket.getRow() + " · Asiento " + currentTicket.getNumber());
        lblPrice.setText("Q " + currentTicket.getFinalPrice());
        lblCustomer.setText(currentTicket.getCustomerName());
        lblPurchaseDate.setText(currentTicket.getPurchaseDate() != null ? currentTicket.getPurchaseDate().toString() : "—");

        boolean alreadyCheckedIn = ticketRepo.isTicketCheckedIn(currentTicket.getIdTicket());
        if (alreadyCheckedIn) {
            lblAccessStatus.setText("⚠️ BOLETO YA UTILIZADO (ACCESO DENEGADO)");
            lblAccessStatus.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 2; -fx-background-color: #C4470A; -fx-text-fill: #ECEDE9;");
            btnValidateEntry.setDisable(true);
            alertInfo.viewAlert("WARNING", "BOLETO YA USADO",
                    "ACCESO DENEGADO", "Este boleto ya fue validado e ingresado previamente.");
        } else {
            lblAccessStatus.setText("✓ BOLETO VÁLIDO - LISTO PARA INGRESAR");
            lblAccessStatus.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 2; -fx-background-color: #1F9C3D; -fx-text-fill: #ECEDE9;");
            btnValidateEntry.setDisable(false);
        }
    }

    @FXML
    public void onValidateEntry(ActionEvent event) {
        if (currentTicket == null) {
            return;
        }

        ticketRepo.markTicketCheckedIn(currentTicket.getIdTicket());
        lblAccessStatus.setText("✓ ACCESO REGISTRADO - DISFRUTE LA FUNCIÓN");
        lblAccessStatus.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 2; -fx-background-color: #12777F; -fx-text-fill: #ECEDE9;");
        btnValidateEntry.setDisable(true);

        alertInfo.viewAlert("INFORMATION", "ACCESO AUTORIZADO",
                "ENTRADA REGISTRADA",
                "El boleto ha sido marcado como ingresado correctamente.\nAsiento: "
                        + currentTicket.getFormattedSeat() + "\nSala: " + currentTicket.getTheaterName());
    }

    @FXML
    public void onBack(ActionEvent event) {
        new ViewFactory().viewMainMenu();
    }

    private void clearDetails() {
        lblMovie.setText("—");
        lblTheater.setText("—");
        lblDate.setText("—");
        lblTime.setText("—");
        lblSeat.setText("—");
        lblPrice.setText("—");
        lblCustomer.setText("—");
        lblPurchaseDate.setText("—");
        lblAccessStatus.setText("ESPERANDO BOLETO");
        lblAccessStatus.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 2; -fx-background-color: #202226; -fx-text-fill: #9AA095;");
    }
}
