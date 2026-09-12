package org.cinekinal.system.controller;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.cinekinal.system.model.Funcion;
import org.cinekinal.system.model.Pelicula;
import org.cinekinal.system.model.Sala;
import org.cinekinal.system.repository.FuncionRepository;
import org.cinekinal.system.repository.PeliculaRepository;
import org.cinekinal.system.repository.SalaRepository;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Validations;
import org.cinekinal.system.utils.ViewFactory;

public class AdministrarFuncionesSalasController implements Initializable {

    // Tabla Funciones
    @FXML
    private TableView<Funcion> tableFunciones;
    @FXML
    private TableColumn<Funcion, String> colFuncionPelicula;
    @FXML
    private TableColumn<Funcion, String> colFuncionSala;
    @FXML
    private TableColumn<Funcion, String> colFuncionFecha;
    @FXML
    private TableColumn<Funcion, String> colFuncionHora;
    @FXML
    private TableColumn<Funcion, String> colFuncionPrecio;

    // Tabla Salas
    @FXML
    private TableView<Sala> tableSalas;
    @FXML
    private TableColumn<Sala, String> colSalaNombre;
    @FXML
    private TableColumn<Sala, String> colSalaTipo;
    @FXML
    private TableColumn<Sala, String> colSalaFilas;
    @FXML
    private TableColumn<Sala, String> colSalaColumnas;
    @FXML
    private TableColumn<Sala, String> colSalaCapacidad;

    private final FuncionRepository funcionRepo = new FuncionRepository();
    private final SalaRepository salaRepo = new SalaRepository();
    private final PeliculaRepository peliculaRepo = new PeliculaRepository();
    private final Validations validate = new Validations();
    private final AlertInformation alertInfo = new AlertInformation();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTablaFunciones();
        configurarTablaSalas();
        cargarDatos();
    }

    private void configurarTablaFunciones() {
        colFuncionPelicula.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTituloPelicula()));
        colFuncionSala.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreSala() + " (" + d.getValue().getTipoSala() + ")"));
        colFuncionFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha() != null ? d.getValue().getFecha().toString() : "—"));
        colFuncionHora.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHora() != null ? d.getValue().getHora().toString() : "—"));
        colFuncionPrecio.setCellValueFactory(d -> new SimpleStringProperty("Q " + d.getValue().getPrecioBase()));
    }

    private void configurarTablaSalas() {
        colSalaNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreSala()));
        colSalaTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipoSala()));
        colSalaFilas.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getFilas())));
        colSalaColumnas.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getColumnas())));
        colSalaCapacidad.setCellValueFactory(d -> new SimpleStringProperty(
                (d.getValue().getFilas() * d.getValue().getColumnas()) + " butacas"));
    }

    private void cargarDatos() {
        tableFunciones.setItems(FXCollections.observableArrayList(funcionRepo.obtenerCartelera()));
        tableSalas.setItems(FXCollections.observableArrayList(salaRepo.obtenerTodas()));
    }

    @FXML
    public void onProgramarFuncion(ActionEvent event) {
        List<Pelicula> peliculas = peliculaRepo.obtenerActivas();
        List<Sala> salas = salaRepo.obtenerTodas();

        if (peliculas.isEmpty()) {
            alertInfo.viewAlert("WARNING", "SIN PELÍCULAS", "CATÁLOGO VACÍO",
                    "Primero debes registrar películas en el catálogo para poder programar funciones.");
            return;
        }

        if (salas.isEmpty()) {
            alertInfo.viewAlert("WARNING", "SIN SALAS", "NO HAY SALAS",
                    "Primero debes crear al menos una sala de proyección.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("PROGRAMAR FUNCIÓN");
        dialog.setHeaderText("Nueva Proyección en Cartelera");
        aplicarEstiloDialogo(dialog);

        ButtonType btnGuardar = new ButtonType("GUARDAR FUNCIÓN", ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("CANCELAR", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, btnCancelar);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 20, 16, 20));

        ComboBox<Pelicula> cmbPelicula = new ComboBox<>(FXCollections.observableArrayList(peliculas));
        cmbPelicula.getSelectionModel().selectFirst();
        cmbPelicula.setMaxWidth(Double.MAX_VALUE);
        cmbPelicula.getStyleClass().add("eva-field");
        cmbPelicula.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Pelicula p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getTitulo() + " (" + p.getDuracionMin() + " min)");
            }
        });
        cmbPelicula.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Pelicula p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getTitulo());
            }
        });

        ComboBox<Sala> cmbSala = new ComboBox<>(FXCollections.observableArrayList(salas));
        cmbSala.getSelectionModel().selectFirst();
        cmbSala.setMaxWidth(Double.MAX_VALUE);
        cmbSala.getStyleClass().add("eva-field");
        cmbSala.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Sala s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : s.getNombreSala() + " · " + s.getTipoSala() + " (" + (s.getFilas() * s.getColumnas()) + " butacas)");
            }
        });
        cmbSala.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Sala s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : s.getNombreSala());
            }
        });

        DatePicker dpFecha = new DatePicker(LocalDate.now());
        dpFecha.setMaxWidth(Double.MAX_VALUE);
        dpFecha.getStyleClass().add("eva-field");

        ComboBox<String> cmbHora = new ComboBox<>(FXCollections.observableArrayList(
                "13:00:00", "14:30:00", "16:00:00", "17:30:00", "19:00:00", "20:30:00", "22:00:00"));
        cmbHora.getSelectionModel().select("17:30:00");
        cmbHora.setEditable(true);
        cmbHora.setMaxWidth(Double.MAX_VALUE);
        cmbHora.getStyleClass().add("eva-field");

        TextField txtPrecio = new TextField("45.00");
        txtPrecio.getStyleClass().add("eva-field");

        grid.add(crearLabel("PELÍCULA:"), 0, 0);
        grid.add(cmbPelicula, 1, 0);

        grid.add(crearLabel("SALA:"), 0, 1);
        grid.add(cmbSala, 1, 1);

        grid.add(crearLabel("FECHA:"), 0, 2);
        grid.add(dpFecha, 1, 2);

        grid.add(crearLabel("HORA (HH:MM:SS):"), 0, 3);
        grid.add(cmbHora, 1, 3);

        grid.add(crearLabel("PRECIO BASE (Q):"), 0, 4);
        grid.add(txtPrecio, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == btnGuardar) {
            Pelicula pSel = cmbPelicula.getValue();
            Sala sSel = cmbSala.getValue();
            LocalDate fechaSel = dpFecha.getValue();
            String horaStr = cmbHora.getValue();
            String precioStr = txtPrecio.getText().trim();

            if (pSel == null || sSel == null || fechaSel == null || horaStr == null || precioStr.isEmpty()) {
                alertInfo.viewAlert("WARNING", "CAMPOS INCOMPLETOS", "DATOS FALTANTES", "Completa todos los campos.");
                return;
            }

            BigDecimal precio;
            try {
                precio = new BigDecimal(precioStr);
            } catch (Exception e) {
                alertInfo.viewAlert("WARNING", "PRECIO INVÁLIDO", "ERROR DE FORMATO", "Ingresa un precio decimal válido (ej. 45.00).");
                return;
            }

            Time horaSql;
            try {
                if (horaStr.length() == 5) {
                    horaStr += ":00";
                }
                horaSql = Time.valueOf(horaStr);
            } catch (Exception e) {
                alertInfo.viewAlert("WARNING", "HORA INVÁLIDA", "FORMATO DE HORA", "Usa el formato HH:MM:SS (ej. 17:30:00).");
                return;
            }

            funcionRepo.crear(pSel.getIdPelicula(), sSel.getIdSala(), Date.valueOf(fechaSel), horaSql, precio);
            cargarDatos();
            alertInfo.viewAlert("INFORMATION", "FUNCIÓN PROGRAMADA", "ÉXITO",
                    "Función programada para \"" + pSel.getTitulo() + "\" en " + sSel.getNombreSala()
                            + " el " + fechaSel + " a las " + horaSql + ".");
        }
    }

    @FXML
    public void onCrearSala(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("CREAR NUEVA SALA");
        dialog.setHeaderText("Alta de Sala y Generación de Butacas");
        aplicarEstiloDialogo(dialog);

        ButtonType btnCrear = new ButtonType("CREAR SALA", ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("CANCELAR", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnCrear, btnCancelar);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 20, 16, 20));

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Ej. Sala 2 - MacroXE");
        txtNombre.getStyleClass().add("eva-field");

        ComboBox<String> cmbTipo = new ComboBox<>(FXCollections.observableArrayList("Normal", "IMAX", "3D", "VIP", "4DX"));
        cmbTipo.getSelectionModel().select("Normal");
        cmbTipo.setMaxWidth(Double.MAX_VALUE);
        cmbTipo.getStyleClass().add("eva-field");

        TextField txtFilas = new TextField("4");
        txtFilas.getStyleClass().add("eva-field");

        TextField txtColumnas = new TextField("8");
        txtColumnas.getStyleClass().add("eva-field");

        grid.add(crearLabel("NOMBRE DE SALA:"), 0, 0);
        grid.add(txtNombre, 1, 0);

        grid.add(crearLabel("TIPO DE SALA:"), 0, 1);
        grid.add(cmbTipo, 1, 1);

        grid.add(crearLabel("NÚMERO DE FILAS (A, B...):"), 0, 2);
        grid.add(txtFilas, 1, 2);

        grid.add(crearLabel("BUTACAS POR FILA (COLUMNAS):"), 0, 3);
        grid.add(txtColumnas, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == btnCrear) {
            String nombre = txtNombre.getText().trim();
            String tipo = cmbTipo.getValue();
            String filasStr = txtFilas.getText().trim();
            String colsStr = txtColumnas.getText().trim();

            if (nombre.isEmpty() || filasStr.isEmpty() || colsStr.isEmpty()) {
                alertInfo.viewAlert("WARNING", "CAMPOS INCOMPLETOS", "DATOS REQUERIDOS", "Completa todos los datos de la sala.");
                return;
            }

            int filas, cols;
            try {
                filas = Integer.parseInt(filasStr);
                cols = Integer.parseInt(colsStr);
                if (filas < 1 || filas > 26 || cols < 1 || cols > 50) {
                    throw new IllegalArgumentException();
                }
            } catch (Exception e) {
                alertInfo.viewAlert("WARNING", "DIMENSIONES INVÁLIDAS", "RANGO INCORRECTO",
                        "Filas deben ser entre 1 y 26 (letras A-Z) y columnas entre 1 y 50.");
                return;
            }

            String idNuevaSala = salaRepo.crear(nombre, tipo, filas, cols);
            cargarDatos();
            alertInfo.viewAlert("INFORMATION", "SALA CREADA", "ÉXITO",
                    "Se creó \"" + nombre + "\" con " + (filas * cols) + " butacas generadas automáticamente.");
        }
    }

    @FXML
    public void onBack(ActionEvent event) {
        new ViewFactory().viewMainMenu();
    }

    private Label crearLabel(String texto) {
        Label lbl = new Label(texto);
        lbl.getStyleClass().add("eva-label");
        return lbl;
    }

    private void aplicarEstiloDialogo(Dialog<?> dialog) {
        try {
            dialog.getDialogPane().getStylesheets().add(
                    getClass().getResource("/org/cinekinal/system/styles/ManageUsersStyles.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("dialog-eva");
        } catch (Exception ignored) {
        }
    }
}
