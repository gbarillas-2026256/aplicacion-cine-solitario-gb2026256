package org.cinekinal.system.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.cinekinal.system.model.Boleto;
import org.cinekinal.system.repository.BoletoRepository;
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

    private final BoletoRepository boletoRepo = new BoletoRepository();
    private final AlertInformation alertInfo = new AlertInformation();
    private Boleto boletoActual = null;

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

        boletoActual = boletoRepo.buscarBoletoPorId(idBoleto);
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
        lblPelicula.setText(boletoActual.getTituloPelicula());
        lblSala.setText(boletoActual.getNombreSala());
        lblFecha.setText(boletoActual.getFecha() != null ? boletoActual.getFecha().toString() : "—");
        lblHora.setText(boletoActual.getHora() != null ? boletoActual.getHora().toString() : "—");
        lblAsiento.setText("Fila " + boletoActual.getFila() + " · Asiento " + boletoActual.getNumero());
        lblPrecio.setText("Q " + boletoActual.getPrecioFinal());
        lblCliente.setText(boletoActual.getNombreCliente());
        lblFechaCompra.setText(boletoActual.getFechaCompra() != null ? boletoActual.getFechaCompra().toString() : "—");

        boolean yaIngreso = boletoRepo.estaBoletoIngresado(boletoActual.getIdBoleto());
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

        boletoRepo.marcarBoletoIngresado(boletoActual.getIdBoleto());
        lblEstadoAcceso.setText("✓ ACCESO REGISTRADO - DISFRUTE LA FUNCIÓN");
        lblEstadoAcceso.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 2; -fx-background-color: #12777F; -fx-text-fill: #ECEDE9;");
        btnValidarIngreso.setDisable(true);

        alertInfo.viewAlert("INFORMATION", "ACCESO AUTORIZADO",
                "ENTRADA REGISTRADA",
                "El boleto ha sido marcado como ingresado correctamente.\nAsiento: "
                        + boletoActual.getAsientoFormateado() + "\nSala: " + boletoActual.getNombreSala());
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
