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
import org.cinekinal.system.model.Customer;
import org.cinekinal.system.model.Ticket;
import org.cinekinal.system.repository.TicketRepository;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.QRCodeGenerator;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

public class MyTicketsController implements Initializable {

    @FXML
    private TableView<Ticket> tableTickets;
    @FXML
    private TableColumn<Ticket, String> colMovie;
    @FXML
    private TableColumn<Ticket, String> colTheater;
    @FXML
    private TableColumn<Ticket, String> colDate;
    @FXML
    private TableColumn<Ticket, String> colTime;
    @FXML
    private TableColumn<Ticket, String> colSeat;
    @FXML
    private TableColumn<Ticket, String> colStatus;

    @FXML
    private VBox panelDetail;
    @FXML
    private Label lblAccessStatus;
    @FXML
    private ImageView imgQR;
    @FXML
    private Label lblMovieTitle;
    @FXML
    private Label lblTheaterSchedule;
    @FXML
    private Label lblSeat;
    @FXML
    private Label lblCustomer;
    @FXML
    private Label lblPrice;
    @FXML
    private Label lblPurchaseDate;
    @FXML
    private Label lblTicketId;
    @FXML
    private Button btnCopyId;

    private final TicketRepository ticketRepo = new TicketRepository();
    private final AlertInformation alertInfo = new AlertInformation();
    private Ticket selectedTicket;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colMovie.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMovieTitle()));
        colTheater.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTheaterName()));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDate() != null ? d.getValue().getDate().toString() : ""));
        colTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTime() != null ? d.getValue().getTime().toString() : ""));
        colSeat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedSeat()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(
                ticketRepo.isTicketCheckedIn(d.getValue().getIdTicket()) ? "🟡 UTILIZADO" : "🟢 VÁLIDO"));

        tableTickets.getSelectionModel().selectedItemProperty().addListener((obs, prev, selected) -> {
            if (selected != null) {
                displayTicketDetails(selected);
            }
        });

        tableTickets.setOnMouseClicked(event -> {
            Ticket selected = tableTickets.getSelectionModel().getSelectedItem();
            if (selected != null) {
                displayTicketDetails(selected);
            }
        });

        loadTickets();
    }

    private void loadTickets() {
        if (!Session.isCustomer() || Session.getCurrentCustomer() == null) {
            clearDetails();
            return;
        }

        Customer customer = Session.getCurrentCustomer();
        List<Ticket> tickets = ticketRepo.getTicketsByCustomer(customer.getIdCustomer());
        tableTickets.setItems(FXCollections.observableArrayList(tickets));

        if (!tickets.isEmpty()) {
            tableTickets.getSelectionModel().select(0);
            Ticket first = tableTickets.getSelectionModel().getSelectedItem();
            if (first == null) {
                first = tickets.get(0);
            }
            displayTicketDetails(first);
        } else {
            clearDetails();
        }
    }

    private void displayTicketDetails(Ticket ticket) {
        this.selectedTicket = ticket;
        boolean alreadyCheckedIn = ticketRepo.isTicketCheckedIn(ticket.getIdTicket());

        if (alreadyCheckedIn) {
            lblAccessStatus.setText("🟡 UTILIZADO · INGRESO YA REGISTRADO");
            lblAccessStatus.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 4; -fx-background-color: #8C8025; -fx-text-fill: #FFFFFF;");
        } else {
            lblAccessStatus.setText("🟢 VÁLIDO PARA INGRESAR · PRESENTA ESTE QR");
            lblAccessStatus.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 4; -fx-background-color: #0F9D66; -fx-text-fill: #FFFFFF;");
        }

        lblMovieTitle.setText("🎬 " + ticket.getMovieTitle());
        lblTheaterSchedule.setText("🏛️ " + ticket.getTheaterName() + " · 📅 " + ticket.getDate() + " · 🕒 " + ticket.getTime());
        lblSeat.setText("🪑 Asiento: " + ticket.getFormattedSeat());
        lblCustomer.setText("👤 Titular: " + ticket.getCustomerName());
        lblPrice.setText("💰 Total pagado: Q" + ticket.getFinalPrice());
        lblPurchaseDate.setText("📅 Compra registrada: " + (ticket.getPurchaseDate() != null ? ticket.getPurchaseDate().toString() : "Reciente"));
        lblTicketId.setText("UUID: " + ticket.getIdTicket());

        String qrContent = "CINE KINAL · TICKET DE ENTRADA\n"
                + "ID Boleto: " + ticket.getIdTicket() + "\n"
                + "Película: " + ticket.getMovieTitle() + "\n"
                + "Sala: " + ticket.getTheaterName() + "\n"
                + "Fecha: " + ticket.getDate() + " " + ticket.getTime() + "\n"
                + "Asiento: " + ticket.getFormattedSeat() + "\n"
                + "Cliente: " + ticket.getCustomerName() + "\n"
                + "Precio: Q" + ticket.getFinalPrice();

        try {
            Image qrImage = QRCodeGenerator.generar(qrContent, 200);
            imgQR.setImage(qrImage);
        } catch (Exception e) {
            imgQR.setImage(null);
        }
    }

    private void clearDetails() {
        selectedTicket = null;
        lblAccessStatus.setText("SIN BOLETOS REGISTRADOS");
        lblAccessStatus.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 4; -fx-background-color: #2A3568; -fx-text-fill: #EAF0FF;");
        lblMovieTitle.setText("Aún no has comprado boletos");
        lblTheaterSchedule.setText("Ve a 'Comprar boletos' para elegir una función.");
        lblSeat.setText("Asiento: -");
        lblCustomer.setText("Titular: -");
        lblPrice.setText("Total pagado: -");
        lblPurchaseDate.setText("Fecha: -");
        lblTicketId.setText("UUID: -");
        imgQR.setImage(null);
    }

    @FXML
    public void onCopyId(MouseEvent event) {
        if (selectedTicket != null && selectedTicket.getIdTicket() != null) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedTicket.getIdTicket());
            clipboard.setContent(content);
            alertInfo.viewAlert("INFORMATION", "PORTAPAPELES", "Código Copiado",
                    "El UUID del boleto fue copiado al portapapeles:\n" + selectedTicket.getIdTicket());
        }
    }

    @FXML
    public void onRefresh(MouseEvent event) {
        loadTickets();
        alertInfo.viewAlert("INFORMATION", "ESTADO ACTUALIZADO", "Boletos actualizados",
                "Se ha sincronizado el estado más reciente de tus boletos.");
    }

    @FXML
    public void onBuyMore(MouseEvent event) {
        new ViewFactory().viewBuyTickets();
    }

    @FXML
    public void onBack(MouseEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
