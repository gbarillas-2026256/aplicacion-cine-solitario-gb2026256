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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.cinekinal.system.model.CorteCaja;
import org.cinekinal.system.model.CorteDetalle;
import org.cinekinal.system.repository.ReporteRepository;
import org.cinekinal.system.repository.ReporteRepository.ReporteFila;
import org.cinekinal.system.repository.ReporteRepository.ResumenHoy;
import org.cinekinal.system.service.CorteCajaService;
import org.cinekinal.system.utils.ViewFactory;

public class ReportesController implements Initializable {

    @FXML
    private Label lblAsistenciaHoy;
    @FXML
    private Label lblFuncionesActivas;

    // Tabla Ocupación
    @FXML
    private TableView<ReporteFila> tableOcupacion;
    @FXML
    private TableColumn<ReporteFila, String> colOcupacionPelicula;
    @FXML
    private TableColumn<ReporteFila, String> colOcupacionSalaHorario;
    @FXML
    private TableColumn<ReporteFila, String> colOcupacionButacas;
    @FXML
    private TableColumn<ReporteFila, String> colOcupacionPorcentaje;

    // Tabla Asistencia
    @FXML
    private TableView<ReporteFila> tableTopAsistencia;
    @FXML
    private TableColumn<ReporteFila, String> colAsistenciaRanking;
    @FXML
    private TableColumn<ReporteFila, String> colAsistenciaBoletos;

    // Pestaña 3: Reportes de Turno e Incidencias de Caja
    @FXML
    private DatePicker dpTurnoInicio;
    @FXML
    private DatePicker dpTurnoFin;
    @FXML
    private TextField txtBuscarTurno;

    @FXML
    private TableView<CorteCaja> tableTurnos;
    @FXML
    private TableColumn<CorteCaja, String> colTurnoFecha;
    @FXML
    private TableColumn<CorteCaja, String> colTurnoRegistro;
    @FXML
    private TableColumn<CorteCaja, String> colTurnoEmpleado;
    @FXML
    private TableColumn<CorteCaja, String> colTurnoPuesto;
    @FXML
    private TableColumn<CorteCaja, String> colTurnoBoletos;
    @FXML
    private TableColumn<CorteCaja, String> colTurnoTotal;
    @FXML
    private TableColumn<CorteCaja, String> colTurnoMensajeResumen;

    @FXML
    private Label lblTurnoResponsableBadge;
    @FXML
    private TextArea txtTurnoMensajeCompleto;
    @FXML
    private Label lblTurnoEntradas;
    @FXML
    private Label lblTurnoDulceria;
    @FXML
    private Label lblTurnoTotalGeneral;

    @FXML
    private TableView<CorteDetalle> tableTurnoDetalles;
    @FXML
    private TableColumn<CorteDetalle, String> colDetCategoria;
    @FXML
    private TableColumn<CorteDetalle, String> colDetDescripcion;
    @FXML
    private TableColumn<CorteDetalle, String> colDetCantidad;
    @FXML
    private TableColumn<CorteDetalle, String> colDetPrecio;
    @FXML
    private TableColumn<CorteDetalle, String> colDetSubtotal;

    private final ReporteRepository reporteRepo = new ReporteRepository();
    private final CorteCajaService corteService = new CorteCajaService();
    private List<CorteCaja> listaTurnosActuales = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colOcupacionPelicula.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna1()));
        colOcupacionSalaHorario.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna2()));
        colOcupacionButacas.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna3()));
        colOcupacionPorcentaje.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna4()));

        colAsistenciaRanking.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna1()));
        colAsistenciaBoletos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna2()));

        configurarTablaTurnos();

        cargarDatos();
        onFiltrarTurnos(null);
    }

    private void configurarTablaTurnos() {
        dpTurnoInicio.setValue(LocalDate.now().minusMonths(1));
        dpTurnoFin.setValue(LocalDate.now().plusDays(1));

        colTurnoFecha.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getFechaCorte())));
        colTurnoRegistro.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaRegistro() != null ? d.getValue().getFechaRegistro().toString() : "—"));
        colTurnoEmpleado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmpleadoNombreCompleto()));
        colTurnoPuesto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmpleadoPuesto()));
        colTurnoBoletos.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getBoletosVendidos())));
        colTurnoTotal.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("Q %.2f", d.getValue().getTotalGeneral() != null ? d.getValue().getTotalGeneral() : BigDecimal.ZERO)));

        colTurnoMensajeResumen.setCellValueFactory(d -> {
            String obs = d.getValue().getObservaciones();
            if (obs == null || obs.isBlank()) {
                return new SimpleStringProperty("— Sin incidencias reportadas —");
            }
            obs = obs.trim().replace("\n", " ");
            return new SimpleStringProperty(obs.length() > 45 ? obs.substring(0, 42) + "..." : obs);
        });

        colDetCategoria.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategoria()));
        colDetDescripcion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));
        colDetCantidad.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCantidad())));
        colDetPrecio.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("Q %.2f", d.getValue().getPrecioUnitario() != null ? d.getValue().getPrecioUnitario() : BigDecimal.ZERO)));
        colDetSubtotal.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("Q %.2f", d.getValue().getSubtotal() != null ? d.getValue().getSubtotal() : BigDecimal.ZERO)));

        // Al seleccionar un turno, actualizar panel de incidencias y detalles
        tableTurnos.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevo) -> {
            if (nuevo == null) {
                lblTurnoResponsableBadge.setText("Empleado: —");
                txtTurnoMensajeCompleto.clear();
                lblTurnoEntradas.setText("Entradas: Q 0.00");
                lblTurnoDulceria.setText("Dulcería: Q 0.00");
                lblTurnoTotalGeneral.setText("Total Turno: Q 0.00");
                tableTurnoDetalles.getItems().clear();
                return;
            }

            lblTurnoResponsableBadge.setText("Empleado: " + nuevo.getEmpleadoNombreCompleto() + " (" + nuevo.getEmpleadoPuesto() + ")");
            String obsTexto = nuevo.getObservaciones();
            if (obsTexto != null && !obsTexto.isBlank()) {
                txtTurnoMensajeCompleto.setText(obsTexto);
            } else {
                txtTurnoMensajeCompleto.setText("(El empleado no registró notas ni incidencias en este turno. El cuadre de caja fue cerrado con normalidad).");
            }

            lblTurnoEntradas.setText(String.format("Entradas: Q %.2f", nuevo.getTotalEntradas() != null ? nuevo.getTotalEntradas() : BigDecimal.ZERO));
            lblTurnoDulceria.setText(String.format("Dulcería: Q %.2f", nuevo.getTotalDulceria() != null ? nuevo.getTotalDulceria() : BigDecimal.ZERO));
            lblTurnoTotalGeneral.setText(String.format("Total Turno: Q %.2f", nuevo.getTotalGeneral() != null ? nuevo.getTotalGeneral() : BigDecimal.ZERO));

            tableTurnoDetalles.setItems(FXCollections.observableArrayList(corteService.obtenerDetalles(nuevo.getIdCorte())));
        });

        // Búsqueda en tiempo real
        txtBuscarTurno.textProperty().addListener((observable, oldValue, newValue) -> filtrarTurnosEnMemoria(newValue));
    }

    @FXML
    public void onFiltrarTurnos(ActionEvent event) {
        LocalDate ini = dpTurnoInicio.getValue();
        LocalDate fin = dpTurnoFin.getValue();
        if (ini == null || fin == null) {
            return;
        }

        listaTurnosActuales = corteService.obtenerPorFecha(Date.valueOf(ini), Date.valueOf(fin));
        filtrarTurnosEnMemoria(txtBuscarTurno.getText());

        tableTurnoDetalles.getItems().clear();
        txtTurnoMensajeCompleto.clear();
        lblTurnoResponsableBadge.setText("Empleado: —");
        lblTurnoEntradas.setText("Entradas: Q 0.00");
        lblTurnoDulceria.setText("Dulcería: Q 0.00");
        lblTurnoTotalGeneral.setText("Total Turno: Q 0.00");
    }

    private void filtrarTurnosEnMemoria(String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            tableTurnos.setItems(FXCollections.observableArrayList(listaTurnosActuales));
            return;
        }
        String filtro = busqueda.toLowerCase().trim();
        List<CorteCaja> filtrados = new ArrayList<>();
        for (CorteCaja c : listaTurnosActuales) {
            boolean matchEmpleado = c.getEmpleadoNombreCompleto() != null && c.getEmpleadoNombreCompleto().toLowerCase().contains(filtro);
            boolean matchPuesto = c.getEmpleadoPuesto() != null && c.getEmpleadoPuesto().toLowerCase().contains(filtro);
            boolean matchObservaciones = c.getObservaciones() != null && c.getObservaciones().toLowerCase().contains(filtro);
            if (matchEmpleado || matchPuesto || matchObservaciones) {
                filtrados.add(c);
            }
        }
        tableTurnos.setItems(FXCollections.observableArrayList(filtrados));
    }

    private void cargarDatos() {
        ResumenHoy hoy = reporteRepo.obtenerResumenHoy();
        lblAsistenciaHoy.setText(hoy.boletosHoy + " espectadores");

        List<ReporteFila> ocupacion = reporteRepo.obtenerOcupacionCartelera();
        tableOcupacion.setItems(FXCollections.observableArrayList(ocupacion));
        lblFuncionesActivas.setText(ocupacion.size() + " funciones registradas");

        List<ReporteFila> topPelis = reporteRepo.obtenerTopPeliculas(null, null, 10);
        tableTopAsistencia.setItems(FXCollections.observableArrayList(topPelis));
    }

    @FXML
    public void onRefrescarOcupacion(ActionEvent event) {
        cargarDatos();
    }

    @FXML
    public void onBack(ActionEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
