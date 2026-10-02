package org.cinekinal.system.controller;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import org.cinekinal.system.model.Customer;
import org.cinekinal.system.model.Seat;
import org.cinekinal.system.model.Showtime;
import org.cinekinal.system.model.TicketPurchaseStatus;
import org.cinekinal.system.repository.CustomerRepository;
import org.cinekinal.system.repository.SeatRepository;
import org.cinekinal.system.repository.ShowtimeRepository;
import org.cinekinal.system.repository.TicketRepository;
import org.cinekinal.system.service.TicketService;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.ViewFactory;

public class BoxOfficeSaleController implements Initializable {

    @FXML
    private ListView<Showtime> listShowtimes;
    @FXML
    private Button btnToday;
    @FXML
    private Button btnTomorrow;
    @FXML
    private Button btnAll;

    @FXML
    private Label lblSelectedTheater;
    @FXML
    private GridPane gridSeats;

    @FXML
    private ComboBox<Customer> cmbCustomer;
    @FXML
    private Label lblMovieSummary;
    @FXML
    private Label lblScheduleSummary;
    @FXML
    private Label lblSeatsSummary;
    @FXML
    private Label lblTotalPayment;
    @FXML
    private Button btnCharge;

    private final ShowtimeRepository showtimeRepo = new ShowtimeRepository();
    private final SeatRepository seatRepo = new SeatRepository();
    private final TicketRepository ticketRepo = new TicketRepository();
    private final CustomerRepository customerRepo = new CustomerRepository();
    private final TicketService ticketService = new TicketService();
    private final AlertInformation alertInfo = new AlertInformation();

    private final Set<Seat> selectedSeats = new HashSet<>();
    private Showtime selectedShowtime = null;
    private List<Showtime> fullBillboard = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initShowtimesList();
        initCustomersCombo();
        loadBillboard();
        onFilterToday(null);
    }

    private void initShowtimesList() {
        listShowtimes.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Showtime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getMovieTitle() + "\n"
                            + item.getTheaterName() + " · " + item.getDate() + " " + item.getTime()
                            + " · Q" + item.getBasePrice());
                }
            }
        });

        listShowtimes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectShowtime(newVal);
            }
        });
    }

    private void initCustomersCombo() {
        Customer genericCustomer = customerRepo.getOrCreateGenericCustomer();
        List<Customer> allCustomers = customerRepo.getAll();

        List<Customer> comboItems = new ArrayList<>();
        if (genericCustomer != null) {
            comboItems.add(genericCustomer);
        }
        for (Customer c : allCustomers) {
            if (genericCustomer == null || !c.getIdCustomer().equals(genericCustomer.getIdCustomer())) {
                comboItems.add(c);
            }
        }

        cmbCustomer.setItems(FXCollections.observableArrayList(comboItems));
        if (!comboItems.isEmpty()) {
            cmbCustomer.getSelectionModel().selectFirst();
        }

        cmbCustomer.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFullName() + (item.isVip() ? " [VIP]" : "") + " (" + item.getEmail() + ")");
                }
            }
        });

        cmbCustomer.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFullName() + (item.isVip() ? " [VIP]" : ""));
                }
            }
        });
    }

    private void loadBillboard() {
        fullBillboard = showtimeRepo.getBillboard();
    }

    @FXML
    public void onFilterToday(ActionEvent event) {
        LocalDate today = LocalDate.now();
        List<Showtime> filtered = fullBillboard.stream()
                .filter(f -> f.getDate() != null && f.getDate().toLocalDate().equals(today))
                .toList();
        listShowtimes.setItems(FXCollections.observableArrayList(filtered));
        updateFilterStyle(btnToday);
    }

    @FXML
    public void onFilterTomorrow(ActionEvent event) {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Showtime> filtered = fullBillboard.stream()
                .filter(f -> f.getDate() != null && f.getDate().toLocalDate().equals(tomorrow))
                .toList();
        listShowtimes.setItems(FXCollections.observableArrayList(filtered));
        updateFilterStyle(btnTomorrow);
    }

    @FXML
    public void onFilterAll(ActionEvent event) {
        listShowtimes.setItems(FXCollections.observableArrayList(fullBillboard));
        updateFilterStyle(btnAll);
    }

    private void updateFilterStyle(Button active) {
        btnToday.getStyleClass().removeAll("boton-filtro-activo", "boton-filtro");
        btnTomorrow.getStyleClass().removeAll("boton-filtro-activo", "boton-filtro");
        btnAll.getStyleClass().removeAll("boton-filtro-activo", "boton-filtro");

        btnToday.getStyleClass().add(btnToday == active ? "boton-filtro-activo" : "boton-filtro");
        btnTomorrow.getStyleClass().add(btnTomorrow == active ? "boton-filtro-activo" : "boton-filtro");
        btnAll.getStyleClass().add(btnAll == active ? "boton-filtro-activo" : "boton-filtro");
    }

    private void selectShowtime(Showtime showtime) {
        this.selectedShowtime = showtime;
        selectedSeats.clear();

        lblMovieSummary.setText("Película: " + showtime.getMovieTitle());
        lblScheduleSummary.setText("Horario: " + showtime.getDate() + " " + showtime.getTime() + " (" + showtime.getTheaterName() + ")");
        lblSelectedTheater.setText(showtime.getTheaterName() + " · " + showtime.getTheaterType() + " · Entrada: Q" + showtime.getBasePrice());

        updateSaleSummary();
        renderSeatsMap();
    }

    private void renderSeatsMap() {
        gridSeats.getChildren().clear();
        if (selectedShowtime == null) {
            return;
        }

        List<Seat> allSeats = seatRepo.getByTheater(selectedShowtime.getIdTheater());
        Set<String> occupiedIds = ticketRepo.getOccupiedSeats(selectedShowtime.getIdShowtime());

        Map<String, Integer> rowIndices = new HashMap<>();
        int rowIndex = 0;

        for (Seat seat : allSeats) {
            if (!rowIndices.containsKey(seat.getRow())) {
                rowIndices.put(seat.getRow(), rowIndex);
                Label lblRow = new Label(seat.getRow());
                lblRow.setStyle("-fx-text-fill: #FF6A13; -fx-font-weight: bold; -fx-padding: 0 8 0 0;");
                gridSeats.add(lblRow, 0, rowIndex);
                rowIndex++;
            }

            int r = rowIndices.get(seat.getRow());
            int c = seat.getNumber();

            Button btnSeat = new Button(seat.getShortLabel());
            btnSeat.getStyleClass().add("eva-seat");

            boolean isOccupied = occupiedIds.contains(seat.getIdSeat());
            if (isOccupied) {
                btnSeat.getStyleClass().add("eva-seat-ocupado");
                btnSeat.setDisable(true);
            } else {
                btnSeat.getStyleClass().add("eva-seat-libre");
                btnSeat.setOnAction(e -> toggleSeatSelection(seat, btnSeat));
            }

            gridSeats.add(btnSeat, c, r);
        }
    }

    private void toggleSeatSelection(Seat seat, Button button) {
        if (selectedSeats.contains(seat)) {
            selectedSeats.remove(seat);
            button.getStyleClass().remove("eva-seat-seleccionado");
            button.getStyleClass().add("eva-seat-libre");
        } else {
            selectedSeats.add(seat);
            button.getStyleClass().remove("eva-seat-libre");
            button.getStyleClass().add("eva-seat-seleccionado");
        }
        updateSaleSummary();
    }

    private void updateSaleSummary() {
        if (selectedSeats.isEmpty()) {
            lblSeatsSummary.setText("Butacas: Ninguna");
            lblTotalPayment.setText("Q 0.00");
            btnCharge.setDisable(true);
            return;
        }

        StringBuilder sb = new StringBuilder("Butacas: ");
        for (Seat a : selectedSeats) {
            sb.append(a.getShortLabel()).append(" ");
        }
        lblSeatsSummary.setText(sb.toString().trim());

        BigDecimal base = selectedShowtime != null ? selectedShowtime.getBasePrice() : BigDecimal.ZERO;
        BigDecimal total = base.multiply(BigDecimal.valueOf(selectedSeats.size()));
        lblTotalPayment.setText(String.format("Q %.2f", total));
        btnCharge.setDisable(false);
    }

    @FXML
    public void onCharge(ActionEvent event) {
        if (selectedShowtime == null || selectedSeats.isEmpty()) {
            return;
        }

        Customer customer = cmbCustomer.getValue();
        if (customer == null) {
            alertInfo.viewAlert("WARNING", "SIN CLIENTE", "SELECCIONA UN CLIENTE", "Selecciona el cliente asignado a la venta.");
            return;
        }

        int successful = 0;
        List<String> failed = new ArrayList<>();
        BigDecimal finalPrice = selectedShowtime.getBasePrice();
        if (customer.isVip()) {
            finalPrice = finalPrice.multiply(new BigDecimal("0.85")).setScale(2, java.math.RoundingMode.HALF_UP);
        }

        for (Seat seat : new ArrayList<>(selectedSeats)) {
            TicketPurchaseStatus status = ticketService.purchase(
                    selectedShowtime.getIdShowtime(),
                    customer.getIdCustomer(),
                    seat.getIdSeat(),
                    finalPrice
            );

            if (status == TicketPurchaseStatus.PURCHASE_COMPLETED) {
                successful++;
            } else {
                failed.add(seat.getShortLabel());
            }
        }

        if (successful > 0) {
            BigDecimal totalCharged = finalPrice.multiply(BigDecimal.valueOf(successful));

            StringBuilder msg = new StringBuilder();
            msg.append("Se emitieron ").append(successful).append(" boleto(s) para \"")
                    .append(selectedShowtime.getMovieTitle()).append("\".\n")
                    .append("Cliente: ").append(customer.getFullName()).append("\n")
                    .append("Sala: ").append(selectedShowtime.getTheaterName()).append("\n")
                    .append(String.format("Total cobrado: Q %.2f", totalCharged));

            if (!failed.isEmpty()) {
                msg.append("\n\nOJO: no se pudieron emitir las butacas ")
                        .append(String.join(", ", failed))
                        .append(" (alguien más las compró primero). NO las cobres.");
            }

            alertInfo.viewAlert("INFORMATION", "VENTA COMPLETADA",
                    "BOLETOS EMITIDOS CON ÉXITO", msg.toString());
            selectedSeats.clear();
            selectShowtime(selectedShowtime);
        } else {
            alertInfo.viewAlert("ERROR", "ERROR EN VENTA",
                    "NO SE PUDO COMPLETAR LA TRANSACCIÓN",
                    "Los asientos seleccionados pudieron haber sido vendidos simultáneamente.");
            selectShowtime(selectedShowtime);
        }
    }

    @FXML
    public void onBack(ActionEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
