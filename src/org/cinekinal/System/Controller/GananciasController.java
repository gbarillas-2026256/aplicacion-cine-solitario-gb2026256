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
import org.cinekinal.system.repository.ReporteRepository;
import org.cinekinal.system.repository.ReporteRepository.ReporteFila;
import org.cinekinal.system.repository.ReporteRepository.ResumenHoy;
import org.cinekinal.system.utils.ViewFactory;

public class GananciasController implements Initializable {

    @FXML
    private Label lblIngresosHoy;
    @FXML
    private Label lblBoletosHoy;
    @FXML
    private Label lblTicketPromedio;

    // Pestaña 1: Ingresos por Rango de Fechas
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

    private final ReporteRepository reporteRepo = new ReporteRepository();

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

        cargarResumenHoy();
        onFiltrarIngresos(null);
        cargarIngresosPeliculas();
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
    public void onBack(ActionEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
