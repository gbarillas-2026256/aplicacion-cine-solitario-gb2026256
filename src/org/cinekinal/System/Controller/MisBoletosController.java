package org.cinekinal.system.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.cinekinal.system.model.Boleto;
import org.cinekinal.system.model.Cliente;
import org.cinekinal.system.repository.BoletoRepository;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.QRCodeGenerator;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

public class MisBoletosController implements Initializable {

    @FXML
    private TableView<Boleto> tablaBoletos;
    @FXML
    private TableColumn<Boleto, String> colPelicula;
    @FXML
    private TableColumn<Boleto, String> colSala;
    @FXML
    private TableColumn<Boleto, String> colFecha;
    @FXML
    private TableColumn<Boleto, String> colHora;
    @FXML
    private TableColumn<Boleto, String> colAsiento;
    @FXML
    private TableColumn<Boleto, String> colEstado;

    @FXML
    private VBox panelDetalle;
    @FXML
    private Label lblEstadoAcceso;
    @FXML
    private ImageView imgQR;
    @FXML
    private Label lblTituloPelicula;
    @FXML
    private Label lblSalaHorario;
    @FXML
    private Label lblAsiento;
    @FXML
    private Label lblCliente;
    @FXML
    private Label lblPrecio;
    @FXML
    private Label lblFechaCompra;
    @FXML
    private Label lblIdBoleto;
    @FXML
    private Button btnCopiarId;

    private final BoletoRepository boletoRepo = new BoletoRepository();
    private final AlertInformation alertInfo = new AlertInformation();
    private Boleto boletoSeleccionado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colPelicula.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTituloPelicula()));
        colSala.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreSala()));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha() != null ? d.getValue().getFecha().toString() : ""));
        colHora.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHora() != null ? d.getValue().getHora().toString() : ""));
        colAsiento.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAsientoFormateado()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(
                boletoRepo.estaBoletoIngresado(d.getValue().getIdBoleto()) ? "🟡 UTILIZADO" : "🟢 VÁLIDO"));

        // Listener de selección
        tablaBoletos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionada) -> {
            if (seleccionada != null) {
                mostrarDetalleBoleto(seleccionada);
            }
        });

        // Clic directo
        tablaBoletos.setOnMouseClicked(event -> {
            Boleto seleccionada = tablaBoletos.getSelectionModel().getSelectedItem();
            if (seleccionada != null) {
                mostrarDetalleBoleto(seleccionada);
            }
        });

        cargarBoletos();
    }

    private void cargarBoletos() {
        if (!Session.esCliente() || Session.getClienteActual() == null) {
            limpiarDetalles();
            return;
        }

        Cliente cliente = Session.getClienteActual();
        List<Boleto> boletos = boletoRepo.obtenerPorCliente(cliente.getIdCliente());
        tablaBoletos.setItems(FXCollections.observableArrayList(boletos));

        if (!boletos.isEmpty()) {
            tablaBoletos.getSelectionModel().select(0);
            Boleto primero = tablaBoletos.getSelectionModel().getSelectedItem();
            if (primero == null) {
                primero = boletos.get(0);
            }
            mostrarDetalleBoleto(primero);
        } else {
            limpiarDetalles();
        }
    }

    private void mostrarDetalleBoleto(Boleto boleto) {
        this.boletoSeleccionado = boleto;
        boolean yaIngreso = boletoRepo.estaBoletoIngresado(boleto.getIdBoleto());

        if (yaIngreso) {
            lblEstadoAcceso.setText("🟡 UTILIZADO · INGRESO YA REGISTRADO");
            lblEstadoAcceso.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 4; -fx-background-color: #8C8025; -fx-text-fill: #FFFFFF;");
        } else {
            lblEstadoAcceso.setText("🟢 VÁLIDO PARA INGRESAR · PRESENTA ESTE QR");
            lblEstadoAcceso.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 4; -fx-background-color: #0F9D66; -fx-text-fill: #FFFFFF;");
        }

        lblTituloPelicula.setText("🎬 " + boleto.getTituloPelicula());
        lblSalaHorario.setText("🏛️ " + boleto.getNombreSala() + " · 📅 " + boleto.getFecha() + " · 🕒 " + boleto.getHora());
        lblAsiento.setText("🪑 Asiento: " + boleto.getAsientoFormateado());
        lblCliente.setText("👤 Titular: " + boleto.getNombreCliente());
        lblPrecio.setText("💰 Total pagado: Q" + boleto.getPrecioFinal());
        lblFechaCompra.setText("📅 Compra registrada: " + (boleto.getFechaCompra() != null ? boleto.getFechaCompra().toString() : "Reciente"));
        lblIdBoleto.setText("UUID: " + boleto.getIdBoleto());

        // Generar Código QR con los datos del boleto
        String contenidoQR = "CINE KINAL · TICKET DE ENTRADA\n"
                + "ID Boleto: " + boleto.getIdBoleto() + "\n"
                + "Película: " + boleto.getTituloPelicula() + "\n"
                + "Sala: " + boleto.getNombreSala() + "\n"
                + "Fecha: " + boleto.getFecha() + " " + boleto.getHora() + "\n"
                + "Asiento: " + boleto.getAsientoFormateado() + "\n"
                + "Cliente: " + boleto.getNombreCliente() + "\n"
                + "Precio: Q" + boleto.getPrecioFinal();

        try {
            Image qrImage = QRCodeGenerator.generar(contenidoQR, 200);
            imgQR.setImage(qrImage);
        } catch (Exception e) {
            imgQR.setImage(null);
        }
    }

    private void limpiarDetalles() {
        boletoSeleccionado = null;
        lblEstadoAcceso.setText("SIN BOLETOS REGISTRADOS");
        lblEstadoAcceso.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 4; -fx-background-color: #2A3568; -fx-text-fill: #EAF0FF;");
        lblTituloPelicula.setText("Aún no has comprado boletos");
        lblSalaHorario.setText("Ve a 'Comprar boletos' para elegir una función.");
        lblAsiento.setText("Asiento: -");
        lblCliente.setText("Titular: -");
        lblPrecio.setText("Total pagado: -");
        lblFechaCompra.setText("Fecha: -");
        lblIdBoleto.setText("UUID: -");
        imgQR.setImage(null);
    }

    @FXML
    public void onCopiarId(MouseEvent event) {
        if (boletoSeleccionado != null && boletoSeleccionado.getIdBoleto() != null) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(boletoSeleccionado.getIdBoleto());
            clipboard.setContent(content);
            alertInfo.viewAlert("INFORMATION", "PORTAPAPELES", "Código Copiado",
                    "El UUID del boleto fue copiado al portapapeles:\n" + boletoSeleccionado.getIdBoleto());
        }
    }

    @FXML
    public void onActualizar(MouseEvent event) {
        cargarBoletos();
        alertInfo.viewAlert("INFORMATION", "ESTADO ACTUALIZADO", "Boletos actualizados",
                "Se ha sincronizado el estado más reciente de tus boletos.");
    }

    @FXML
    public void onComprarMas(MouseEvent event) {
        new ViewFactory().viewComprarBoletos();
    }

    @FXML
    public void onVolver(MouseEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
