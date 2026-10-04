package org.cinekinal.system.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import org.cinekinal.system.model.Movie;
import org.cinekinal.system.repository.MovieRepository;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.TrailerPlayer;
import org.cinekinal.system.utils.Validations;
import org.cinekinal.system.utils.ViewFactory;

/**
 * Controller for Managing Movie Catalog and Titles.
 */
public class ManageMoviesController implements Initializable {

    @FXML
    private TableView<Movie> tableMovies;
    @FXML
    private TableColumn<Movie, String> colTitle;
    @FXML
    private TableColumn<Movie, String> colGenre;
    @FXML
    private TableColumn<Movie, String> colRating;
    @FXML
    private TableColumn<Movie, String> colDuration;

    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDeactivate;

    @FXML
    private ImageView imgPoster;
    @FXML
    private Label lblDetailTitle;
    @FXML
    private Label lblDetailMeta;
    @FXML
    private Label lblDetailSynopsis;
    @FXML
    private Button btnWatchTrailer;

    private final MovieRepository movieRepo = new MovieRepository();
    private final Validations validate = new Validations();
    private final AlertInformation alertInfo = new AlertInformation();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        colGenre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGenre() != null ? d.getValue().getGenre() : "—"));
        colRating.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRating() != null ? d.getValue().getRating() : "—"));
        colDuration.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDurationMin() + " min"));

        tableMovies.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                btnEdit.setDisable(true);
                btnDeactivate.setDisable(true);
                clearDetail();
            } else {
                btnEdit.setDisable(false);
                btnDeactivate.setDisable(false);
                showMovieDetail(newVal);
            }
        });

        loadTable();
    }

    private void loadTable() {
        List<Movie> list = movieRepo.getActiveMovies();
        tableMovies.setItems(FXCollections.observableArrayList(list));
        btnEdit.setDisable(true);
        btnDeactivate.setDisable(true);
        clearDetail();
    }

    private void showMovieDetail(Movie movie) {
        lblDetailTitle.setText(movie.getTitle());
        lblDetailMeta.setText((movie.getGenre() != null ? movie.getGenre() : "Género no especificado")
                + " · " + (movie.getRating() != null ? movie.getRating() : "Sin clasif.") + " · "
                + movie.getDurationMin() + " min");
        lblDetailSynopsis.setText(movie.getSynopsis() != null && !movie.getSynopsis().isEmpty()
                ? movie.getSynopsis() : "Sin sinopsis disponible.");

        if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty()) {
            try {
                Image img = new Image(movie.getPosterUrl(), true);
                imgPoster.setImage(img);
            } catch (Exception e) {
                imgPoster.setImage(null);
            }
        } else {
            imgPoster.setImage(null);
        }

        btnWatchTrailer.setDisable(movie.getTrailerUrl() == null || movie.getTrailerUrl().trim().isEmpty());
    }

    private void clearDetail() {
        lblDetailTitle.setText("Selecciona una película");
        lblDetailMeta.setText("Género · Duración");
        lblDetailSynopsis.setText("Sinopsis...");
        imgPoster.setImage(null);
        btnWatchTrailer.setDisable(true);
    }

    @FXML
    public void onAddMovie(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("AGREGAR PELÍCULA");
        dialog.setHeaderText("Nueva Película para el Catálogo");
        applyDialogStyle(dialog);

        ButtonType btnSaveType = new ButtonType("GUARDAR", ButtonData.OK_DONE);
        ButtonType btnCancelType = new ButtonType("CANCELAR", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, btnCancelType);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 20, 16, 20));

        TextField txtTitle = new TextField();
        txtTitle.setPromptText("Título de la película");
        txtTitle.getStyleClass().add("eva-field");

        TextField txtGenre = new TextField();
        txtGenre.setPromptText("Acción, Aventura, Sci-Fi...");
        txtGenre.getStyleClass().add("eva-field");

        ComboBox<String> cmbRating = new ComboBox<>(FXCollections.observableArrayList("A", "B", "B-15", "C", "PG", "PG-13", "R"));
        cmbRating.setValue("PG-13");
        cmbRating.setMaxWidth(Double.MAX_VALUE);
        cmbRating.getStyleClass().add("eva-field");

        TextField txtDuration = new TextField();
        txtDuration.setPromptText("Ej. 120");
        txtDuration.getStyleClass().add("eva-field");

        TextArea txtSynopsis = new TextArea();
        txtSynopsis.setPromptText("Resumen o sinopsis...");
        txtSynopsis.setPrefRowCount(3);
        txtSynopsis.setWrapText(true);
        txtSynopsis.getStyleClass().add("eva-field");

        TextField txtPoster = new TextField();
        txtPoster.setPromptText("https://... enlace de la imagen");
        txtPoster.getStyleClass().add("eva-field");

        TextField txtTrailer = new TextField();
        txtTrailer.setPromptText("(ningún archivo elegido)");
        txtTrailer.setEditable(false);
        txtTrailer.getStyleClass().add("eva-field");
        HBox trailerPicker = buildTrailerPicker(txtTrailer);

        grid.add(createLabel("TÍTULO:"), 0, 0);
        grid.add(txtTitle, 1, 0);

        grid.add(createLabel("GÉNERO:"), 0, 1);
        grid.add(txtGenre, 1, 1);

        grid.add(createLabel("CLASIFICACIÓN:"), 0, 2);
        grid.add(cmbRating, 1, 2);

        grid.add(createLabel("DURACIÓN (MIN):"), 0, 3);
        grid.add(txtDuration, 1, 3);

        grid.add(createLabel("SINOPSIS:"), 0, 4);
        grid.add(txtSynopsis, 1, 4);

        grid.add(createLabel("URL PÓSTER:"), 0, 5);
        grid.add(txtPoster, 1, 5);

        grid.add(createLabel("TRÁILER (ARCHIVO):"), 0, 6);
        grid.add(trailerPicker, 1, 6);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == btnSaveType) {
            String title = txtTitle.getText().trim();
            String genre = txtGenre.getText().trim();
            String rating = cmbRating.getValue();
            String durStr = txtDuration.getText().trim();
            String synopsis = txtSynopsis.getText().trim();
            String poster = txtPoster.getText().trim();
            String trailer = txtTrailer.getText().trim();

            if (validate.validateTextEmpty(title) || validate.validateTextEmpty(durStr)) {
                alertInfo.viewAlert("WARNING", "CAMPOS REQUERIDOS", "FALTAN DATOS", "El título y la duración son obligatorios.");
                return;
            }

            int duration;
            try {
                duration = Integer.parseInt(durStr);
            } catch (NumberFormatException e) {
                alertInfo.viewAlert("WARNING", "DURACIÓN INVÁLIDA", "NÚMERO INCORRECTO", "La duración debe ser un número entero de minutos.");
                return;
            }

            movieRepo.create(title, genre, rating, duration, synopsis,
                    poster.isEmpty() ? null : poster, trailer.isEmpty() ? null : trailer);

            loadTable();
            alertInfo.viewAlert("INFORMATION", "PELÍCULA AGREGADA", "ÉXITO", "La película \"" + title + "\" fue agregada al catálogo.");
        }
    }

    @FXML
    public void onEditMovie(ActionEvent event) {
        Movie selected = tableMovies.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("EDITAR PELÍCULA");
        dialog.setHeaderText("Editar: " + selected.getTitle());
        applyDialogStyle(dialog);

        ButtonType btnSaveType = new ButtonType("GUARDAR CAMBIOS", ButtonData.OK_DONE);
        ButtonType btnCancelType = new ButtonType("CANCELAR", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, btnCancelType);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 20, 16, 20));

        TextField txtTitle = new TextField(selected.getTitle());
        txtTitle.getStyleClass().add("eva-field");

        TextField txtGenre = new TextField(selected.getGenre() != null ? selected.getGenre() : "");
        txtGenre.getStyleClass().add("eva-field");

        ComboBox<String> cmbRating = new ComboBox<>(FXCollections.observableArrayList("A", "B", "B-15", "C", "PG", "PG-13", "R"));
        cmbRating.setValue(selected.getRating() != null ? selected.getRating() : "PG-13");
        cmbRating.setMaxWidth(Double.MAX_VALUE);
        cmbRating.getStyleClass().add("eva-field");

        TextField txtDuration = new TextField(String.valueOf(selected.getDurationMin()));
        txtDuration.getStyleClass().add("eva-field");

        TextArea txtSynopsis = new TextArea(selected.getSynopsis() != null ? selected.getSynopsis() : "");
        txtSynopsis.setPrefRowCount(3);
        txtSynopsis.setWrapText(true);
        txtSynopsis.getStyleClass().add("eva-field");

        TextField txtPoster = new TextField(selected.getPosterUrl() != null ? selected.getPosterUrl() : "");
        txtPoster.getStyleClass().add("eva-field");

        TextField txtTrailer = new TextField(selected.getTrailerUrl() != null ? selected.getTrailerUrl() : "");
        txtTrailer.setEditable(false);
        txtTrailer.getStyleClass().add("eva-field");
        HBox trailerPicker = buildTrailerPicker(txtTrailer);

        grid.add(createLabel("TÍTULO:"), 0, 0);
        grid.add(txtTitle, 1, 0);

        grid.add(createLabel("GÉNERO:"), 0, 1);
        grid.add(txtGenre, 1, 1);

        grid.add(createLabel("CLASIFICACIÓN:"), 0, 2);
        grid.add(cmbRating, 1, 2);

        grid.add(createLabel("DURACIÓN (MIN):"), 0, 3);
        grid.add(txtDuration, 1, 3);

        grid.add(createLabel("SINOPSIS:"), 0, 4);
        grid.add(txtSynopsis, 1, 4);

        grid.add(createLabel("URL PÓSTER:"), 0, 5);
        grid.add(txtPoster, 1, 5);

        grid.add(createLabel("TRÁILER (ARCHIVO):"), 0, 6);
        grid.add(trailerPicker, 1, 6);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == btnSaveType) {
            String title = txtTitle.getText().trim();
            String genre = txtGenre.getText().trim();
            String rating = cmbRating.getValue();
            String durStr = txtDuration.getText().trim();
            String synopsis = txtSynopsis.getText().trim();
            String poster = txtPoster.getText().trim();
            String trailer = txtTrailer.getText().trim();

            int duration;
            try {
                duration = Integer.parseInt(durStr);
            } catch (NumberFormatException e) {
                alertInfo.viewAlert("WARNING", "DURACIÓN INVÁLIDA", "NÚMERO INCORRECTO", "La duración debe ser un número entero de minutos.");
                return;
            }

            movieRepo.edit(selected.getIdMovie(), title, genre, rating, duration, synopsis,
                    poster.isEmpty() ? null : poster, trailer.isEmpty() ? null : trailer);

            loadTable();
            alertInfo.viewAlert("INFORMATION", "CAMBIOS GUARDADOS", "ÉXITO", "Los datos de la película fueron actualizados.");
        }
    }

    @FXML
    public void onDeactivateMovie(ActionEvent event) {
        Movie selected = tableMovies.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Alert conf = new Alert(AlertType.CONFIRMATION);
        conf.setTitle("CONFIRMAR DESACTIVACIÓN");
        conf.setHeaderText("Desactivar Película");
        conf.setContentText("¿Seguro que deseas retirar de cartelera a \"" + selected.getTitle() + "\"?");
        applyDialogStyle(conf);

        Optional<ButtonType> res = conf.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            movieRepo.deactivate(selected.getIdMovie());
            loadTable();
            clearDetail();
            alertInfo.viewAlert("INFORMATION", "PELÍCULA RETIRADA", "ÉXITO", "La película fue desactivada del catálogo activo.");
        }
    }

    @FXML
    public void onWatchTrailer(ActionEvent event) {
        Movie movie = tableMovies.getSelectionModel().getSelectedItem();
        if (movie == null || movie.getTrailerUrl() == null || movie.getTrailerUrl().trim().isEmpty()) {
            return;
        }
        TrailerPlayer.play(movie.getTrailerUrl().trim());
    }

    /**
     * Builds a "pick a local trailer file" control: a read-only field showing
     * the stored path plus a button that opens a FileChooser, copies the
     * chosen video into the trailers/ folder next to the app, and writes the
     * resulting path into the field.
     */
    private HBox buildTrailerPicker(TextField txtTrailer) {
        Button btnChoose = new Button("ELEGIR...");
        btnChoose.getStyleClass().add("eva-button-ghost");
        btnChoose.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Elegir video del tráiler");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video MP4", "*.mp4"));
            File chosen = chooser.showOpenDialog(btnChoose.getScene().getWindow());
            if (chosen == null) {
                return;
            }
            try {
                File trailersDir = new File("trailers");
                trailersDir.mkdirs();
                File dest = new File(trailersDir, chosen.getName());
                Files.copy(chosen.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                txtTrailer.setText(dest.getPath());
            } catch (IOException ex) {
                alertInfo.viewAlert("ERROR", "NO SE PUDO COPIAR EL ARCHIVO", "ERROR AL GUARDAR", ex.getMessage());
            }
        });
        HBox box = new HBox(8, txtTrailer, btnChoose);
        HBox.setHgrow(txtTrailer, Priority.ALWAYS);
        return box;
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
