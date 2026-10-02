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
import org.cinekinal.system.model.CashClosing;
import org.cinekinal.system.model.CashClosingDetail;
import org.cinekinal.system.repository.ReportRepository;
import org.cinekinal.system.repository.ReportRepository.ReportRow;
import org.cinekinal.system.repository.ReportRepository.TodaySummary;
import org.cinekinal.system.service.CashClosingService;
import org.cinekinal.system.utils.ViewFactory;

/**
 * Controller for cinema operational and shift reports.
 */
public class ReportesController implements Initializable {

    @FXML
    private Label lblAsistenciaHoy;
    @FXML
    private Label lblFuncionesActivas;

    // Tabla Ocupación
    @FXML
    private TableView<ReportRow> tableOcupacion;
    @FXML
    private TableColumn<ReportRow, String> colOcupacionPelicula;
    @FXML
    private TableColumn<ReportRow, String> colOcupacionSalaHorario;
    @FXML
    private TableColumn<ReportRow, String> colOcupacionButacas;
    @FXML
    private TableColumn<ReportRow, String> colOcupacionPorcentaje;

    // Tabla Asistencia
    @FXML
    private TableView<ReportRow> tableTopAsistencia;
    @FXML
    private TableColumn<ReportRow, String> colAsistenciaRanking;
    @FXML
    private TableColumn<ReportRow, String> colAsistenciaBoletos;

    // Pestaña 3: Reportes de Turno e Incidencias de Caja
    @FXML
    private DatePicker dpTurnoInicio;
    @FXML
    private DatePicker dpTurnoFin;
    @FXML
    private TextField txtBuscarTurno;

    @FXML
    private TableView<CashClosing> tableTurnos;
    @FXML
    private TableColumn<CashClosing, String> colTurnoFecha;
    @FXML
    private TableColumn<CashClosing, String> colTurnoRegistro;
    @FXML
    private TableColumn<CashClosing, String> colTurnoEmpleado;
    @FXML
    private TableColumn<CashClosing, String> colTurnoPuesto;
    @FXML
    private TableColumn<CashClosing, String> colTurnoBoletos;
    @FXML
    private TableColumn<CashClosing, String> colTurnoTotal;
    @FXML
    private TableColumn<CashClosing, String> colTurnoMensajeResumen;

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
    private TableView<CashClosingDetail> tableTurnoDetalles;
    @FXML
    private TableColumn<CashClosingDetail, String> colDetCategoria;
    @FXML
    private TableColumn<CashClosingDetail, String> colDetDescripcion;
    @FXML
    private TableColumn<CashClosingDetail, String> colDetCantidad;
    @FXML
    private TableColumn<CashClosingDetail, String> colDetPrecio;
    @FXML
    private TableColumn<CashClosingDetail, String> colDetSubtotal;

    private final ReportRepository reportRepo = new ReportRepository();
    private final CashClosingService closingService = new CashClosingService();
    private List<CashClosing> listaTurnosActuales = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colOcupacionPelicula.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn1()));
        colOcupacionSalaHorario.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn2()));
        colOcupacionButacas.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn3()));
        colOcupacionPorcentaje.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn4()));

        colAsistenciaRanking.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn1()));
        colAsistenciaBoletos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn2()));

        configurarTablaTurnos();

        cargarDatos();
        onFiltrarTurnos(null);
    }

    private void configurarTablaTurnos() {
        dpTurnoInicio.setValue(LocalDate.now().minusMonths(1));
        dpTurnoFin.setValue(LocalDate.now().plusDays(1));

        colTurnoFecha.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getClosingDate())));
        colTurnoRegistro.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCreatedAt() != null ? d.getValue().getCreatedAt().toString() : "—"));
        colTurnoEmpleado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmployeeFullName()));
        colTurnoPuesto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmployeePosition()));
        colTurnoBoletos.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getTicketsSold())));
        colTurnoTotal.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("Q %.2f", d.getValue().getGrandTotal() != null ? d.getValue().getGrandTotal() : BigDecimal.ZERO)));

        colTurnoMensajeResumen.setCellValueFactory(d -> {
            String obs = d.getValue().getNotes();
            if (obs == null || obs.isBlank()) {
                return new SimpleStringProperty("— Sin incidencias reportadas —");
            }
            obs = obs.trim().replace("\n", " ");
            return new SimpleStringProperty(obs.length() > 45 ? obs.substring(0, 42) + "..." : obs);
        });

        colDetCategoria.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory()));
        colDetDescripcion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        colDetCantidad.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));
        colDetPrecio.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("Q %.2f", d.getValue().getUnitPrice() != null ? d.getValue().getUnitPrice() : BigDecimal.ZERO)));
        colDetSubtotal.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("Q %.2f", d.getValue().getSubtotal() != null ? d.getValue().getSubtotal() : BigDecimal.ZERO)));

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

            lblTurnoResponsableBadge.setText("Empleado: " + nuevo.getEmployeeFullName() + " (" + nuevo.getEmployeePosition() + ")");
            String obsTexto = nuevo.getNotes();
            if (obsTexto != null && !obsTexto.isBlank()) {
                txtTurnoMensajeCompleto.setText(obsTexto);
            } else {
                txtTurnoMensajeCompleto.setText("(El empleado no registró notas ni incidencias en este turno. El cuadre de caja fue cerrado con normalidad).");
            }

            lblTurnoEntradas.setText(String.format("Entradas: Q %.2f", nuevo.getTotalTickets() != null ? nuevo.getTotalTickets() : BigDecimal.ZERO));
            lblTurnoDulceria.setText(String.format("Dulcería: Q %.2f", nuevo.getTotalConcessions() != null ? nuevo.getTotalConcessions() : BigDecimal.ZERO));
            lblTurnoTotalGeneral.setText(String.format("Total Turno: Q %.2f", nuevo.getGrandTotal() != null ? nuevo.getGrandTotal() : BigDecimal.ZERO));

            tableTurnoDetalles.setItems(FXCollections.observableArrayList(closingService.getDetails(nuevo.getIdClosing())));
        });

        txtBuscarTurno.textProperty().addListener((observable, oldValue, newValue) -> filtrarTurnosEnMemoria(newValue));
    }

    @FXML
    public void onFiltrarTurnos(ActionEvent event) {
        LocalDate ini = dpTurnoInicio.getValue();
        LocalDate fin = dpTurnoFin.getValue();
        if (ini == null || fin == null) {
            return;
        }

        listaTurnosActuales = closingService.getByDateRange(Date.valueOf(ini), Date.valueOf(fin));
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
        List<CashClosing> filtrados = new ArrayList<>();
        for (CashClosing c : listaTurnosActuales) {
            boolean matchEmpleado = c.getEmployeeFullName() != null && c.getEmployeeFullName().toLowerCase().contains(filtro);
            boolean matchPuesto = c.getEmployeePosition() != null && c.getEmployeePosition().toLowerCase().contains(filtro);
            boolean matchObservaciones = c.getNotes() != null && c.getNotes().toLowerCase().contains(filtro);
            if (matchEmpleado || matchPuesto || matchObservaciones) {
                filtrados.add(c);
            }
        }
        tableTurnos.setItems(FXCollections.observableArrayList(filtrados));
    }

    private void cargarDatos() {
        TodaySummary hoy = reportRepo.getTodaySummary();
        lblAsistenciaHoy.setText(hoy.ticketsToday + " espectadores");

        List<ReportRow> ocupacion = reportRepo.getBillboardOccupancy();
        tableOcupacion.setItems(FXCollections.observableArrayList(ocupacion));
        lblFuncionesActivas.setText(ocupacion.size() + " funciones registradas");

        List<ReportRow> topPelis = reportRepo.getTopMovies(null, null, 10);
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
