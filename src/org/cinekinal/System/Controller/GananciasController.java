package org.cinekinal.system.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.cinekinal.system.model.CashClosing;
import org.cinekinal.system.model.CashClosingDetail;
import org.cinekinal.system.repository.ReportRepository;
import org.cinekinal.system.repository.ReportRepository.ReportRow;
import org.cinekinal.system.repository.ReportRepository.TodaySummary;
import org.cinekinal.system.service.CashClosingService;
import org.cinekinal.system.utils.ViewFactory;

/**
 * Controller for cinema earnings and financial analytics.
 */
public class GananciasController implements Initializable {

    @FXML
    private Label lblIngresosHoy;
    @FXML
    private Label lblBoletosHoy;
    @FXML
    private Label lblTicketPromedio;

    // Pestaña 1: Gráficas Financieras
    @FXML
    private PieChart chartDistribucion;
    @FXML
    private BarChart<String, Number> chartPeliculas;
    @FXML
    private CategoryAxis axisPeliculaX;
    @FXML
    private NumberAxis axisPeliculaY;

    // Pestaña 2: Ingresos por Rango de Fechas
    @FXML
    private DatePicker dpInicio;
    @FXML
    private DatePicker dpFin;
    @FXML
    private Label lblTotalPeriodo;
    @FXML
    private TableView<ReportRow> tableIngresos;
    @FXML
    private TableColumn<ReportRow, String> colIngresoFecha;
    @FXML
    private TableColumn<ReportRow, String> colIngresoBoletos;
    @FXML
    private TableColumn<ReportRow, String> colIngresoTotal;

    // Pestaña 2: Ingresos por Película
    @FXML
    private TableView<ReportRow> tableIngresosPelicula;
    @FXML
    private TableColumn<ReportRow, String> colPeliculaTitulo;
    @FXML
    private TableColumn<ReportRow, String> colPeliculaBoletos;
    @FXML
    private TableColumn<ReportRow, String> colPeliculaIngresos;

    // Pestaña 3: Cortes de caja enviados por los empleados
    @FXML
    private DatePicker dpCorteInicio;
    @FXML
    private DatePicker dpCorteFin;
    @FXML
    private Label lblTotalCortes;
    @FXML
    private Label lblObservacionesCorte;
    @FXML
    private TableView<CashClosing> tableCortes;
    @FXML
    private TableColumn<CashClosing, String> colCorteFecha;
    @FXML
    private TableColumn<CashClosing, String> colCorteEmpleado;
    @FXML
    private TableColumn<CashClosing, String> colCortePuesto;
    @FXML
    private TableColumn<CashClosing, String> colCorteBoletos;
    @FXML
    private TableColumn<CashClosing, String> colCorteEntradas;
    @FXML
    private TableColumn<CashClosing, String> colCorteDulceria;
    @FXML
    private TableColumn<CashClosing, String> colCorteTotal;
    @FXML
    private TableView<CashClosingDetail> tableCorteDetalles;
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dpInicio.setValue(LocalDate.now().minusMonths(1));
        dpFin.setValue(LocalDate.now().plusDays(1));

        colIngresoFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn1()));
        colIngresoBoletos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn2()));
        colIngresoTotal.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn3()));

        colPeliculaTitulo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn1()));
        colPeliculaBoletos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn2()));
        colPeliculaIngresos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn3()));

        configurarTablaCortes();

        cargarResumenHoy();
        onFiltrarIngresos(null);
        cargarIngresosPeliculas();
        onFiltrarCortes(null);
        actualizarGraficas();
    }

    private void configurarTablaCortes() {
        dpCorteInicio.setValue(LocalDate.now().minusMonths(1));
        dpCorteFin.setValue(LocalDate.now());

        colCorteFecha.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getClosingDate())));
        colCorteEmpleado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmployeeFullName()));
        colCortePuesto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmployeePosition()));
        colCorteBoletos.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getTicketsSold())));
        colCorteEntradas.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getTotalTickets())));
        colCorteDulceria.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getTotalConcessions())));
        colCorteTotal.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getGrandTotal())));

        colDetCategoria.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory()));
        colDetDescripcion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        colDetCantidad.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));
        colDetPrecio.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getUnitPrice())));
        colDetSubtotal.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getSubtotal())));

        tableCortes.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevo) -> {
            if (nuevo == null) {
                tableCorteDetalles.getItems().clear();
                lblObservacionesCorte.setText("Observaciones: —");
                return;
            }
            tableCorteDetalles.setItems(FXCollections.observableArrayList(
                    closingService.getDetails(nuevo.getIdClosing())));
            String obsTexto = nuevo.getNotes();
            lblObservacionesCorte.setText("Observaciones: "
                    + (obsTexto == null || obsTexto.isBlank() ? "—" : obsTexto));
        });
    }

    @FXML
    public void onFiltrarCortes(ActionEvent event) {
        LocalDate ini = dpCorteInicio.getValue();
        LocalDate fin = dpCorteFin.getValue();
        if (ini == null || fin == null) {
            return;
        }

        List<CashClosing> cortes = closingService.getByDateRange(Date.valueOf(ini), Date.valueOf(fin));
        tableCortes.setItems(FXCollections.observableArrayList(cortes));

        BigDecimal total = BigDecimal.ZERO;
        for (CashClosing corte : cortes) {
            if (corte.getGrandTotal() != null) {
                total = total.add(corte.getGrandTotal());
            }
        }
        lblTotalCortes.setText(String.format("Total Cortes: Q %.2f", total));

        tableCorteDetalles.getItems().clear();
        lblObservacionesCorte.setText("Observaciones: —");
    }

    private void cargarResumenHoy() {
        TodaySummary hoy = reportRepo.getTodaySummary();
        lblIngresosHoy.setText("Q " + hoy.incomeToday);
        lblBoletosHoy.setText(hoy.ticketsToday + " boletos");

        if (hoy.ticketsToday > 0) {
            BigDecimal promedio = hoy.incomeToday.divide(BigDecimal.valueOf(hoy.ticketsToday), 2, RoundingMode.HALF_UP);
            lblTicketPromedio.setText("Q " + promedio);
        } else {
            lblTicketPromedio.setText("Q 0.00");
        }
    }

    @FXML
    public void onFiltrarIngresos(ActionEvent event) {
        LocalDate ini = dpInicio.getValue();
        LocalDate fin = dpFin.getValue();
        Date sqlIni = ini != null ? Date.valueOf(ini) : null;
        Date sqlFin = fin != null ? Date.valueOf(fin) : null;

        List<ReportRow> filas = reportRepo.getDailyIncome(sqlIni, sqlFin);
        tableIngresos.setItems(FXCollections.observableArrayList(filas));

        BigDecimal totalPeriodo = BigDecimal.ZERO;
        for (ReportRow f : filas) {
            try {
                String str = f.getColumn3().replace("Q", "").trim();
                totalPeriodo = totalPeriodo.add(new BigDecimal(str));
            } catch (Exception ignored) {}
        }
        lblTotalPeriodo.setText(String.format("Total Período: Q %.2f", totalPeriodo));
    }

    private void cargarIngresosPeliculas() {
        tableIngresosPelicula.setItems(FXCollections.observableArrayList(
                reportRepo.getMovieIncome(null, null)));
    }

    @FXML
    public void onActualizarGraficas(ActionEvent event) {
        actualizarGraficas();
    }

    private void actualizarGraficas() {
        // 1. Gráfica de distribución de ingresos (Boletos vs Dulcería)
        List<CashClosing> cortes = closingService.getByDateRange(
                Date.valueOf(LocalDate.now().minusMonths(6)),
                Date.valueOf(LocalDate.now().plusDays(1)));
        BigDecimal totalEntradas = BigDecimal.ZERO;
        BigDecimal totalDulceria = BigDecimal.ZERO;

        for (CashClosing c : cortes) {
            if (c.getTotalTickets() != null) {
                totalEntradas = totalEntradas.add(c.getTotalTickets());
            }
            if (c.getTotalConcessions() != null) {
                totalDulceria = totalDulceria.add(c.getTotalConcessions());
            }
        }

        BigDecimal totalTaquillaHistorica = BigDecimal.ZERO;
        for (ReportRow f : reportRepo.getMovieIncome(null, null)) {
            try {
                totalTaquillaHistorica = totalTaquillaHistorica.add(new BigDecimal(f.getColumn3().replace("Q", "").trim()));
            } catch (Exception ignored) {}
        }
        if (totalTaquillaHistorica.compareTo(totalEntradas) > 0) {
            totalEntradas = totalTaquillaHistorica;
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        if (totalEntradas.compareTo(BigDecimal.ZERO) > 0 || totalDulceria.compareTo(BigDecimal.ZERO) > 0) {
            pieData.add(new PieChart.Data(String.format("🎟️ Boletos (Q %.2f)", totalEntradas), totalEntradas.doubleValue()));
            pieData.add(new PieChart.Data(String.format("🍿 Dulcería (Q %.2f)", totalDulceria), totalDulceria.doubleValue()));
        } else {
            pieData.add(new PieChart.Data("Sin ventas aún", 1));
        }
        chartDistribucion.setData(pieData);

        // 2. Gráfica de recaudación por película (BarChart)
        chartPeliculas.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Recaudación (Q)");

        List<ReportRow> peliculas = reportRepo.getMovieIncome(null, null);
        for (ReportRow p : peliculas) {
            try {
                double monto = Double.parseDouble(p.getColumn3().replace("Q", "").trim());
                String titulo = p.getColumn1();
                if (titulo.length() > 18) {
                    titulo = titulo.substring(0, 16) + "..";
                }
                series.getData().add(new XYChart.Data<>(titulo, monto));
            } catch (Exception ignored) {}
        }
        chartPeliculas.getData().add(series);
    }

    @FXML
    public void onBack(ActionEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
