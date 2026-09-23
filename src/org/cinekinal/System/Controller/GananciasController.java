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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import org.cinekinal.system.model.CorteCaja;
import org.cinekinal.system.model.CorteDetalle;
import org.cinekinal.system.repository.ReporteRepository;
import org.cinekinal.system.repository.ReporteRepository.ReporteFila;
import org.cinekinal.system.repository.ReporteRepository.ResumenHoy;
import org.cinekinal.system.service.CorteCajaService;
import org.cinekinal.system.utils.ViewFactory;

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
    private TableView<ReporteFila> tableIngresos;
    @FXML
    private TableColumn<ReporteFila, String> colIngresoFecha;
    @FXML
    private TableColumn<ReporteFila, String> colIngresoBoletos;
    @FXML
    private TableColumn<ReporteFila, String> colIngresoTotal;

    // Pestaña 2: Ingresos por Película
    @FXML
    private TableView<ReporteFila> tableIngresosPelicula;
    @FXML
    private TableColumn<ReporteFila, String> colPeliculaTitulo;
    @FXML
    private TableColumn<ReporteFila, String> colPeliculaBoletos;
    @FXML
    private TableColumn<ReporteFila, String> colPeliculaIngresos;

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
    private TableView<CorteCaja> tableCortes;
    @FXML
    private TableColumn<CorteCaja, String> colCorteFecha;
    @FXML
    private TableColumn<CorteCaja, String> colCorteEmpleado;
    @FXML
    private TableColumn<CorteCaja, String> colCortePuesto;
    @FXML
    private TableColumn<CorteCaja, String> colCorteBoletos;
    @FXML
    private TableColumn<CorteCaja, String> colCorteEntradas;
    @FXML
    private TableColumn<CorteCaja, String> colCorteDulceria;
    @FXML
    private TableColumn<CorteCaja, String> colCorteTotal;
    @FXML
    private TableView<CorteDetalle> tableCorteDetalles;
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dpInicio.setValue(LocalDate.now().minusMonths(1));
        dpFin.setValue(LocalDate.now().plusDays(1));

        colIngresoFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna1()));
        colIngresoBoletos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna2()));
        colIngresoTotal.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna3()));

        colPeliculaTitulo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna1()));
        colPeliculaBoletos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna2()));
        colPeliculaIngresos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna3()));

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

        colCorteFecha.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getFechaCorte())));
        colCorteEmpleado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmpleadoNombreCompleto()));
        colCortePuesto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmpleadoPuesto()));
        colCorteBoletos.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getBoletosVendidos())));
        colCorteEntradas.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getTotalEntradas())));
        colCorteDulceria.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getTotalDulceria())));
        colCorteTotal.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getTotalGeneral())));

        colDetCategoria.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategoria()));
        colDetDescripcion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));
        colDetCantidad.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCantidad())));
        colDetPrecio.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getPrecioUnitario())));
        colDetSubtotal.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getSubtotal())));

        //Al seleccionar un corte, se carga su desglose de dulceria abajo
        tableCortes.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevo) -> {
            if (nuevo == null) {
                tableCorteDetalles.getItems().clear();
                lblObservacionesCorte.setText("Observaciones: —");
                return;
            }
            tableCorteDetalles.setItems(FXCollections.observableArrayList(
                    corteService.obtenerDetalles(nuevo.getIdCorte())));
            String obsTexto = nuevo.getObservaciones();
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

        List<CorteCaja> cortes = corteService.obtenerPorFecha(Date.valueOf(ini), Date.valueOf(fin));
        tableCortes.setItems(FXCollections.observableArrayList(cortes));

        //Se suma directo del BigDecimal, no parseando texto de la tabla
        BigDecimal total = BigDecimal.ZERO;
        for (CorteCaja corte : cortes) {
            if (corte.getTotalGeneral() != null) {
                total = total.add(corte.getTotalGeneral());
            }
        }
        lblTotalCortes.setText(String.format("Total Cortes: Q %.2f", total));

        tableCorteDetalles.getItems().clear();
        lblObservacionesCorte.setText("Observaciones: —");
    }

    private void cargarResumenHoy() {
        ResumenHoy hoy = reporteRepo.obtenerResumenHoy();
        lblIngresosHoy.setText("Q " + hoy.ingresosHoy);
        lblBoletosHoy.setText(hoy.boletosHoy + " boletos");

        if (hoy.boletosHoy > 0) {
            BigDecimal promedio = hoy.ingresosHoy.divide(BigDecimal.valueOf(hoy.boletosHoy), 2, RoundingMode.HALF_UP);
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

        List<ReporteFila> filas = reporteRepo.obtenerIngresosPorDia(sqlIni, sqlFin);
        tableIngresos.setItems(FXCollections.observableArrayList(filas));

        BigDecimal totalPeriodo = BigDecimal.ZERO;
        for (ReporteFila f : filas) {
            try {
                String str = f.getColumna3().replace("Q", "").trim();
                totalPeriodo = totalPeriodo.add(new BigDecimal(str));
            } catch (Exception ignored) {}
        }
        lblTotalPeriodo.setText(String.format("Total Período: Q %.2f", totalPeriodo));
    }

    private void cargarIngresosPeliculas() {
        tableIngresosPelicula.setItems(FXCollections.observableArrayList(
                reporteRepo.obtenerIngresosPorPelicula(null, null)));
    }

    @FXML
    public void onActualizarGraficas(ActionEvent event) {
        actualizarGraficas();
    }

    private void actualizarGraficas() {
        // 1. Gráfica de distribución de ingresos (Boletos vs Dulcería)
        List<CorteCaja> cortes = corteService.obtenerPorFecha(
                Date.valueOf(LocalDate.now().minusMonths(6)),
                Date.valueOf(LocalDate.now().plusDays(1)));
        BigDecimal totalEntradas = BigDecimal.ZERO;
        BigDecimal totalDulceria = BigDecimal.ZERO;

        for (CorteCaja c : cortes) {
            if (c.getTotalEntradas() != null) {
                totalEntradas = totalEntradas.add(c.getTotalEntradas());
            }
            if (c.getTotalDulceria() != null) {
                totalDulceria = totalDulceria.add(c.getTotalDulceria());
            }
        }

        // Si aún no hay cortes o para sumar todas las entradas de taquilla registradas
        BigDecimal totalTaquillaHistorica = BigDecimal.ZERO;
        for (ReporteFila f : reporteRepo.obtenerIngresosPorPelicula(null, null)) {
            try {
                totalTaquillaHistorica = totalTaquillaHistorica.add(new BigDecimal(f.getColumna3().replace("Q", "").trim()));
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

        List<ReporteFila> peliculas = reporteRepo.obtenerIngresosPorPelicula(null, null);
        for (ReporteFila p : peliculas) {
            try {
                double monto = Double.parseDouble(p.getColumna3().replace("Q", "").trim());
                String titulo = p.getColumna1();
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
