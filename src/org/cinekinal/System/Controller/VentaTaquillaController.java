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
import org.cinekinal.system.model.Asiento;
import org.cinekinal.system.model.BoletoCompraStatus;
import org.cinekinal.system.model.Cliente;
import org.cinekinal.system.model.Funcion;
import org.cinekinal.system.repository.AsientoRepository;
import org.cinekinal.system.repository.BoletoRepository;
import org.cinekinal.system.repository.ClienteRepository;
import org.cinekinal.system.repository.FuncionRepository;
import org.cinekinal.system.service.BoletoService;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.ViewFactory;

public class VentaTaquillaController implements Initializable {

    @FXML
    private ListView<Funcion> listFunciones;
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
    private ComboBox<Cliente> cmbCliente;
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

    private final FuncionRepository funcionRepo = new FuncionRepository();
    private final AsientoRepository asientoRepo = new AsientoRepository();
    private final BoletoRepository boletoRepo = new BoletoRepository();
    private final ClienteRepository clienteRepo = new ClienteRepository();
    private final BoletoService boletoService = new BoletoService();
    private final AlertInformation alertInfo = new AlertInformation();

    private final Set<Asiento> asientosSeleccionados = new HashSet<>();
    private Funcion funcionSeleccionada = null;
    private List<Funcion> carteleraCompleta = new ArrayList<>();

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
            protected void updateItem(Funcion item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getTituloPelicula() + "\n"
                            + item.getNombreSala() + " · " + item.getFecha() + " " + item.getHora()
                            + " · Q" + item.getPrecioBase());
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
        Cliente generico = clienteRepo.obtenerOcrearClienteGenerico();
        List<Cliente> clientes = clienteRepo.obtenerTodos();

        List<Cliente> comboItems = new ArrayList<>();
        if (generico != null) {
            comboItems.add(generico);
        }
        for (Cliente c : clientes) {
            if (generico == null || !c.getIdCliente().equals(generico.getIdCliente())) {
                comboItems.add(c);
            }
        }

        cmbCliente.setItems(FXCollections.observableArrayList(comboItems));
        if (!comboItems.isEmpty()) {
            cmbCliente.getSelectionModel().selectFirst();
        }

        cmbCliente.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Cliente item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombreCompleto() + (item.isEsVip() ? " [VIP]" : "") + " (" + item.getCorreo() + ")");
                }
            }
        });

        cmbCliente.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Cliente item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombreCompleto() + (item.isEsVip() ? " [VIP]" : ""));
                }
            }
        });
    }

    private void cargarCartelera() {
        carteleraCompleta = funcionRepo.obtenerCartelera();
    }

    @FXML
    public void onFiltrarHoy(ActionEvent event) {
        LocalDate hoy = LocalDate.now();
        List<Funcion> filtradas = carteleraCompleta.stream()
                .filter(f -> f.getFecha() != null && f.getFecha().toLocalDate().equals(hoy))
                .toList();
        listFunciones.setItems(FXCollections.observableArrayList(filtradas));
        actualizarEstiloFiltro(btnHoy);
    }

    @FXML
    public void onFiltrarManana(ActionEvent event) {
        LocalDate manana = LocalDate.now().plusDays(1);
        List<Funcion> filtradas = carteleraCompleta.stream()
                .filter(f -> f.getFecha() != null && f.getFecha().toLocalDate().equals(manana))
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

    private void seleccionarFuncion(Funcion funcion) {
        this.funcionSeleccionada = funcion;
        asientosSeleccionados.clear();

        lblPeliculaResumen.setText("Película: " + funcion.getTituloPelicula());
        lblHorarioResumen.setText("Horario: " + funcion.getFecha() + " " + funcion.getHora() + " (" + funcion.getNombreSala() + ")");
        lblSalaSeleccionada.setText(funcion.getNombreSala() + " · " + funcion.getTipoSala() + " · Entrada: Q" + funcion.getPrecioBase());

        actualizarResumenVenta();
        renderizarMapaAsientos();
    }

    private void renderizarMapaAsientos() {
        gridAsientos.getChildren().clear();
        if (funcionSeleccionada == null) {
            return;
        }

        List<Asiento> todosAsientos = asientoRepo.obtenerPorSala(funcionSeleccionada.getIdSala());
        Set<String> ocupados = boletoRepo.obtenerAsientosOcupados(funcionSeleccionada.getIdFuncion());

        Map<String, Integer> filaIndices = new HashMap<>();
        int indiceFila = 0;

        for (Asiento asiento : todosAsientos) {
            if (!filaIndices.containsKey(asiento.getFila())) {
                filaIndices.put(asiento.getFila(), indiceFila);
                Label lblFila = new Label(asiento.getFila());
                lblFila.setStyle("-fx-text-fill: #FF6A13; -fx-font-weight: bold; -fx-padding: 0 8 0 0;");
                gridAsientos.add(lblFila, 0, indiceFila);
                indiceFila++;
            }

            int r = filaIndices.get(asiento.getFila());
            int c = asiento.getNumero();

            Button btnAsiento = new Button(asiento.getFila() + asiento.getNumero());
            btnAsiento.getStyleClass().add("eva-seat");

            boolean estaOcupado = ocupados.contains(asiento.getIdAsiento());
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

    private void alternarSeleccionAsiento(Asiento asiento, Button boton) {
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
        for (Asiento a : asientosSeleccionados) {
            sb.append(a.getFila()).append(a.getNumero()).append(" ");
        }
        lblAsientosResumen.setText(sb.toString().trim());

        BigDecimal base = funcionSeleccionada != null ? funcionSeleccionada.getPrecioBase() : BigDecimal.ZERO;
        BigDecimal total = base.multiply(BigDecimal.valueOf(asientosSeleccionados.size()));
        lblTotalPagar.setText(String.format("Q %.2f", total));
        btnCobrar.setDisable(false);
    }

    @FXML
    public void onCobrar(ActionEvent event) {
        if (funcionSeleccionada == null || asientosSeleccionados.isEmpty()) {
            return;
        }

        Cliente cliente = cmbCliente.getValue();
        if (cliente == null) {
            alertInfo.viewAlert("WARNING", "SIN CLIENTE", "SELECCIONA UN CLIENTE", "Selecciona el cliente asignado a la venta.");
            return;
        }

        int exitosos = 0;
        BigDecimal precioFinal = funcionSeleccionada.getPrecioBase();
        if (cliente.isEsVip()) {
            precioFinal = precioFinal.multiply(new BigDecimal("0.85")).setScale(2, java.math.RoundingMode.HALF_UP);
        }

        for (Asiento asiento : new ArrayList<>(asientosSeleccionados)) {
            BoletoCompraStatus status = boletoService.comprar(
                    funcionSeleccionada.getIdFuncion(),
                    cliente.getIdCliente(),
                    asiento.getIdAsiento(),
                    precioFinal
            );

            if (status == BoletoCompraStatus.COMPRA_EXITOSA) {
                exitosos++;
            }
        }

        if (exitosos > 0) {
            alertInfo.viewAlert("INFORMATION", "VENTA COMPLETADA",
                    "BOLETOS EMITIDOS CON ÉXITO",
                    "Se emitieron " + exitosos + " boleto(s) para \"" + funcionSeleccionada.getTituloPelicula() + "\".\n"
                            + "Cliente: " + cliente.getNombreCompleto() + "\n"
                            + "Sala: " + funcionSeleccionada.getNombreSala() + "\n"
                            + "Total cobrado: " + lblTotalPagar.getText());
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
