package org.cinekinal.system.controller;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.cinekinal.system.model.CashClosingDetail;
import org.cinekinal.system.model.CashClosingSaveStatus;
import org.cinekinal.system.model.Employee;
import org.cinekinal.system.repository.CashClosingRepository;
import org.cinekinal.system.service.CashClosingService;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

/**
 * Cash closing controller for cashier employees at shift end.
 */
public class CorteCajaController implements Initializable {

    @FXML
    private Label lblEmpleado;
    @FXML
    private Label lblFecha;
    @FXML
    private Label lblBoletosVendidos;
    @FXML
    private Label lblTotalEntradas;
    @FXML
    private Label lblTotalDulceria;
    @FXML
    private Label lblTotalGeneral;

    @FXML
    private TableView<CashClosingRepository.TicketsPerMovie> tablaEntradas;
    @FXML
    private TableColumn<CashClosingRepository.TicketsPerMovie, String> colPelicula;
    @FXML
    private TableColumn<CashClosingRepository.TicketsPerMovie, String> colBoletos;
    @FXML
    private TableColumn<CashClosingRepository.TicketsPerMovie, String> colTotalPelicula;

    @FXML
    private ComboBox<String> cmbCategoria;
    @FXML
    private TextField txtDescripcion;
    @FXML
    private TextField txtCantidad;
    @FXML
    private TextField txtPrecioUnitario;

    @FXML
    private TableView<CashClosingDetail> tablaDulceria;
    @FXML
    private TableColumn<CashClosingDetail, String> colCategoria;
    @FXML
    private TableColumn<CashClosingDetail, String> colDescripcion;
    @FXML
    private TableColumn<CashClosingDetail, String> colCantidad;
    @FXML
    private TableColumn<CashClosingDetail, String> colPrecioUnitario;
    @FXML
    private TableColumn<CashClosingDetail, String> colSubtotal;

    @FXML
    private TextArea txtObservaciones;

    private final CashClosingService closingService = new CashClosingService();
    private final AlertInformation alertInfo = new AlertInformation();

    private final List<CashClosingDetail> detalles = new ArrayList<>();
    private Date fechaCorte;
    private BigDecimal totalEntradas = BigDecimal.ZERO;
    private int boletosVendidos = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!Session.isEmployee()) {
            alertInfo.viewAlert("ERROR", "SOLO EMPLEADOS", "Acceso restringido",
                    "El corte de caja lo realiza el personal del cine.");
            new ViewFactory().viewMainMenu();
            return;
        }

        Employee empleado = Session.getCurrentEmployee();
        if (empleado.getHierarchyLevel() == 1) {
            alertInfo.viewAlert("INFORMATION", "FUNCIÓN OPERATIVA", "Corte exclusivo de empleados de turno",
                    "El corte de caja es realizado por los empleados de taquilla/dulcería al cerrar su turno.\nComo Dueño, puedes consultar todos los cortes y reportes en 'Ver ganancias'.");
            new ViewFactory().viewMainMenu();
            return;
        }
        fechaCorte = Date.valueOf(LocalDate.now());

        lblEmpleado.setText(empleado.getFirstName() + " " + empleado.getLastName()
                + " (" + empleado.getPositionName() + ")");
        lblFecha.setText(fechaCorte.toString());

        configurarTablas();
        cmbCategoria.setItems(FXCollections.observableArrayList(
                "Combo", "Palomitas", "Palomera coleccionable", "Bebidas", "Comestibles / Golosinas", "Promociones", "Otro"));
        cmbCategoria.getSelectionModel().selectFirst();

        cargarEntradasDelDia();
        actualizarTotales();
    }

    private void configurarTablas() {
        colPelicula.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        colBoletos.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getTicketsSold())));
        colTotalPelicula.setCellValueFactory(d -> new SimpleStringProperty(String.format("Q %.2f", d.getValue().getTotal())));

        colCategoria.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory()));
        colDescripcion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        colCantidad.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));
        colPrecioUnitario.setCellValueFactory(d -> new SimpleStringProperty(String.format("Q %.2f", d.getValue().getUnitPrice())));
        colSubtotal.setCellValueFactory(d -> new SimpleStringProperty(String.format("Q %.2f", d.getValue().getSubtotal())));
    }

    private void cargarEntradasDelDia() {
        CashClosingRepository.DailyTicketsSummary entradas = closingService.getDailyTicketsSummary(fechaCorte);
        totalEntradas = entradas.totalTickets;
        boletosVendidos = entradas.ticketsSold;

        lblBoletosVendidos.setText(String.valueOf(boletosVendidos));
        tablaEntradas.setItems(FXCollections.observableArrayList(
                closingService.getTicketsPerMovie(fechaCorte)));
    }

    private BigDecimal calcularTotalDulceria() {
        BigDecimal total = BigDecimal.ZERO;
        for (CashClosingDetail d : detalles) {
            total = total.add(d.getSubtotal());
        }
        return total;
    }

    private void actualizarTotales() {
        BigDecimal dulceria = calcularTotalDulceria();
        lblTotalEntradas.setText(String.format("Q %.2f", totalEntradas));
        lblTotalDulceria.setText(String.format("Q %.2f", dulceria));
        lblTotalGeneral.setText(String.format("Q %.2f", totalEntradas.add(dulceria)));
        tablaDulceria.setItems(FXCollections.observableArrayList(detalles));
    }

    @FXML
    public void onAgregarLinea(ActionEvent event) {
        String categoria = cmbCategoria.getValue();
        String descripcion = txtDescripcion.getText().trim();
        String cantidadTexto = txtCantidad.getText().trim();
        String precioTexto = txtPrecioUnitario.getText().trim();

        if (descripcion.isEmpty() || cantidadTexto.isEmpty() || precioTexto.isEmpty()) {
            alertInfo.viewAlert("WARNING", "CAMPOS INCOMPLETOS", "Faltan datos",
                    "Completa descripción, cantidad y precio unitario.");
            return;
        }

        int cantidad;
        BigDecimal precio;
        try {
            cantidad = Integer.parseInt(cantidadTexto);
            precio = new BigDecimal(precioTexto);
        } catch (NumberFormatException e) {
            alertInfo.viewAlert("WARNING", "DATOS INVÁLIDOS", "Revisa los números",
                    "La cantidad debe ser un número entero y el precio un número (ej: 25.50).");
            return;
        }

        if (cantidad <= 0 || precio.compareTo(BigDecimal.ZERO) <= 0) {
            alertInfo.viewAlert("WARNING", "DATOS INVÁLIDOS", "Valores fuera de rango",
                    "La cantidad y el precio deben ser mayores a cero.");
            return;
        }

        detalles.add(new CashClosingDetail(categoria, descripcion, cantidad, precio));
        limpiarFormularioLinea();
        actualizarTotales();
    }

    @FXML
    public void onQuitarLinea(ActionEvent event) {
        CashClosingDetail seleccionado = tablaDulceria.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alertInfo.viewAlert("WARNING", "SIN SELECCIÓN", "Ninguna línea seleccionada",
                    "Selecciona una línea de la tabla para quitarla.");
            return;
        }
        detalles.remove(seleccionado);
        actualizarTotales();
    }

    @FXML
    public void onCerrarCorte(ActionEvent event) {
        Employee empleado = Session.getCurrentEmployee();

        CashClosingSaveStatus resultado = closingService.saveClosing(
                empleado.getIdEmployee(), fechaCorte, totalEntradas, boletosVendidos,
                txtObservaciones.getText().trim(), detalles);

        switch (resultado) {
            case SAVED -> {
                alertInfo.viewAlert("INFORMATION", "CORTE Y REPORTE ENVIADO", "Cierre de turno registrado con éxito",
                        "Empleado: " + empleado.getFirstName() + " " + empleado.getLastName() + "\n"
                        + "Fecha: " + fechaCorte + "\n"
                        + "Entradas: " + lblTotalEntradas.getText() + " (" + boletosVendidos + " boletos)\n"
                        + "Dulcería: " + lblTotalDulceria.getText() + "\n"
                        + "TOTAL GENERAL DEL DÍA: " + lblTotalGeneral.getText() + "\n\n"
                        + "El reporte de incidencias y el balance financiero ya están disponibles para el Dueño en 'Reportes' y 'Ganancias'.");
                new ViewFactory().viewMainMenu();
            }
            case ALREADY_EXISTS_TODAY -> alertInfo.viewAlert("WARNING", "CORTE YA REALIZADO",
                    "Ya cerraste caja hoy",
                    "Solo se puede hacer un corte por día. Si necesitas modificarlo, "
                    + "pídele al Dueño que lo revise.");
            case SAVE_ERROR -> alertInfo.viewAlert("ERROR", "ERROR AL GUARDAR",
                    "No se pudo guardar el corte",
                    "Ocurrió un error al guardar. Verifica la conexión a la base de datos.");
        }
    }

    private void limpiarFormularioLinea() {
        txtDescripcion.clear();
        txtCantidad.clear();
        txtPrecioUnitario.clear();
        cmbCategoria.getSelectionModel().selectFirst();
    }

    @FXML
    public void onVolver(ActionEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
