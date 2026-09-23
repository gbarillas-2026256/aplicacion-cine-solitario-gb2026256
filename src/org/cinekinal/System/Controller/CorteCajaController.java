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
import org.cinekinal.system.model.CorteDetalle;
import org.cinekinal.system.model.CorteGuardadoStatus;
import org.cinekinal.system.model.Empleado;
import org.cinekinal.system.repository.CorteCajaRepository;
import org.cinekinal.system.service.CorteCajaService;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

/**
 * Corte de caja del dia que hace un empleado al cerrar su turno.
 *
 * Las ENTRADAS no se capturan a mano: el sistema ya las conoce por la
 * tabla Boletos, asi que se precargan solas al abrir la pantalla. Lo
 * que el empleado agrega son las lineas de dulceria (combos, palomeras
 * especiales, comestibles por aparte), que no viven en ninguna tabla
 * de ventas todavia.
 *
 * Los detalles se acumulan en memoria y se guardan TODOS JUNTOS al
 * presionar "Cerrar corte" -- asi, si el empleado se equivoca en una
 * linea, puede quitarla antes de enviar nada a la base de datos.
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
    private TableView<CorteCajaRepository.EntradasPorPelicula> tablaEntradas;
    @FXML
    private TableColumn<CorteCajaRepository.EntradasPorPelicula, String> colPelicula;
    @FXML
    private TableColumn<CorteCajaRepository.EntradasPorPelicula, String> colBoletos;
    @FXML
    private TableColumn<CorteCajaRepository.EntradasPorPelicula, String> colTotalPelicula;

    @FXML
    private ComboBox<String> cmbCategoria;
    @FXML
    private TextField txtDescripcion;
    @FXML
    private TextField txtCantidad;
    @FXML
    private TextField txtPrecioUnitario;

    @FXML
    private TableView<CorteDetalle> tablaDulceria;
    @FXML
    private TableColumn<CorteDetalle, String> colCategoria;
    @FXML
    private TableColumn<CorteDetalle, String> colDescripcion;
    @FXML
    private TableColumn<CorteDetalle, String> colCantidad;
    @FXML
    private TableColumn<CorteDetalle, String> colPrecioUnitario;
    @FXML
    private TableColumn<CorteDetalle, String> colSubtotal;

    @FXML
    private TextArea txtObservaciones;

    private final CorteCajaService corteService = new CorteCajaService();
    private final AlertInformation alertInfo = new AlertInformation();

    private final List<CorteDetalle> detalles = new ArrayList<>();
    private Date fechaCorte;
    private BigDecimal totalEntradas = BigDecimal.ZERO;
    private int boletosVendidos = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!Session.esEmpleado()) {
            alertInfo.viewAlert("ERROR", "SOLO EMPLEADOS", "Acceso restringido",
                    "El corte de caja lo realiza el personal del cine.");
            new ViewFactory().viewMainMenu();
            return;
        }

        Empleado empleado = Session.getEmpleadoActual();
        if (empleado.getNivelJerarquico() == 1) {
            alertInfo.viewAlert("INFORMATION", "FUNCIÓN OPERATIVA", "Corte exclusivo de empleados de turno",
                    "El corte de caja es realizado por los empleados de taquilla/dulcería al cerrar su turno.\nComo Dueño, puedes consultar todos los cortes y reportes en 'Ver ganancias'.");
            new ViewFactory().viewMainMenu();
            return;
        }
        fechaCorte = Date.valueOf(LocalDate.now());

        lblEmpleado.setText(empleado.getNombres() + " " + empleado.getApellidos()
                + " (" + empleado.getNombrePuesto() + ")");
        lblFecha.setText(fechaCorte.toString());

        configurarTablas();
        cmbCategoria.setItems(FXCollections.observableArrayList(
                "Combo", "Palomitas", "Palomera coleccionable", "Bebidas", "Comestibles / Golosinas", "Promociones", "Otro"));
        cmbCategoria.getSelectionModel().selectFirst();

        cargarEntradasDelDia();
        actualizarTotales();
    }

    private void configurarTablas() {
        colPelicula.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitulo()));
        colBoletos.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getBoletosVendidos())));
        colTotalPelicula.setCellValueFactory(d -> new SimpleStringProperty(String.format("Q %.2f", d.getValue().getTotal())));

        colCategoria.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategoria()));
        colDescripcion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));
        colCantidad.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCantidad())));
        colPrecioUnitario.setCellValueFactory(d -> new SimpleStringProperty(String.format("Q %.2f", d.getValue().getPrecioUnitario())));
        colSubtotal.setCellValueFactory(d -> new SimpleStringProperty(String.format("Q %.2f", d.getValue().getSubtotal())));
    }

    private void cargarEntradasDelDia() {
        CorteCajaRepository.EntradasDelDia entradas = corteService.obtenerEntradasDelDia(fechaCorte);
        totalEntradas = entradas.totalEntradas;
        boletosVendidos = entradas.boletosVendidos;

        lblBoletosVendidos.setText(String.valueOf(boletosVendidos));
        tablaEntradas.setItems(FXCollections.observableArrayList(
                corteService.obtenerEntradasPorPelicula(fechaCorte)));
    }

    private BigDecimal calcularTotalDulceria() {
        BigDecimal total = BigDecimal.ZERO;
        for (CorteDetalle d : detalles) {
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

        detalles.add(new CorteDetalle(categoria, descripcion, cantidad, precio));
        limpiarFormularioLinea();
        actualizarTotales();
    }

    @FXML
    public void onQuitarLinea(ActionEvent event) {
        CorteDetalle seleccionado = tablaDulceria.getSelectionModel().getSelectedItem();
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
        Empleado empleado = Session.getEmpleadoActual();

        CorteGuardadoStatus resultado = corteService.guardarCorte(
                empleado.getIdEmpleado(), fechaCorte, totalEntradas, boletosVendidos,
                txtObservaciones.getText().trim(), detalles);

        switch (resultado) {
            case CORTE_GUARDADO -> {
                alertInfo.viewAlert("INFORMATION", "CORTE Y REPORTE ENVIADO", "Cierre de turno registrado con éxito",
                        "Empleado: " + empleado.getNombres() + " " + empleado.getApellidos() + "\n"
                        + "Fecha: " + fechaCorte + "\n"
                        + "Entradas: " + lblTotalEntradas.getText() + " (" + boletosVendidos + " boletos)\n"
                        + "Dulcería: " + lblTotalDulceria.getText() + "\n"
                        + "TOTAL GENERAL DEL DÍA: " + lblTotalGeneral.getText() + "\n\n"
                        + "El reporte de incidencias y el balance financiero ya están disponibles para el Dueño en 'Reportes' y 'Ganancias'.");
                new ViewFactory().viewMainMenu();
            }
            case YA_EXISTE_CORTE_HOY -> alertInfo.viewAlert("WARNING", "CORTE YA REALIZADO",
                    "Ya cerraste caja hoy",
                    "Solo se puede hacer un corte por día. Si necesitas modificarlo, "
                    + "pídele al Dueño que lo revise.");
            case ERROR_AL_GUARDAR -> alertInfo.viewAlert("ERROR", "ERROR AL GUARDAR",
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
