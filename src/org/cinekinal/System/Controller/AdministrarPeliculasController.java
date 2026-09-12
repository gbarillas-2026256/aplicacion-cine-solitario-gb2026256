package org.cinekinal.system.controller;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
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
import org.cinekinal.system.model.Pelicula;
import org.cinekinal.system.repository.PeliculaRepository;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Validations;
import org.cinekinal.system.utils.ViewFactory;

public class AdministrarPeliculasController implements Initializable {

    @FXML
    private TableView<Pelicula> tablePeliculas;
    @FXML
    private TableColumn<Pelicula, String> colTitulo;
    @FXML
    private TableColumn<Pelicula, String> colGenero;
    @FXML
    private TableColumn<Pelicula, String> colClasificacion;
    @FXML
    private TableColumn<Pelicula, String> colDuracion;

    @FXML
    private Button btnEditar;
    @FXML
    private Button btnDesactivar;

    @FXML
    private ImageView imgPoster;
    @FXML
    private Label lblTituloDetalle;
    @FXML
    private Label lblMetaDetalle;
    @FXML
    private Label lblSinopsisDetalle;
    @FXML
    private Button btnVerTrailer;

    private final PeliculaRepository peliculaRepo = new PeliculaRepository();
    private final Validations validate = new Validations();
    private final AlertInformation alertInfo = new AlertInformation();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colTitulo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitulo()));
        colGenero.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGenero()));
        colClasificacion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClasificacion()));
        colDuracion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDuracionMin() + " min"));

        tablePeliculas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                mostrarDetallePelicula(newVal);
                btnEditar.setDisable(false);
                btnDesactivar.setDisable(false);
            } else {
                limpiarDetalle();
                btnEditar.setDisable(true);
                btnDesactivar.setDisable(true);
            }
        });

        cargarTabla();
    }

    private void cargarTabla() {
        List<Pelicula> lista = peliculaRepo.obtenerActivas();
        tablePeliculas.setItems(FXCollections.observableArrayList(lista));
    }

    private void mostrarDetallePelicula(Pelicula p) {
        lblTituloDetalle.setText(p.getTitulo());
        lblMetaDetalle.setText((p.getGenero() != null ? p.getGenero() : "General") + " · "
                + (p.getClasificacion() != null ? p.getClasificacion() : "PG") + " · "
                + p.getDuracionMin() + " min");
        lblSinopsisDetalle.setText(p.getSinopsis() != null && !p.getSinopsis().isEmpty()
                ? p.getSinopsis() : "Sin sinopsis disponible.");

        if (p.getPosterUrl() != null && !p.getPosterUrl().isEmpty()) {
            try {
                Image img = new Image(p.getPosterUrl(), true);
                imgPoster.setImage(img);
            } catch (Exception e) {
                imgPoster.setImage(null);
            }
        } else {
            imgPoster.setImage(null);
        }

        btnVerTrailer.setDisable(p.getTrailerUrl() == null || p.getTrailerUrl().trim().isEmpty());
    }

    private void limpiarDetalle() {
        lblTituloDetalle.setText("Selecciona una película");
        lblMetaDetalle.setText("Género · Duración");
        lblSinopsisDetalle.setText("Sinopsis...");
        imgPoster.setImage(null);
        btnVerTrailer.setDisable(true);
    }

    @FXML
    public void onAgregarPelicula(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("AGREGAR PELÍCULA");
        dialog.setHeaderText("Nueva Película para el Catálogo");
        aplicarEstiloDialogo(dialog);

        ButtonType btnGuardar = new ButtonType("GUARDAR", ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("CANCELAR", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, btnCancelar);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 20, 16, 20));

        TextField txtTitulo = new TextField();
        txtTitulo.setPromptText("Título de la película");
        txtTitulo.getStyleClass().add("eva-field");

        TextField txtGenero = new TextField();
        txtGenero.setPromptText("Acción, Aventura, Sci-Fi...");
        txtGenero.getStyleClass().add("eva-field");

        ComboBox<String> cmbClasif = new ComboBox<>(FXCollections.observableArrayList("A", "B", "B-15", "C", "PG", "PG-13", "R"));
        cmbClasif.setValue("PG-13");
        cmbClasif.setMaxWidth(Double.MAX_VALUE);
        cmbClasif.getStyleClass().add("eva-field");

        TextField txtDuracion = new TextField();
        txtDuracion.setPromptText("Ej. 120");
        txtDuracion.getStyleClass().add("eva-field");

        TextArea txtSinopsis = new TextArea();
        txtSinopsis.setPromptText("Resumen o sinopsis...");
        txtSinopsis.setPrefRowCount(3);
        txtSinopsis.setWrapText(true);
        txtSinopsis.getStyleClass().add("eva-field");

        TextField txtPoster = new TextField();
        txtPoster.setPromptText("https://... enlace de la imagen");
        txtPoster.getStyleClass().add("eva-field");

        TextField txtTrailer = new TextField();
        txtTrailer.setPromptText("https://www.youtube.com/watch?v=...");
        txtTrailer.getStyleClass().add("eva-field");

        grid.add(crearLabel("TÍTULO:"), 0, 0);
        grid.add(txtTitulo, 1, 0);

        grid.add(crearLabel("GÉNERO:"), 0, 1);
        grid.add(txtGenero, 1, 1);

        grid.add(crearLabel("CLASIFICACIÓN:"), 0, 2);
        grid.add(cmbClasif, 1, 2);

        grid.add(crearLabel("DURACIÓN (MIN):"), 0, 3);
        grid.add(txtDuracion, 1, 3);

        grid.add(crearLabel("SINOPSIS:"), 0, 4);
        grid.add(txtSinopsis, 1, 4);

        grid.add(crearLabel("URL PÓSTER:"), 0, 5);
        grid.add(txtPoster, 1, 5);

        grid.add(crearLabel("URL TRÁILER:"), 0, 6);
        grid.add(txtTrailer, 1, 6);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == btnGuardar) {
            String titulo = txtTitulo.getText().trim();
            String genero = txtGenero.getText().trim();
            String clasif = cmbClasif.getValue();
            String durStr = txtDuracion.getText().trim();
            String sinopsis = txtSinopsis.getText().trim();
            String poster = txtPoster.getText().trim();
            String trailer = txtTrailer.getText().trim();

            if (validate.validateTextEmpty(titulo) || validate.validateTextEmpty(durStr)) {
                alertInfo.viewAlert("WARNING", "CAMPOS REQUERIDOS", "FALTAN DATOS", "El título y la duración son obligatorios.");
                return;
            }

            int duracion;
            try {
                duracion = Integer.parseInt(durStr);
            } catch (NumberFormatException e) {
                alertInfo.viewAlert("WARNING", "DURACIÓN INVÁLIDA", "NÚMERO INCORRECTO", "La duración debe ser un número entero de minutos.");
                return;
            }

            peliculaRepo.crear(titulo, genero, clasif, duracion, sinopsis,
                    poster.isEmpty() ? null : poster, trailer.isEmpty() ? null : trailer);

            cargarTabla();
            alertInfo.viewAlert("INFORMATION", "PELÍCULA AGREGADA", "ÉXITO", "La película \"" + titulo + "\" fue agregada al catálogo.");
        }
    }

    @FXML
    public void onEditarPelicula(ActionEvent event) {
        Pelicula seleccionada = tablePeliculas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("EDITAR PELÍCULA");
        dialog.setHeaderText("Editar: " + seleccionada.getTitulo());
        aplicarEstiloDialogo(dialog);

        ButtonType btnGuardar = new ButtonType("GUARDAR CAMBIOS", ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("CANCELAR", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, btnCancelar);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 20, 16, 20));

        TextField txtTitulo = new TextField(seleccionada.getTitulo());
        txtTitulo.getStyleClass().add("eva-field");

        TextField txtGenero = new TextField(seleccionada.getGenero() != null ? seleccionada.getGenero() : "");
        txtGenero.getStyleClass().add("eva-field");

        ComboBox<String> cmbClasif = new ComboBox<>(FXCollections.observableArrayList("A", "B", "B-15", "C", "PG", "PG-13", "R"));
        cmbClasif.setValue(seleccionada.getClasificacion() != null ? seleccionada.getClasificacion() : "PG-13");
        cmbClasif.setMaxWidth(Double.MAX_VALUE);
        cmbClasif.getStyleClass().add("eva-field");

        TextField txtDuracion = new TextField(String.valueOf(seleccionada.getDuracionMin()));
        txtDuracion.getStyleClass().add("eva-field");

        TextArea txtSinopsis = new TextArea(seleccionada.getSinopsis() != null ? seleccionada.getSinopsis() : "");
        txtSinopsis.setPrefRowCount(3);
        txtSinopsis.setWrapText(true);
        txtSinopsis.getStyleClass().add("eva-field");

        TextField txtPoster = new TextField(seleccionada.getPosterUrl() != null ? seleccionada.getPosterUrl() : "");
        txtPoster.getStyleClass().add("eva-field");

        TextField txtTrailer = new TextField(seleccionada.getTrailerUrl() != null ? seleccionada.getTrailerUrl() : "");
        txtTrailer.getStyleClass().add("eva-field");

        grid.add(crearLabel("TÍTULO:"), 0, 0);
        grid.add(txtTitulo, 1, 0);

        grid.add(crearLabel("GÉNERO:"), 0, 1);
        grid.add(txtGenero, 1, 1);

        grid.add(crearLabel("CLASIFICACIÓN:"), 0, 2);
        grid.add(cmbClasif, 1, 2);

        grid.add(crearLabel("DURACIÓN (MIN):"), 0, 3);
        grid.add(txtDuracion, 1, 3);

        grid.add(crearLabel("SINOPSIS:"), 0, 4);
        grid.add(txtSinopsis, 1, 4);

        grid.add(crearLabel("URL PÓSTER:"), 0, 5);
        grid.add(txtPoster, 1, 5);

        grid.add(crearLabel("URL TRÁILER:"), 0, 6);
        grid.add(txtTrailer, 1, 6);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == btnGuardar) {
            String titulo = txtTitulo.getText().trim();
            String genero = txtGenero.getText().trim();
            String clasif = cmbClasif.getValue();
            String durStr = txtDuracion.getText().trim();
            String sinopsis = txtSinopsis.getText().trim();
            String poster = txtPoster.getText().trim();
            String trailer = txtTrailer.getText().trim();

            int duracion;
            try {
                duracion = Integer.parseInt(durStr);
            } catch (NumberFormatException e) {
                alertInfo.viewAlert("WARNING", "DURACIÓN INVÁLIDA", "NÚMERO INCORRECTO", "La duración debe ser un número entero de minutos.");
                return;
            }

            peliculaRepo.editar(seleccionada.getIdPelicula(), titulo, genero, clasif, duracion, sinopsis,
                    poster.isEmpty() ? null : poster, trailer.isEmpty() ? null : trailer);

            cargarTabla();
            alertInfo.viewAlert("INFORMATION", "CAMBIOS GUARDADOS", "ÉXITO", "Los datos de la película fueron actualizados.");
        }
    }

    @FXML
    public void onDesactivarPelicula(ActionEvent event) {
        Pelicula seleccionada = tablePeliculas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            return;
        }

        Alert conf = new Alert(AlertType.CONFIRMATION);
        conf.setTitle("CONFIRMAR DESACTIVACIÓN");
        conf.setHeaderText("Desactivar Película");
        conf.setContentText("¿Seguro que deseas retirar de cartelera a \"" + seleccionada.getTitulo() + "\"?");
        aplicarEstiloDialogo(conf);

        Optional<ButtonType> res = conf.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            peliculaRepo.desactivar(seleccionada.getIdPelicula());
            cargarTabla();
            limpiarDetalle();
            alertInfo.viewAlert("INFORMATION", "PELÍCULA RETIRADA", "ÉXITO", "La película fue desactivada del catálogo activo.");
        }
    }

    @FXML
    public void onVerTrailer(ActionEvent event) {
        Pelicula p = tablePeliculas.getSelectionModel().getSelectedItem();
        if (p == null || p.getTrailerUrl() == null || p.getTrailerUrl().trim().isEmpty()) {
            return;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(p.getTrailerUrl().trim()));
            } else {
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", p.getTrailerUrl().trim()).start();
            }
        } catch (Exception e) {
            alertInfo.viewAlert("ERROR", "ERROR AL ABRIR TRÁILER", "FALLO DE NAVEGADOR",
                    "No se pudo abrir el navegador web: " + e.getMessage());
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
