package org.cinekinal.system.controller;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.cinekinal.system.model.Empleado;
import org.cinekinal.system.model.Funcion;
import org.cinekinal.system.repository.FuncionRepository;
import org.cinekinal.system.repository.SolicitudRepository;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

public class CambiarPrecioController implements Initializable {

    @FXML
    private TableView<Funcion> tableFunciones;
    @FXML
    private TableColumn<Funcion, String> colPelicula;
    @FXML
    private TableColumn<Funcion, String> colSala;
    @FXML
    private TableColumn<Funcion, String> colFecha;
    @FXML
    private TableColumn<Funcion, String> colHora;
    @FXML
    private TableColumn<Funcion, String> colPrecioActual;

    @FXML
    private Label lblPeliculaSel;
    @FXML
    private Label lblHorarioSel;
    @FXML
    private Label lblPrecioBaseSel;
    @FXML
    private TextField txtNuevoPrecio;
    @FXML
    private Label lblNotaPermiso;
    @FXML
    private Button btnAplicarPrecio;

    private final FuncionRepository funcionRepo = new FuncionRepository();
    private final SolicitudRepository solicitudRepo = new SolicitudRepository();
    private final AlertInformation alertInfo = new AlertInformation();
    private Funcion funcionSeleccionada = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colPelicula.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTituloPelicula()));
        colSala.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreSala()));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha() != null ? d.getValue().getFecha().toString() : "—"));
        colHora.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHora() != null ? d.getValue().getHora().toString() : "—"));
        colPrecioActual.setCellValueFactory(d -> new SimpleStringProperty("Q " + d.getValue().getPrecioBase()));

        tableFunciones.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                funcionSeleccionada = newVal;
                lblPeliculaSel.setText("Película: " + newVal.getTituloPelicula());
                lblHorarioSel.setText("Horario: " + newVal.getFecha() + " " + newVal.getHora() + " (" + newVal.getNombreSala() + ")");
                lblPrecioBaseSel.setText("Precio actual: Q " + newVal.getPrecioBase());
                txtNuevoPrecio.setText(newVal.getPrecioBase().toString());
                btnAplicarPrecio.setDisable(false);
            } else {
                funcionSeleccionada = null;
                lblPeliculaSel.setText("Película: —");
                lblHorarioSel.setText("Horario: —");
                lblPrecioBaseSel.setText("Precio actual: —");
                txtNuevoPrecio.clear();
                btnAplicarPrecio.setDisable(true);
            }
        });

        Empleado emp = Session.getEmpleadoActual();
        if (emp != null && emp.getNivelJerarquico() == 1) {
            lblNotaPermiso.setText("Rol: Dueño (nivel 1). Los cambios de precio se aplican inmediatamente.");
            btnAplicarPrecio.setText("APLICAR PRECIO DIRECTO");
        } else {
            lblNotaPermiso.setText("Rol: Gerente (nivel 2). Este cambio generará una solicitud de aprobación para el Dueño.");
            btnAplicarPrecio.setText("ENVIAR SOLICITUD DE CAMBIO");
        }

        cargarTabla();
    }

    private void cargarTabla() {
        tableFunciones.setItems(FXCollections.observableArrayList(funcionRepo.obtenerCartelera()));
    }

    @FXML
    public void onAplicarPrecio(ActionEvent event) {
        if (funcionSeleccionada == null) {
            return;
        }

        String precioStr = txtNuevoPrecio.getText().trim();
        BigDecimal nuevoPrecio;
        try {
            nuevoPrecio = new BigDecimal(precioStr);
            if (nuevoPrecio.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            alertInfo.viewAlert("WARNING", "PRECIO INVÁLIDO", "ERROR DE PRECIO",
                    "Ingresa una cantidad válida mayor a 0 (ej. 35.00 o 50.00).");
            return;
        }

        Empleado emp = Session.getEmpleadoActual();
        if (emp != null && emp.getNivelJerarquico() == 1) {
            // Dueño: aplica directo
            boolean ok = funcionRepo.actualizarPrecioBase(funcionSeleccionada.getIdFuncion(), nuevoPrecio);
            if (ok) {
                cargarTabla();
                alertInfo.viewAlert("INFORMATION", "PRECIO ACTUALIZADO", "CAMBIO APLICADO",
                        "El precio de \"" + funcionSeleccionada.getTituloPelicula() + "\" ha sido fijado en Q " + nuevoPrecio + ".");
            } else {
                alertInfo.viewAlert("ERROR", "ERROR AL ACTUALIZAR", "FALLO", "No se pudo actualizar el precio en la base de datos.");
            }
        } else if (emp != null) {
            // Gerente u otro: genera Solicitud formal
            String accion = "CAMBIO DE PRECIO: " + funcionSeleccionada.getTituloPelicula()
                    + " (" + funcionSeleccionada.getFecha() + " " + funcionSeleccionada.getHora() + ")"
                    + " de Q" + funcionSeleccionada.getPrecioBase() + " a Q" + nuevoPrecio;
            solicitudRepo.crear(emp.getIdEmpleado(), accion);
            alertInfo.viewAlert("INFORMATION", "SOLICITUD ENVIADA", "PENDIENTE DE APROBACIÓN",
                    "Tu solicitud para cambiar el precio a Q " + nuevoPrecio
                            + " ha sido enviada al Dueño para su revisión.");
        }
    }

    @FXML
    public void onBack(ActionEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
