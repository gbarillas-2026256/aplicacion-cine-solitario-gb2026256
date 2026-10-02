package org.cinekinal.system.controller;

import java.awt.Desktop;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.cinekinal.system.model.Customer;
import org.cinekinal.system.model.Seat;
import org.cinekinal.system.model.Showtime;
import org.cinekinal.system.model.Ticket;
import org.cinekinal.system.model.TicketPurchaseStatus;
import org.cinekinal.system.repository.SeatRepository;
import org.cinekinal.system.repository.ShowtimeRepository;
import org.cinekinal.system.repository.TicketRepository;
import org.cinekinal.system.service.TicketService;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.QRCodeGenerator;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

public class BuyTicketController implements Initializable {

    // Search and Filters
    @FXML
    private TextField txtSearchMovie;
    @FXML
    private TableView<Showtime> tableBillboard;
    @FXML
    private TableColumn<Showtime, String> colMovie;
    @FXML
    private TableColumn<Showtime, String> colTheater;
    @FXML
    private TableColumn<Showtime, String> colDate;
    @FXML
    private TableColumn<Showtime, String> colTime;
    @FXML
    private TableColumn<Showtime, String> colPrice;

    @FXML
    private GridPane gridSeats;
    @FXML
    private Label lblSelection;
    @FXML
    private Button btnBuy;

    @FXML
    private Button btnToday;
    @FXML
    private Button btnTomorrow;
    @FXML
    private Button btnAll;
    @FXML
    private DatePicker dpDate;

    // Panel 1: Movie Details
    @FXML
    private VBox panelMovieDetails;
    @FXML
    private ImageView imgPoster;
    @FXML
    private Label lblMovieTitle;
    @FXML
    private Label lblGenre;
    @FXML
    private Label lblRating;
    @FXML
    private Label lblDuration;
    @FXML
    private Label lblTheaterType;
    @FXML
    private Label lblPriceDetail;
    @FXML
    private Label lblSynopsis;
    @FXML
    private Button btnTrailer;

    // Panel 2: Ticket with QR Code
    @FXML
    private VBox panelTicket;
    @FXML
    private ImageView imgQR;
    @FXML
    private Label lblTicketMovie;
    @FXML
    private Label lblTicketTheaterSchedule;
    @FXML
    private Label lblTicketSeat;
    @FXML
    private Label lblTicketCustomer;
    @FXML
    private Label lblTicketPayment;
    @FXML
    private Label lblTicketId;

    private final ShowtimeRepository showtimeRepo = new ShowtimeRepository();
    private final SeatRepository seatRepo = new SeatRepository();
    private final TicketRepository ticketRepo = new TicketRepository();
    private final TicketService ticketService = new TicketService();
    private final AlertInformation alertInfo = new AlertInformation();

    private final Map<String, Button> seatButtons = new HashMap<>();
    private Showtime selectedShowtime;
    private Seat selectedSeat;

    private record CardData(String last4Digits, String cardholderName) {}

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colMovie.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMovieTitle()));
        colTheater.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTheaterName()));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getDate())));
        colTime.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getTime())));
        colPrice.setCellValueFactory(d -> new SimpleStringProperty("Q" + d.getValue().getBasePrice()));

        btnTrailer.setDisable(true);

        tableBillboard.getSelectionModel().selectedItemProperty().addListener((obs, prev, selected) -> {
            if (selected != null) {
                selectShowtime(selected);
            }
        });

        tableBillboard.setOnMouseClicked(event -> {
            Showtime selected = tableBillboard.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectShowtime(selected);
            }
        });

        if (txtSearchMovie != null) {
            txtSearchMovie.setOnAction(e -> onSearch(null));
        }

        loadBillboard(showtimeRepo.getBillboard());
    }

    private void selectShowtime(Showtime showtime) {
        if (showtime == null) {
            clearMovieDetails();
            return;
        }
        this.selectedShowtime = showtime;
        displayMovieDetails(showtime);
        loadSeatMap(showtime);
        showDetailsPanel();
    }

    private void loadBillboard(List<Showtime> showtimes) {
        tableBillboard.setItems(FXCollections.observableArrayList(showtimes));
        if (!showtimes.isEmpty()) {
            tableBillboard.getSelectionModel().select(0);
            Showtime first = tableBillboard.getSelectionModel().getSelectedItem();
            if (first == null) {
                first = showtimes.get(0);
            }
            selectShowtime(first);
        } else {
            clearMovieDetails();
        }
    }

    @FXML
    public void onSearch(MouseEvent event) {
        String query = txtSearchMovie.getText();
        if (query == null || query.isBlank()) {
            loadBillboard(showtimeRepo.getBillboard());
            return;
        }
        List<Showtime> results = showtimeRepo.searchByTitle(query.trim());
        loadBillboard(results);
        if (results.isEmpty()) {
            alertInfo.viewAlert("INFORMATION", "BÚSQUEDA DE PELÍCULAS", "Sin resultados",
                    "No se encontraron funciones para \"" + query.trim() + "\".");
        }
    }

    private void displayMovieDetails(Showtime showtime) {
        lblMovieTitle.setText(showtime.getMovieTitle());
        lblGenre.setText("Género: " + (showtime.getGenre() != null ? showtime.getGenre() : "N/D"));
        lblRating.setText("Clasificación: " + (showtime.getRating() != null ? showtime.getRating() : "N/D"));
        lblDuration.setText("Duración: " + showtime.getDurationMin() + " min");
        lblTheaterType.setText("Sala: " + showtime.getTheaterName() + " (" + showtime.getTheaterType() + ")");
        lblPriceDetail.setText("Precio base: Q" + showtime.getBasePrice());

        if (showtime.getSynopsis() != null && !showtime.getSynopsis().isBlank()) {
            lblSynopsis.setText(showtime.getSynopsis());
        } else {
            lblSynopsis.setText("Sin sinopsis registrada para esta película.");
        }

        loadPosterAsync(showtime.getPosterUrl());
        btnTrailer.setDisable(showtime.getTrailerUrl() == null || showtime.getTrailerUrl().isBlank());
    }

    private void loadPosterAsync(String urlStr) {
        if (urlStr == null || urlStr.isBlank()) {
            imgPoster.setImage(null);
            return;
        }
        CompletableFuture.supplyAsync(() -> {
            try {
                if (urlStr.startsWith("http://") || urlStr.startsWith("https://")) {
                    URL url = URI.create(urlStr).toURL();
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                    conn.setConnectTimeout(4000);
                    conn.setReadTimeout(5000);
                    if (conn.getResponseCode() == 200) {
                        try (InputStream in = conn.getInputStream()) {
                            return new Image(in);
                        }
                    }
                } else {
                    return new Image(urlStr);
                }
            } catch (Exception ignored) {
            }
            return null;
        }).thenAccept(image -> Platform.runLater(() -> imgPoster.setImage(image)));
    }

    private void clearMovieDetails() {
        lblMovieTitle.setText("No hay funciones para esta fecha");
        lblGenre.setText("Género: -");
        lblRating.setText("Clasificación: -");
        lblDuration.setText("Duración: -");
        lblTheaterType.setText("Sala: -");
        lblPriceDetail.setText("Precio: -");
        lblSynopsis.setText("Selecciona otra fecha o programa una nueva función.");
        imgPoster.setImage(null);
        btnTrailer.setDisable(true);
        gridSeats.getChildren().clear();
        lblSelection.setText("Ningún asiento seleccionado");
    }

    private void loadSeatMap(Showtime showtime) {
        selectedSeat = null;
        seatButtons.clear();
        lblSelection.setText("Ningún asiento seleccionado");
        gridSeats.getChildren().clear();

        List<Seat> seats = seatRepo.getByTheater(showtime.getIdTheater());
        Set<String> occupiedIds = ticketRepo.getOccupiedSeats(showtime.getIdShowtime());

        for (Seat seat : seats) {
            Button btn = new Button(seat.getShortLabel());
            btn.getStyleClass().add("eva-seat");

            boolean isOccupied = occupiedIds.contains(seat.getIdSeat());
            btn.getStyleClass().add(isOccupied ? "eva-seat-ocupado" : "eva-seat-libre");
            btn.setDisable(isOccupied);
            if (!isOccupied) {
                btn.setOnAction(e -> selectSeat(seat, btn));
            }
            seatButtons.put(seat.getIdSeat(), btn);

            int row = Character.toUpperCase(seat.getRow().charAt(0)) - 'A';
            int col = seat.getNumber() - 1;
            gridSeats.add(btn, col, row);
        }
    }

    private void selectSeat(Seat seat, Button btn) {
        if (selectedSeat != null) {
            Button prevBtn = seatButtons.get(selectedSeat.getIdSeat());
            if (prevBtn != null) {
                prevBtn.getStyleClass().remove("eva-seat-seleccionado");
                prevBtn.getStyleClass().add("eva-seat-libre");
            }
        }
        selectedSeat = seat;
        btn.getStyleClass().remove("eva-seat-libre");
        btn.getStyleClass().add("eva-seat-seleccionado");
        lblSelection.setText("Asiento seleccionado: " + seat.getShortLabel() + " (Q" + selectedShowtime.getBasePrice() + ")");
    }

    @FXML
    public void onWatchTrailer(MouseEvent event) {
        if (selectedShowtime != null && selectedShowtime.getTrailerUrl() != null) {
            openWebLink(selectedShowtime.getTrailerUrl());
        }
    }

    private void openWebLink(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
            }
        } catch (Exception e) {
            alertInfo.viewAlert("WARNING", "TRÁILER", "Enlace del tráiler:", url);
        }
    }

    @FXML
    public void onFilterToday(MouseEvent event) {
        Date today = Date.valueOf(LocalDate.now());
        loadBillboard(showtimeRepo.getBillboardByDate(today));
    }

    @FXML
    public void onFilterTomorrow(MouseEvent event) {
        Date tomorrow = Date.valueOf(LocalDate.now().plusDays(1));
        loadBillboard(showtimeRepo.getBillboardByDate(tomorrow));
    }

    @FXML
    public void onFilterAll(MouseEvent event) {
        loadBillboard(showtimeRepo.getBillboard());
    }

    @FXML
    public void onDateChanged(ActionEvent event) {
        if (dpDate.getValue() != null) {
            Date date = Date.valueOf(dpDate.getValue());
            loadBillboard(showtimeRepo.getBillboardByDate(date));
        }
    }

    @FXML
    public void onBuy(MouseEvent event) {
        if (selectedShowtime == null || selectedSeat == null) {
            alertInfo.viewAlert("WARNING", "FALTAN DATOS", "Elige función y asiento",
                    "Selecciona una función de la cartelera y un asiento libre antes de comprar.");
            return;
        }

        String customerId;
        String customerDisplayName;
        if (Session.isCustomer() && Session.getCurrentCustomer() != null) {
            Customer currentCustomer = Session.getCurrentCustomer();
            customerId = currentCustomer.getIdCustomer();
            customerDisplayName = currentCustomer.getFullName();
        } else if (Session.isEmployee()) {
            alertInfo.viewAlert("WARNING", "MODO ADMINISTRADOR", "Venta en Taquilla",
                    "Estás conectado como empleado. Para registrar ventas desde recepción, "
                    + "utiliza la opción 'Registrar Venta / Taquilla'.");
            return;
        } else {
            alertInfo.viewAlert("ERROR", "SIN SESIÓN", "Acceso no válido", "Debes iniciar sesión para comprar boletos.");
            return;
        }

        if (!confirmPurchase(selectedShowtime, selectedSeat)) {
            return;
        }

        Optional<CardData> cardOpt = promptCardData(customerDisplayName);
        if (cardOpt.isEmpty()) {
            return;
        }

        CardData card = cardOpt.get();

        TicketPurchaseStatus result = ticketService.purchase(
                selectedShowtime.getIdShowtime(), customerId,
                selectedSeat.getIdSeat(), selectedShowtime.getBasePrice());

        switch (result) {
            case PURCHASE_COMPLETED -> {
                Ticket ticket = ticketRepo.getLastPurchasedTicket(
                        selectedShowtime.getIdShowtime(), selectedSeat.getIdSeat());

                String ticketId = ticket != null ? ticket.getIdTicket() : UUID.randomUUID().toString();
                String displayName = (ticket != null && ticket.getCustomerName() != null)
                        ? ticket.getCustomerName() : customerDisplayName;

                String qrContent = buildQRContent(ticketId, selectedShowtime, selectedSeat, displayName);
                Image qrImage = QRCodeGenerator.generar(qrContent, 220);

                displayTicket(ticketId, selectedShowtime, selectedSeat, displayName, card, qrImage);
                loadSeatMap(selectedShowtime);
            }
            case SEAT_ALREADY_SOLD -> {
                alertInfo.viewAlert("WARNING", "ASIENTO OCUPADO", "Asiento no disponible",
                        "Alguien más reservó este asiento hace unos instantes. Por favor elige otro.");
                loadSeatMap(selectedShowtime);
            }
            case PURCHASE_ERROR -> alertInfo.viewAlert("ERROR", "ERROR", "No se pudo procesar la compra",
                    "Ocurrió un error al registrar la compra. Intenta de nuevo.");
        }
    }

    private boolean confirmPurchase(Showtime showtime, Seat seat) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("CONFIRMAR COMPRA DE BOLETO");
        confirmation.setHeaderText("¿Confirmas la compra de este boleto?");
        confirmation.setContentText("🎬 Película: " + showtime.getMovieTitle()
                + "\n🏛️ Sala: " + showtime.getTheaterName() + " (" + showtime.getTheaterType() + ")"
                + "\n📅 Horario: " + showtime.getDate() + " " + showtime.getTime()
                + "\n🪑 Asiento: " + seat.getShortLabel()
                + "\n💰 Total a pagar: Q" + showtime.getBasePrice());
        Optional<ButtonType> response = confirmation.showAndWait();
        return response.isPresent() && response.get() == ButtonType.OK;
    }

    private Optional<CardData> promptCardData(String defaultName) {
        Dialog<CardData> dialog = new Dialog<>();
        dialog.setTitle("PAGO CON TARJETA DE CRÉDITO / DÉBITO");
        dialog.setHeaderText("Simulación de Cobro Seguro · Ingrese los datos de su tarjeta");

        ButtonType btnPay = new ButtonType("Pagar Q" + selectedShowtime.getBasePrice(), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnPay, ButtonType.CANCEL);

        TextField txtNumber = new TextField();
        txtNumber.setPromptText("1234 5678 9012 3456");
        TextField txtExpiry = new TextField();
        txtExpiry.setPromptText("MM/AA (ej. 08/28)");
        PasswordField txtCvv = new PasswordField();
        txtCvv.setPromptText("3 o 4 dígitos");
        TextField txtCardName = new TextField();
        txtCardName.setPromptText("Nombre como figura en el plástico");
        if (defaultName != null && !defaultName.isBlank()) {
            txtCardName.setText(defaultName);
        }

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #ff4d4d; -fx-font-size: 11px; -fx-font-weight: bold;");
        lblError.setWrapText(true);
        lblError.setMaxWidth(300);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(14, 18, 14, 18));
        grid.addRow(0, new Label("Número de tarjeta (16 dígitos):"), txtNumber);
        grid.addRow(1, new Label("Vencimiento (MM/AA):"), txtExpiry);
        grid.addRow(2, new Label("Código de seguridad CVV:"), txtCvv);
        grid.addRow(3, new Label("Nombre en la tarjeta:"), txtCardName);
        grid.add(lblError, 1, 4);
        dialog.getDialogPane().setContent(grid);

        Button payButtonNode = (Button) dialog.getDialogPane().lookupButton(btnPay);
        payButtonNode.addEventFilter(ActionEvent.ACTION, event -> {
            String error = validateCard(txtNumber.getText(), txtExpiry.getText(),
                    txtCvv.getText(), txtCardName.getText());
            if (error != null) {
                lblError.setText(error);
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == btnPay) {
                String digitsOnly = txtNumber.getText().replaceAll("[^0-9]", "");
                String last4 = digitsOnly.length() >= 4 ? digitsOnly.substring(digitsOnly.length() - 4) : "0000";
                return new CardData(last4, txtCardName.getText().trim());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private String validateCard(String number, String expiry, String cvv, String name) {
        String digitsOnly = number == null ? "" : number.replaceAll("[^0-9]", "");
        if (digitsOnly.length() != 16) {
            return "El número de tarjeta debe tener exactamente 16 dígitos.";
        }
        if (expiry == null || !expiry.trim().matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
            return "El vencimiento debe tener formato MM/AA (ej. 08/28).";
        }
        if (cvv == null || !cvv.trim().matches("[0-9]{3,4}")) {
            return "El código CVV debe tener 3 o 4 dígitos numéricos.";
        }
        if (name == null || name.isBlank()) {
            return "Ingresa el nombre del titular como aparece en la tarjeta.";
        }
        return null;
    }

    private String buildQRContent(String ticketId, Showtime showtime, Seat seat, String customerName) {
        return "CINE KINAL · TICKET DE ENTRADA\n"
                + "ID Boleto: " + ticketId + "\n"
                + "Película: " + showtime.getMovieTitle() + "\n"
                + "Sala: " + showtime.getTheaterName() + " (" + showtime.getTheaterType() + ")\n"
                + "Fecha: " + showtime.getDate() + " " + showtime.getTime() + "\n"
                + "Asiento: " + seat.getShortLabel() + "\n"
                + "Cliente: " + customerName + "\n"
                + "Precio: Q" + showtime.getBasePrice();
    }

    private void displayTicket(String ticketId, Showtime showtime, Seat seat,
                               String customerName, CardData card, Image qrImage) {
        panelMovieDetails.setVisible(false);
        panelMovieDetails.setManaged(false);
        panelTicket.setVisible(true);
        panelTicket.setManaged(true);

        imgQR.setImage(qrImage);
        lblTicketMovie.setText("🎬 " + showtime.getMovieTitle());
        lblTicketTheaterSchedule.setText("🏛️ " + showtime.getTheaterName() + " (" + showtime.getTheaterType() + ") · " + showtime.getDate() + " " + showtime.getTime());
        lblTicketSeat.setText("🪑 Asiento: " + seat.getShortLabel());
        lblTicketCustomer.setText("👤 Cliente: " + customerName);
        lblTicketPayment.setText("💳 Total Pagado: Q" + showtime.getBasePrice() + " (Tarjeta **** " + card.last4Digits() + ")");
        lblTicketId.setText("UUID / Código de Entrada: " + ticketId);
    }

    @FXML
    public void onNewPurchase(MouseEvent event) {
        showDetailsPanel();
        if (selectedShowtime != null) {
            loadSeatMap(selectedShowtime);
        }
    }

    private void showDetailsPanel() {
        if (panelTicket != null) {
            panelTicket.setVisible(false);
            panelTicket.setManaged(false);
        }
        if (panelMovieDetails != null) {
            panelMovieDetails.setVisible(true);
            panelMovieDetails.setManaged(true);
        }
    }

    @FXML
    public void onGoToMyTickets(MouseEvent event) {
        new ViewFactory().viewMyTickets();
    }

    @FXML
    public void onBack(MouseEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
