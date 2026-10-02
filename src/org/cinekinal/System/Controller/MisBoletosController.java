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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.cinekinal.system.model.Customer;
import org.cinekinal.system.model.Ticket;
import org.cinekinal.system.repository.TicketRepository;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.QRCodeGenerator;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

public class MisBoletosController implements Initializable {

    @FXML
    private TableView<Ticket> tablaBoletos;
    @FXML
    private TableColumn<Ticket, String> colPelicula;
    @FXML
    private TableColumn<Ticket, String> colSala;
    @FXML
    private TableColumn<Ticket, String> colFecha;
    @FXML
    private TableColumn<Ticket, String> colHora;
    @FXML
    private TableColumn<Ticket, String> colAsiento;
    @FXML
    private TableColumn<Ticket, String> colEstado;

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

    private final TicketRepository ticketRepo = new TicketRepository();
    private final AlertInformation alertInfo = new AlertInformation();
    private Ticket boletoSeleccionado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colPelicula.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMovieTitle()));
        colSala.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTheaterName()));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDate() != null ? d.getValue().getDate().toString() : ""));
        colHora.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTime() != null ? d.getValue().getTime().toString() : ""));
        colAsiento.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedSeat()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(
                ticketRepo.isTicketCheckedIn(d.getValue().getIdTicket()) ? "🟡 UTILIZADO" : "🟢 VÁLIDO"));

        // Listener de selección
        tablaBoletos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionada) -> {
            if (seleccionada != null) {
                mostrarDetalleBoleto(seleccionada);
            }
        });

        // Clic directo
        tablaBoletos.setOnMouseClicked(event -> {
            Ticket seleccionada = tablaBoletos.getSelectionModel().getSelectedItem();
            if (seleccionada != null) {
                mostrarDetalleBoleto(seleccionada);
            }
        });

        cargarBoletos();
    }

    private void cargarBoletos() {
        if (!Session.isCustomer() || Session.getCurrentCustomer() == null) {
            limpiarDetalles();
            return;
        }

        Customer cliente = Session.getCurrentCustomer();
        List<Ticket> boletos = ticketRepo.getTicketsByCustomer(cliente.getIdCustomer());
        tablaBoletos.setItems(FXCollections.observableArrayList(boletos));

        if (!boletos.isEmpty()) {
            tablaBoletos.getSelectionModel().select(0);
            Ticket primero = tablaBoletos.getSelectionModel().getSelectedItem();
            if (primero == null) {
                primero = boletos.get(0);
            }
            mostrarDetalleBoleto(primero);
        } else {
            limpiarDetalles();
        }
    }

    private void mostrarDetalleBoleto(Ticket boleto) {
        this.boletoSeleccionado = boleto;
        boolean yaIngreso = ticketRepo.isTicketCheckedIn(boleto.getIdTicket());

        if (yaIngreso) {
            lblEstadoAcceso.setText("🟡 UTILIZADO · INGRESO YA REGISTRADO");
            lblEstadoAcceso.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 4; -fx-background-color: #8C8025; -fx-text-fill: #FFFFFF;");
        } else {
            lblEstadoAcceso.setText("🟢 VÁLIDO PARA INGRESAR · PRESENTA ESTE QR");
            lblEstadoAcceso.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 4; -fx-background-color: #0F9D66; -fx-text-fill: #FFFFFF;");
        }

        lblTituloPelicula.setText("🎬 " + boleto.getMovieTitle());
        lblSalaHorario.setText("🏛️ " + boleto.getTheaterName() + " · 📅 " + boleto.getDate() + " · 🕒 " + boleto.getTime());
        lblAsiento.setText("🪑 Asiento: " + boleto.getFormattedSeat());
        lblCliente.setText("👤 Titular: " + boleto.getCustomerName());
        lblPrecio.setText("💰 Total pagado: Q" + boleto.getFinalPrice());
        lblFechaCompra.setText("📅 Compra registrada: " + (boleto.getPurchaseDate() != null ? boleto.getPurchaseDate().toString() : "Reciente"));
        lblIdBoleto.setText("UUID: " + boleto.getIdTicket());

        // Generar Código QR con los datos del boleto
        String contenidoQR = "CINE KINAL · TICKET DE ENTRADA\n"
                + "ID Boleto: " + boleto.getIdTicket() + "\n"
                + "Película: " + boleto.getMovieTitle() + "\n"
                + "Sala: " + boleto.getTheaterName() + "\n"
                + "Fecha: " + boleto.getDate() + " " + boleto.getTime() + "\n"
                + "Asiento: " + boleto.getFormattedSeat() + "\n"
                + "Cliente: " + boleto.getCustomerName() + "\n"
                + "Precio: Q" + boleto.getFinalPrice();

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
        if (boletoSeleccionado != null && boletoSeleccionado.getIdTicket() != null) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(boletoSeleccionado.getIdTicket());
            clipboard.setContent(content);
            alertInfo.viewAlert("INFORMATION", "PORTAPAPELES", "Código Copiado",
                    "El UUID del boleto fue copiado al portapapeles:\n" + boletoSeleccionado.getIdTicket());
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
