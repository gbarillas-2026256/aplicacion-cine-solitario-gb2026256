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

public class VerificarEntradaController implements Initializable {

    @FXML
    private TextField txtIdBoleto;
    @FXML
    private Label lblEstadoAcceso;
    @FXML
    private Label lblPelicula;
    @FXML
    private Label lblSala;
    @FXML
    private Label lblFecha;
    @FXML
    private Label lblHora;
    @FXML
    private Label lblAsiento;
    @FXML
    private Label lblPrecio;
    @FXML
    private Label lblCliente;
    @FXML
    private Label lblFechaCompra;
    @FXML
    private Button btnValidarIngreso;

    private final TicketRepository ticketRepo = new TicketRepository();
    private final AlertInformation alertInfo = new AlertInformation();
    private Ticket boletoActual = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnValidarIngreso.setDisable(true);
        limpiarDetalles();
    }

    @FXML
    public void onBuscarBoleto(ActionEvent event) {
        String idBoleto = txtIdBoleto.getText().trim();
        if (idBoleto.isEmpty()) {
            alertInfo.viewAlert("WARNING", "CAMPO VACÍO",
                    "INGRESA UN CÓDIGO", "Por favor ingresa o escanea el ID del boleto.");
            return;
        }

        // Si se escaneó el contenido completo del QR, extraer automáticamente el UUID
        if (idBoleto.contains("ID Boleto:")) {
            idBoleto = idBoleto.substring(idBoleto.indexOf("ID Boleto:") + "ID Boleto:".length()).trim();
            if (idBoleto.contains("\n")) {
                idBoleto = idBoleto.substring(0, idBoleto.indexOf("\n")).trim();
            }
            txtIdBoleto.setText(idBoleto);
        } else if (idBoleto.contains("Boleto:")) {
            idBoleto = idBoleto.substring(idBoleto.indexOf("Boleto:") + "Boleto:".length()).trim();
            if (idBoleto.contains("\n")) {
                idBoleto = idBoleto.substring(0, idBoleto.indexOf("\n")).trim();
            }
            txtIdBoleto.setText(idBoleto);
        }

        boletoActual = ticketRepo.findTicketById(idBoleto);
        if (boletoActual == null) {
            limpiarDetalles();
            lblEstadoAcceso.setText("❌ BOLETO NO ENCONTRADO / INVÁLIDO");
            lblEstadoAcceso.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 2; -fx-background-color: #7A0C14; -fx-text-fill: #ECEDE9;");
            btnValidarIngreso.setDisable(true);
            alertInfo.viewAlert("ERROR", "BOLETO INVÁLIDO",
                    "NO REGISTRADO", "No existe ningún boleto emitido con el identificador ingresado.");
            return;
        }

        // Mostrar detalles
        lblPelicula.setText(boletoActual.getMovieTitle());
        lblSala.setText(boletoActual.getTheaterName());
        lblFecha.setText(boletoActual.getDate() != null ? boletoActual.getDate().toString() : "—");
        lblHora.setText(boletoActual.getTime() != null ? boletoActual.getTime().toString() : "—");
        lblAsiento.setText("Fila " + boletoActual.getRow() + " · Asiento " + boletoActual.getNumber());
        lblPrecio.setText("Q " + boletoActual.getFinalPrice());
        lblCliente.setText(boletoActual.getCustomerName());
        lblFechaCompra.setText(boletoActual.getPurchaseDate() != null ? boletoActual.getPurchaseDate().toString() : "—");

        boolean yaIngreso = ticketRepo.isTicketCheckedIn(boletoActual.getIdTicket());
        if (yaIngreso) {
            lblEstadoAcceso.setText("⚠️ BOLETO YA UTILIZADO (ACCESO DENEGADO)");
            lblEstadoAcceso.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 2; -fx-background-color: #C4470A; -fx-text-fill: #ECEDE9;");
            btnValidarIngreso.setDisable(true);
            alertInfo.viewAlert("WARNING", "BOLETO YA USADO",
                    "ACCESO DENEGADO", "Este boleto ya fue validado e ingresado previamente.");
        } else {
            lblEstadoAcceso.setText("✓ BOLETO VÁLIDO - LISTO PARA INGRESAR");
            lblEstadoAcceso.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 2; -fx-background-color: #1F9C3D; -fx-text-fill: #ECEDE9;");
            btnValidarIngreso.setDisable(false);
        }
    }

    @FXML
    public void onValidarIngreso(ActionEvent event) {
        if (boletoActual == null) {
            return;
        }

        ticketRepo.markTicketCheckedIn(boletoActual.getIdTicket());
        lblEstadoAcceso.setText("✓ ACCESO REGISTRADO - DISFRUTE LA FUNCIÓN");
        lblEstadoAcceso.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 2; -fx-background-color: #12777F; -fx-text-fill: #ECEDE9;");
        btnValidarIngreso.setDisable(true);

        alertInfo.viewAlert("INFORMATION", "ACCESO AUTORIZADO",
                "ENTRADA REGISTRADA",
                "El boleto ha sido marcado como ingresado correctamente.\nAsiento: "
                        + boletoActual.getFormattedSeat() + "\nSala: " + boletoActual.getTheaterName());
    }

    @FXML
    public void onBack(ActionEvent event) {
        new ViewFactory().viewMainMenu();
    }

    private void limpiarDetalles() {
        lblPelicula.setText("—");
        lblSala.setText("—");
        lblFecha.setText("—");
        lblHora.setText("—");
        lblAsiento.setText("—");
        lblPrecio.setText("—");
        lblCliente.setText("—");
        lblFechaCompra.setText("—");
        lblEstadoAcceso.setText("ESPERANDO BOLETO");
        lblEstadoAcceso.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 2; -fx-background-color: #202226; -fx-text-fill: #9AA095;");
    }
}
