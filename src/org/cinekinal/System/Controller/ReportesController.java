package org.cinekinal.system.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.cinekinal.system.repository.ReporteRepository;
import org.cinekinal.system.repository.ReporteRepository.ReporteFila;
import org.cinekinal.system.repository.ReporteRepository.ResumenHoy;
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

    private final ReporteRepository reporteRepo = new ReporteRepository();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colOcupacionPelicula.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna1()));
        colOcupacionSalaHorario.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna2()));
        colOcupacionButacas.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna3()));
        colOcupacionPorcentaje.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna4()));

        colAsistenciaRanking.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna1()));
        colAsistenciaBoletos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumna2()));

        cargarDatos();
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
