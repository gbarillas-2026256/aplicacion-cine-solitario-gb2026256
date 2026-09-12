package org.cinekinal.system.controller;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
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

public class ReportesGananciasController implements Initializable {

    @FXML
    private Label lblBoletosHoy;
    @FXML
    private Label lblIngresosHoy;

    // Pestaña 1: Ingresos por Día
    @FXML
    private DatePicker dpInicio;
    @FXML
    private DatePicker dpFin;
    @FXML
    private TableView<ReporteFila> tableIngresos;
    @FXML
    private TableColumn<ReporteFila, String> colIngresoFecha;
    @FXML
    private TableColumn<ReporteFila, String> colIngresoBoletos;
    @FXML
    private TableColumn<ReporteFila, String> colIngresoTotal;

    // Pestaña 2: Top Películas
    @FXML
    private TableView<ReporteFila> tableTopPeliculas;
    @FXML
    private TableColumn<ReporteFila, String> colTopRanking;
    @FXML
    private TableColumn<ReporteFila, String> colTopBoletos;
    @FXML
    private TableColumn<ReporteFila, String> colTopIngresos;

    // Pestaña 3: Ocupación
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

    private final ReporteRepository reporteRepo = new ReporteRepository();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dpInicio.setValue(LocalDate.now().minusMonths(1));
        dpFin.setValue(LocalDate.now().plusDays(1));

        colIngresoFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna1()));
        colIngresoBoletos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna2()));
        colIngresoTotal.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna3()));

        colTopRanking.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna1()));
        colTopBoletos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna2()));
        colTopIngresos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna3()));

        colOcupacionPelicula.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna1()));
        colOcupacionSalaHorario.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna2()));
        colOcupacionButacas.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna3()));
        colOcupacionPorcentaje.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna4()));

        cargarResumenHoy();
        onFiltrarIngresos(null);
        cargarTopPeliculas();
        cargarOcupacion();
    }

    private void cargarResumenHoy() {
        ResumenHoy hoy = reporteRepo.obtenerResumenHoy();
        lblBoletosHoy.setText(String.valueOf(hoy.boletosHoy));
        lblIngresosHoy.setText("Q " + hoy.ingresosHoy);
    }

    @FXML
    public void onFiltrarIngresos(ActionEvent event) {
        LocalDate ini = dpInicio.getValue();
        LocalDate fin = dpFin.getValue();
        Date sqlIni = ini != null ? Date.valueOf(ini) : null;
        Date sqlFin = fin != null ? Date.valueOf(fin) : null;

        tableIngresos.setItems(FXCollections.observableArrayList(reporteRepo.obtenerIngresosPorDia(sqlIni, sqlFin)));
    }

    private void cargarTopPeliculas() {
        tableTopPeliculas.setItems(FXCollections.observableArrayList(reporteRepo.obtenerTopPeliculas(null, null, 10)));
    }

    private void cargarOcupacion() {
        tableOcupacion.setItems(FXCollections.observableArrayList(reporteRepo.obtenerOcupacionCartelera()));
    }

    @FXML
    public void onBack(ActionEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
