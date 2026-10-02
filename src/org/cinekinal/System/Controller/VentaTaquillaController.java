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

public class VentaTaquillaController implements Initializable {

    @FXML
    private ListView<Showtime> listFunciones;
    @FXML
    private Button btnHoy;
    @FXML
    private Button btnManana;
    @FXML
    private Button btnTodas;

    @FXML
    private Label lblSalaSeleccionada;
    @FXML
    private GridPane gridAsientos;

    @FXML
    private ComboBox<Customer> cmbCliente;
    @FXML
    private Label lblPeliculaResumen;
    @FXML
    private Label lblHorarioResumen;
    @FXML
    private Label lblAsientosResumen;
    @FXML
    private Label lblTotalPagar;
    @FXML
    private Button btnCobrar;

    private final ShowtimeRepository showtimeRepo = new ShowtimeRepository();
    private final SeatRepository seatRepo = new SeatRepository();
    private final TicketRepository ticketRepo = new TicketRepository();
    private final CustomerRepository customerRepo = new CustomerRepository();
    private final TicketService ticketService = new TicketService();
    private final AlertInformation alertInfo = new AlertInformation();

    private final Set<Seat> asientosSeleccionados = new HashSet<>();
    private Showtime funcionSeleccionada = null;
    private List<Showtime> carteleraCompleta = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarListaFunciones();
        configurarComboClientes();
        cargarCartelera();
        onFiltrarHoy(null);
    }

    private void configurarListaFunciones() {
        listFunciones.setCellFactory(lv -> new ListCell<>() {
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

        listFunciones.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                seleccionarFuncion(newVal);
            }
        });
    }

    private void configurarComboClientes() {
        Customer generico = customerRepo.getOrCreateGenericCustomer();
        List<Customer> clientes = customerRepo.getAll();

        List<Customer> comboItems = new ArrayList<>();
        if (generico != null) {
            comboItems.add(generico);
        }
        for (Customer c : clientes) {
            if (generico == null || !c.getIdCustomer().equals(generico.getIdCustomer())) {
                comboItems.add(c);
            }
        }

        cmbCliente.setItems(FXCollections.observableArrayList(comboItems));
        if (!comboItems.isEmpty()) {
            cmbCliente.getSelectionModel().selectFirst();
        }

        cmbCliente.setCellFactory(lv -> new ListCell<>() {
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

        cmbCliente.setButtonCell(new ListCell<>() {
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

    private void cargarCartelera() {
        carteleraCompleta = showtimeRepo.getBillboard();
    }

    @FXML
    public void onFiltrarHoy(ActionEvent event) {
        LocalDate hoy = LocalDate.now();
        List<Showtime> filtradas = carteleraCompleta.stream()
                .filter(f -> f.getDate() != null && f.getDate().toLocalDate().equals(hoy))
                .toList();
        listFunciones.setItems(FXCollections.observableArrayList(filtradas));
        actualizarEstiloFiltro(btnHoy);
    }

    @FXML
    public void onFiltrarManana(ActionEvent event) {
        LocalDate manana = LocalDate.now().plusDays(1);
        List<Showtime> filtradas = carteleraCompleta.stream()
                .filter(f -> f.getDate() != null && f.getDate().toLocalDate().equals(manana))
                .toList();
        listFunciones.setItems(FXCollections.observableArrayList(filtradas));
        actualizarEstiloFiltro(btnManana);
    }

    @FXML
    public void onFiltrarTodas(ActionEvent event) {
        listFunciones.setItems(FXCollections.observableArrayList(carteleraCompleta));
        actualizarEstiloFiltro(btnTodas);
    }

    private void actualizarEstiloFiltro(Button activo) {
        btnHoy.getStyleClass().removeAll("boton-filtro-activo", "boton-filtro");
        btnManana.getStyleClass().removeAll("boton-filtro-activo", "boton-filtro");
        btnTodas.getStyleClass().removeAll("boton-filtro-activo", "boton-filtro");

        btnHoy.getStyleClass().add(btnHoy == activo ? "boton-filtro-activo" : "boton-filtro");
        btnManana.getStyleClass().add(btnManana == activo ? "boton-filtro-activo" : "boton-filtro");
        btnTodas.getStyleClass().add(btnTodas == activo ? "boton-filtro-activo" : "boton-filtro");
    }

    private void seleccionarFuncion(Showtime funcion) {
        this.funcionSeleccionada = funcion;
        asientosSeleccionados.clear();

        lblPeliculaResumen.setText("Película: " + funcion.getMovieTitle());
        lblHorarioResumen.setText("Horario: " + funcion.getDate() + " " + funcion.getTime() + " (" + funcion.getTheaterName() + ")");
        lblSalaSeleccionada.setText(funcion.getTheaterName() + " · " + funcion.getTheaterType() + " · Entrada: Q" + funcion.getBasePrice());

        actualizarResumenVenta();
        renderizarMapaAsientos();
    }

    private void renderizarMapaAsientos() {
        gridAsientos.getChildren().clear();
        if (funcionSeleccionada == null) {
            return;
        }

        List<Seat> todosAsientos = seatRepo.getByTheater(funcionSeleccionada.getIdTheater());
        Set<String> ocupados = ticketRepo.getOccupiedSeats(funcionSeleccionada.getIdShowtime());

        Map<String, Integer> filaIndices = new HashMap<>();
        int indiceFila = 0;

        for (Seat asiento : todosAsientos) {
            if (!filaIndices.containsKey(asiento.getRow())) {
                filaIndices.put(asiento.getRow(), indiceFila);
                Label lblFila = new Label(asiento.getRow());
                lblFila.setStyle("-fx-text-fill: #FF6A13; -fx-font-weight: bold; -fx-padding: 0 8 0 0;");
                gridAsientos.add(lblFila, 0, indiceFila);
                indiceFila++;
            }

            int r = filaIndices.get(asiento.getRow());
            int c = asiento.getNumber();

            Button btnAsiento = new Button(asiento.getShortLabel());
            btnAsiento.getStyleClass().add("eva-seat");

            boolean estaOcupado = ocupados.contains(asiento.getIdSeat());
            if (estaOcupado) {
                btnAsiento.getStyleClass().add("eva-seat-ocupado");
                btnAsiento.setDisable(true);
            } else {
                btnAsiento.getStyleClass().add("eva-seat-libre");
                btnAsiento.setOnAction(e -> alternarSeleccionAsiento(asiento, btnAsiento));
            }

            gridAsientos.add(btnAsiento, c, r);
        }
    }

    private void alternarSeleccionAsiento(Seat asiento, Button boton) {
        if (asientosSeleccionados.contains(asiento)) {
            asientosSeleccionados.remove(asiento);
            boton.getStyleClass().remove("eva-seat-seleccionado");
            boton.getStyleClass().add("eva-seat-libre");
        } else {
            asientosSeleccionados.add(asiento);
            boton.getStyleClass().remove("eva-seat-libre");
            boton.getStyleClass().add("eva-seat-seleccionado");
        }
        actualizarResumenVenta();
    }

    private void actualizarResumenVenta() {
        if (asientosSeleccionados.isEmpty()) {
            lblAsientosResumen.setText("Butacas: Ninguna");
            lblTotalPagar.setText("Q 0.00");
            btnCobrar.setDisable(true);
            return;
        }

        StringBuilder sb = new StringBuilder("Butacas: ");
        for (Seat a : asientosSeleccionados) {
            sb.append(a.getShortLabel()).append(" ");
        }
        lblAsientosResumen.setText(sb.toString().trim());

        BigDecimal base = funcionSeleccionada != null ? funcionSeleccionada.getBasePrice() : BigDecimal.ZERO;
        BigDecimal total = base.multiply(BigDecimal.valueOf(asientosSeleccionados.size()));
        lblTotalPagar.setText(String.format("Q %.2f", total));
        btnCobrar.setDisable(false);
    }

    @FXML
    public void onCobrar(ActionEvent event) {
        if (funcionSeleccionada == null || asientosSeleccionados.isEmpty()) {
            return;
        }

        Customer cliente = cmbCliente.getValue();
        if (cliente == null) {
            alertInfo.viewAlert("WARNING", "SIN CLIENTE", "SELECCIONA UN CLIENTE", "Selecciona el cliente asignado a la venta.");
            return;
        }

        int exitosos = 0;
        List<String> fallidos = new ArrayList<>();
        BigDecimal precioFinal = funcionSeleccionada.getBasePrice();
        if (cliente.isVip()) {
            precioFinal = precioFinal.multiply(new BigDecimal("0.85")).setScale(2, java.math.RoundingMode.HALF_UP);
        }

        for (Seat asiento : new ArrayList<>(asientosSeleccionados)) {
            TicketPurchaseStatus status = ticketService.purchase(
                    funcionSeleccionada.getIdShowtime(),
                    cliente.getIdCustomer(),
                    asiento.getIdSeat(),
                    precioFinal
            );

            if (status == TicketPurchaseStatus.PURCHASE_COMPLETED) {
                exitosos++;
            } else {
                fallidos.add(asiento.getShortLabel());
            }
        }

        if (exitosos > 0) {
            //El total se calcula con los boletos que SI se emitieron, no con
            //los seleccionados: si alguno fallo, cobrar el total original
            //seria cobrarle al cliente un boleto que nunca se emitio.
            BigDecimal totalCobrado = precioFinal.multiply(BigDecimal.valueOf(exitosos));

            StringBuilder mensaje = new StringBuilder();
            mensaje.append("Se emitieron ").append(exitosos).append(" boleto(s) para \"")
                    .append(funcionSeleccionada.getMovieTitle()).append("\".\n")
                    .append("Cliente: ").append(cliente.getFullName()).append("\n")
                    .append("Sala: ").append(funcionSeleccionada.getTheaterName()).append("\n")
                    .append(String.format("Total cobrado: Q %.2f", totalCobrado));

            if (!fallidos.isEmpty()) {
                mensaje.append("\n\nOJO: no se pudieron emitir las butacas ")
                        .append(String.join(", ", fallidos))
                        .append(" (alguien más las compró primero). NO las cobres.");
            }

            alertInfo.viewAlert("INFORMATION", "VENTA COMPLETADA",
                    "BOLETOS EMITIDOS CON ÉXITO", mensaje.toString());
            asientosSeleccionados.clear();
            seleccionarFuncion(funcionSeleccionada);
        } else {
            alertInfo.viewAlert("ERROR", "ERROR EN VENTA",
                    "NO SE PUDO COMPLETAR LA TRANSACCIÓN",
                    "Los asientos seleccionados pudieron haber sido vendidos simultáneamente.");
            seleccionarFuncion(funcionSeleccionada);
        }
    }

    @FXML
    public void onBack(ActionEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
