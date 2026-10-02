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
import org.cinekinal.system.model.Movie;
import org.cinekinal.system.model.Showtime;
import org.cinekinal.system.model.Theater;
import org.cinekinal.system.repository.MovieRepository;
import org.cinekinal.system.repository.ShowtimeRepository;
import org.cinekinal.system.repository.TheaterRepository;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Validations;
import org.cinekinal.system.utils.ViewFactory;

public class ManageShowtimesTheatersController implements Initializable {

    // Showtime Table
    @FXML
    private TableView<Showtime> tableShowtimes;
    @FXML
    private TableColumn<Showtime, String> colShowtimeMovie;
    @FXML
    private TableColumn<Showtime, String> colShowtimeTheater;
    @FXML
    private TableColumn<Showtime, String> colShowtimeDate;
    @FXML
    private TableColumn<Showtime, String> colShowtimeTime;
    @FXML
    private TableColumn<Showtime, String> colShowtimePrice;

    // Theater Table
    @FXML
    private TableView<Theater> tableTheaters;
    @FXML
    private TableColumn<Theater, String> colTheaterName;
    @FXML
    private TableColumn<Theater, String> colTheaterType;
    @FXML
    private TableColumn<Theater, String> colTheaterRows;
    @FXML
    private TableColumn<Theater, String> colTheaterColumns;
    @FXML
    private TableColumn<Theater, String> colTheaterCapacity;

    private final ShowtimeRepository showtimeRepo = new ShowtimeRepository();
    private final TheaterRepository theaterRepo = new TheaterRepository();
    private final MovieRepository movieRepo = new MovieRepository();
    private final Validations validate = new Validations();
    private final AlertInformation alertInfo = new AlertInformation();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initShowtimesTable();
        initTheatersTable();
        loadData();
    }

    private void initShowtimesTable() {
        colShowtimeMovie.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMovieTitle()));
        colShowtimeTheater.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTheaterName() + " (" + d.getValue().getTheaterType() + ")"));
        colShowtimeDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDate() != null ? d.getValue().getDate().toString() : "—"));
        colShowtimeTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTime() != null ? d.getValue().getTime().toString() : "—"));
        colShowtimePrice.setCellValueFactory(d -> new SimpleStringProperty("Q " + d.getValue().getBasePrice()));
    }

    private void initTheatersTable() {
        colTheaterName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colTheaterType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType()));
        colTheaterRows.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getRows())));
        colTheaterColumns.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getColumns())));
        colTheaterCapacity.setCellValueFactory(d -> new SimpleStringProperty(
                (d.getValue().getRows() * d.getValue().getColumns()) + " butacas"));
    }

    private void loadData() {
        tableShowtimes.setItems(FXCollections.observableArrayList(showtimeRepo.getBillboard()));
        tableTheaters.setItems(FXCollections.observableArrayList(theaterRepo.getAll()));
    }

    @FXML
    public void onAddShowtime(ActionEvent event) {
        List<Movie> activeMovies = movieRepo.getActiveMovies();
        List<Theater> theaters = theaterRepo.getAll();

        if (activeMovies.isEmpty()) {
            alertInfo.viewAlert("WARNING", "SIN PELÍCULAS", "CATÁLOGO VACÍO",
                    "Primero debes registrar películas en el catálogo para poder programar funciones.");
            return;
        }

        if (theaters.isEmpty()) {
            alertInfo.viewAlert("WARNING", "SIN SALAS", "NO HAY SALAS",
                    "Primero debes crear al menos una sala de proyección.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("PROGRAMAR FUNCIÓN");
        dialog.setHeaderText("Nueva Proyección en Cartelera");
        applyDialogStyle(dialog);

        ButtonType btnSave = new ButtonType("GUARDAR FUNCIÓN", ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("CANCELAR", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, btnCancel);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 20, 16, 20));

        ComboBox<Movie> cmbMovie = new ComboBox<>(FXCollections.observableArrayList(activeMovies));
        cmbMovie.getSelectionModel().selectFirst();
        cmbMovie.setMaxWidth(Double.MAX_VALUE);
        cmbMovie.getStyleClass().add("eva-field");
        cmbMovie.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Movie p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getTitle() + " (" + p.getDurationMin() + " min)");
            }
        });
        cmbMovie.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Movie p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getTitle());
            }
        });

        ComboBox<Theater> cmbTheater = new ComboBox<>(FXCollections.observableArrayList(theaters));
        cmbTheater.getSelectionModel().selectFirst();
        cmbTheater.setMaxWidth(Double.MAX_VALUE);
        cmbTheater.getStyleClass().add("eva-field");
        cmbTheater.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Theater s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : s.getName() + " · " + s.getType() + " (" + (s.getRows() * s.getColumns()) + " butacas)");
            }
        });
        cmbTheater.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Theater s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : s.getName());
            }
        });

        DatePicker dpDate = new DatePicker(LocalDate.now());
        dpDate.setMaxWidth(Double.MAX_VALUE);
        dpDate.getStyleClass().add("eva-field");

        ComboBox<String> cmbTime = new ComboBox<>(FXCollections.observableArrayList(
                "13:00:00", "14:30:00", "16:00:00", "17:30:00", "19:00:00", "20:30:00", "22:00:00"));
        cmbTime.getSelectionModel().select("17:30:00");
        cmbTime.setEditable(true);
        cmbTime.setMaxWidth(Double.MAX_VALUE);
        cmbTime.getStyleClass().add("eva-field");

        TextField txtPrice = new TextField("45.00");
        txtPrice.getStyleClass().add("eva-field");

        grid.add(createLabel("PELÍCULA:"), 0, 0);
        grid.add(cmbMovie, 1, 0);

        grid.add(createLabel("SALA:"), 0, 1);
        grid.add(cmbTheater, 1, 1);

        grid.add(createLabel("FECHA:"), 0, 2);
        grid.add(dpDate, 1, 2);

        grid.add(createLabel("HORA (HH:MM:SS):"), 0, 3);
        grid.add(cmbTime, 1, 3);

        grid.add(createLabel("PRECIO BASE (Q):"), 0, 4);
        grid.add(txtPrice, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == btnSave) {
            Movie selectedMovie = cmbMovie.getValue();
            Theater selectedTheater = cmbTheater.getValue();
            LocalDate selectedDate = dpDate.getValue();
            String timeStr = cmbTime.getValue();
            String priceStr = txtPrice.getText().trim();

            if (selectedMovie == null || selectedTheater == null || selectedDate == null || timeStr == null || priceStr.isEmpty()) {
                alertInfo.viewAlert("WARNING", "CAMPOS INCOMPLETOS", "DATOS FALTANTES", "Completa todos los campos.");
                return;
            }

            BigDecimal price;
            try {
                price = new BigDecimal(priceStr);
            } catch (Exception e) {
                alertInfo.viewAlert("WARNING", "PRECIO INVÁLIDO", "ERROR DE FORMATO", "Ingresa un precio decimal válido (ej. 45.00).");
                return;
            }

            Time sqlTime;
            try {
                if (timeStr.length() == 5) {
                    timeStr += ":00";
                }
                sqlTime = Time.valueOf(timeStr);
            } catch (Exception e) {
                alertInfo.viewAlert("WARNING", "HORA INVÁLIDA", "FORMATO DE HORA", "Usa el formato HH:MM:SS (ej. 17:30:00).");
                return;
            }

            showtimeRepo.create(selectedMovie.getIdMovie(), selectedTheater.getIdTheater(), Date.valueOf(selectedDate), sqlTime, price);
            loadData();
            alertInfo.viewAlert("INFORMATION", "FUNCIÓN PROGRAMADA", "ÉXITO",
                    "Función programada para \"" + selectedMovie.getTitle() + "\" en " + selectedTheater.getName()
                            + " el " + selectedDate + " a las " + sqlTime + ".");
        }
    }

    @FXML
    public void onAddTheater(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("CREAR NUEVA SALA");
        dialog.setHeaderText("Alta de Sala y Generación de Butacas");
        applyDialogStyle(dialog);

        ButtonType btnCreate = new ButtonType("CREAR SALA", ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("CANCELAR", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnCreate, btnCancel);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 20, 16, 20));

        TextField txtName = new TextField();
        txtName.setPromptText("Ej. Sala 2 - MacroXE");
        txtName.getStyleClass().add("eva-field");

        ComboBox<String> cmbType = new ComboBox<>(FXCollections.observableArrayList("Normal", "IMAX", "3D", "VIP", "4DX"));
        cmbType.getSelectionModel().select("Normal");
        cmbType.setMaxWidth(Double.MAX_VALUE);
        cmbType.getStyleClass().add("eva-field");

        TextField txtRows = new TextField("4");
        txtRows.getStyleClass().add("eva-field");

        TextField txtCols = new TextField("8");
        txtCols.getStyleClass().add("eva-field");

        grid.add(createLabel("NOMBRE DE SALA:"), 0, 0);
        grid.add(txtName, 1, 0);

        grid.add(createLabel("TIPO DE SALA:"), 0, 1);
        grid.add(cmbType, 1, 1);

        grid.add(createLabel("NÚMERO DE FILAS (A, B...):"), 0, 2);
        grid.add(txtRows, 1, 2);

        grid.add(createLabel("BUTACAS POR FILA (COLUMNAS):"), 0, 3);
        grid.add(txtCols, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == btnCreate) {
            String name = txtName.getText().trim();
            String type = cmbType.getValue();
            String rowsStr = txtRows.getText().trim();
            String colsStr = txtCols.getText().trim();

            if (name.isEmpty() || rowsStr.isEmpty() || colsStr.isEmpty()) {
                alertInfo.viewAlert("WARNING", "CAMPOS INCOMPLETOS", "DATOS REQUERIDOS", "Completa todos los datos de la sala.");
                return;
            }

            int rows, cols;
            try {
                rows = Integer.parseInt(rowsStr);
                cols = Integer.parseInt(colsStr);
                if (rows < 1 || rows > 26 || cols < 1 || cols > 50) {
                    throw new IllegalArgumentException();
                }
            } catch (Exception e) {
                alertInfo.viewAlert("WARNING", "DIMENSIONES INVÁLIDAS", "RANGO INCORRECTO",
                        "Filas deben ser entre 1 y 26 (letras A-Z) y columnas entre 1 y 50.");
                return;
            }

            theaterRepo.create(name, type, rows, cols);
            loadData();
            alertInfo.viewAlert("INFORMATION", "SALA CREADA", "ÉXITO",
                    "Se creó \"" + name + "\" con " + (rows * cols) + " butacas generadas automáticamente.");
        }
    }

    @FXML
    public void onBack(ActionEvent event) {
        new ViewFactory().viewMainMenu();
    }

    private Label createLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("eva-label");
        return lbl;
    }

    private void applyDialogStyle(Dialog<?> dialog) {
        try {
            dialog.getDialogPane().getStylesheets().add(
                    getClass().getResource("/org/cinekinal/system/styles/ManageUsersStyles.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("dialog-eva");
        } catch (Exception ignored) {
        }
    }
}
