package org.cinekinal.system.controller;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.cinekinal.system.model.Asiento;
import org.cinekinal.system.model.BoletoCompraStatus;
import org.cinekinal.system.model.Cliente;
import org.cinekinal.system.model.Funcion;
import org.cinekinal.system.repository.AsientoRepository;
import org.cinekinal.system.repository.BoletoRepository;
import org.cinekinal.system.repository.FuncionRepository;
import org.cinekinal.system.service.BoletoService;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

public class CompraBoletoController implements Initializable {

    @FXML
    private TableView<Funcion> tablaCartelera;
    @FXML
    private TableColumn<Funcion, String> colPelicula;
    @FXML
    private TableColumn<Funcion, String> colSala;
    @FXML
    private TableColumn<Funcion, String> colFecha;
    @FXML
    private TableColumn<Funcion, String> colHora;
    @FXML
    private TableColumn<Funcion, String> colPrecio;

    @FXML
    private GridPane gridAsientos;
    @FXML
    private Label lblSeleccion;
    @FXML
    private Button btnComprar;

    @FXML
    private Button btnHoy;
    @FXML
    private Button btnManana;
    @FXML
    private Button btnTodas;
    @FXML
    private DatePicker dpFecha;

    @FXML
    private ImageView imgPoster;
    @FXML
    private Label lblTituloPelicula;
    @FXML
    private Label lblGenero;
    @FXML
    private Label lblClasificacion;
    @FXML
    private Label lblDuracion;
    @FXML
    private Label lblSalaTipo;
    @FXML
    private Label lblPrecioFicha;
    @FXML
    private Label lblSinopsis;
    @FXML
    private Button btnTrailer;

    private final FuncionRepository funcionRepo = new FuncionRepository();
    private final AsientoRepository asientoRepo = new AsientoRepository();
    private final BoletoRepository boletoRepo = new BoletoRepository();
    private final BoletoService boletoService = new BoletoService();
    private final AlertInformation alertInfo = new AlertInformation();

    private final Map<String, Button> botonesPorAsiento = new HashMap<>();
    private Funcion funcionSeleccionada;
    private Asiento asientoSeleccionado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colPelicula.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTituloPelicula()));
        colSala.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreSala()));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getFecha())));
        colHora.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getHora())));
        colPrecio.setCellValueFactory(d -> new SimpleStringProperty("Q" + d.getValue().getPrecioBase()));

        btnTrailer.setDisable(true);

        cargarCartelera(funcionRepo.obtenerCartelera());

        tablaCartelera.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionada) -> {
            if (seleccionada != null) {
                funcionSeleccionada = seleccionada;
                mostrarDetallePelicula(seleccionada);
                cargarMapaAsientos(seleccionada);
            }
        });
    }

    private void cargarCartelera(List<Funcion> funciones) {
        tablaCartelera.setItems(FXCollections.observableArrayList(funciones));
        if (!funciones.isEmpty()) {
            tablaCartelera.getSelectionModel().select(0);
        } else {
            limpiarFichaDetalle();
        }
    }

    private void mostrarDetallePelicula(Funcion funcion) {
        lblTituloPelicula.setText(funcion.getTituloPelicula());
        lblGenero.setText("Género: " + (funcion.getGenero() != null ? funcion.getGenero() : "N/D"));
        lblClasificacion.setText("Clasificación: " + (funcion.getClasificacion() != null ? funcion.getClasificacion() : "N/D"));
        lblDuracion.setText("Duración: " + funcion.getDuracionMin() + " min");
        lblSalaTipo.setText("Sala: " + funcion.getNombreSala() + " (" + funcion.getTipoSala() + ")");
        lblPrecioFicha.setText("Precio base: Q" + funcion.getPrecioBase());

        if (funcion.getSinopsis() != null && !funcion.getSinopsis().isBlank()) {
            lblSinopsis.setText(funcion.getSinopsis());
        } else {
            lblSinopsis.setText("Sin sinopsis registrada para esta película.");
        }

        // Cargar póster con fallback
        if (funcion.getPosterUrl() != null && !funcion.getPosterUrl().isBlank()) {
            try {
                imgPoster.setImage(new Image(funcion.getPosterUrl(), true));
            } catch (Exception e) {
                imgPoster.setImage(null);
            }
        } else {
            imgPoster.setImage(null);
        }

        btnTrailer.setDisable(funcion.getTrailerUrl() == null || funcion.getTrailerUrl().isBlank());
    }

    private void limpiarFichaDetalle() {
        lblTituloPelicula.setText("No hay funciones para esta fecha");
        lblGenero.setText("Género: -");
        lblClasificacion.setText("Clasificación: -");
        lblDuracion.setText("Duración: -");
        lblSalaTipo.setText("Sala: -");
        lblPrecioFicha.setText("Precio: -");
        lblSinopsis.setText("Selecciona otra fecha o programa una nueva función.");
        imgPoster.setImage(null);
        btnTrailer.setDisable(true);
        gridAsientos.getChildren().clear();
        lblSeleccion.setText("Ningún asiento seleccionado");
    }

    private void cargarMapaAsientos(Funcion funcion) {
        asientoSeleccionado = null;
        botonesPorAsiento.clear();
        lblSeleccion.setText("Ningún asiento seleccionado");
        gridAsientos.getChildren().clear();

        List<Asiento> asientos = asientoRepo.obtenerPorSala(funcion.getIdSala());
        Set<String> idsOcupados = boletoRepo.obtenerAsientosOcupados(funcion.getIdFuncion());

        for (Asiento asiento : asientos) {
            Button boton = new Button(asiento.getEtiqueta());
            boton.getStyleClass().add("eva-seat");

            boolean ocupado = idsOcupados.contains(asiento.getIdAsiento());
            boton.getStyleClass().add(ocupado ? "eva-seat-ocupado" : "eva-seat-libre");
            boton.setDisable(ocupado);
            if (!ocupado) {
                boton.setOnAction(e -> seleccionarAsiento(asiento, boton));
            }
            botonesPorAsiento.put(asiento.getIdAsiento(), boton);

            int fila = Character.toUpperCase(asiento.getFila().charAt(0)) - 'A';
            int columna = asiento.getNumero() - 1;
            gridAsientos.add(boton, columna, fila);
        }
    }

    private void seleccionarAsiento(Asiento asiento, Button boton) {
        if (asientoSeleccionado != null) {
            Button botonAnterior = botonesPorAsiento.get(asientoSeleccionado.getIdAsiento());
            if (botonAnterior != null) {
                botonAnterior.getStyleClass().remove("eva-seat-seleccionado");
                botonAnterior.getStyleClass().add("eva-seat-libre");
            }
        }
        asientoSeleccionado = asiento;
        boton.getStyleClass().remove("eva-seat-libre");
        boton.getStyleClass().add("eva-seat-seleccionado");
        lblSeleccion.setText("Asiento seleccionado: " + asiento.getEtiqueta() + " (Q" + funcionSeleccionada.getPrecioBase() + ")");
    }

    @FXML
    public void onVerTrailer(MouseEvent event) {
        if (funcionSeleccionada != null && funcionSeleccionada.getTrailerUrl() != null) {
            String urlTrailer = funcionSeleccionada.getTrailerUrl();
            abrirEnlaceWeb(urlTrailer);
        }
    }

    private void abrirEnlaceWeb(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
            }
        } catch (Exception e) {
            alertInfo.viewAlert("WARNING", "TRÁILER", "Enlace del tráiler:", url);
        }
    }

    @FXML
    public void onFiltrarHoy(MouseEvent event) {
        Date fechaHoy = Date.valueOf(LocalDate.now());
        cargarCartelera(funcionRepo.obtenerCarteleraPorFecha(fechaHoy));
    }

    @FXML
    public void onFiltrarManana(MouseEvent event) {
        Date fechaManana = Date.valueOf(LocalDate.now().plusDays(1));
        cargarCartelera(funcionRepo.obtenerCarteleraPorFecha(fechaManana));
    }

    @FXML
    public void onFiltrarTodas(MouseEvent event) {
        cargarCartelera(funcionRepo.obtenerCartelera());
    }

    @FXML
    public void onFechaCambiada(ActionEvent event) {
        if (dpFecha.getValue() != null) {
            Date fecha = Date.valueOf(dpFecha.getValue());
            cargarCartelera(funcionRepo.obtenerCarteleraPorFecha(fecha));
        }
    }

    @FXML
    public void onComprar(MouseEvent event) {
        if (funcionSeleccionada == null || asientoSeleccionado == null) {
            alertInfo.viewAlert("WARNING", "FALTAN DATOS", "Elige función y asiento",
                    "Selecciona una función de la cartelera y un asiento libre antes de comprar.");
            return;
        }

        String idCliente;
        if (Session.esCliente()) {
            idCliente = Session.getClienteActual().getIdCliente();
        } else if (Session.esEmpleado()) {
            alertInfo.viewAlert("WARNING", "MODO ADMINISTRADOR", "Venta en Taquilla",
                    "Estás conectado como empleado. Para asociar boletos a un cliente específico, "
                    + "el cliente debe iniciar sesión o registrarse.");
            return;
        } else {
            alertInfo.viewAlert("ERROR", "SIN SESIÓN", "Acceso no válido", "Debes iniciar sesión para comprar boletos.");
            return;
        }

        BoletoCompraStatus resultado = boletoService.comprar(
                funcionSeleccionada.getIdFuncion(), idCliente,
                asientoSeleccionado.getIdAsiento(), funcionSeleccionada.getPrecioBase());

        switch (resultado) {
            case COMPRA_EXITOSA -> {
                alertInfo.viewAlert("INFORMATION", "COMPRA EXITOSA", "¡Disfruta la película!",
                        "Tu boleto para \"" + funcionSeleccionada.getTituloPelicula()
                        + "\" en el asiento " + asientoSeleccionado.getEtiqueta() + " quedó confirmado.");
                cargarMapaAsientos(funcionSeleccionada);
            }
            case ASIENTO_YA_VENDIDO -> {
                alertInfo.viewAlert("WARNING", "ASIENTO OCUPADO", "Asiento no disponible",
                        "Alguien más reservó este asiento hace unos instantes. Por favor elige otro.");
                cargarMapaAsientos(funcionSeleccionada);
            }
            case ERROR_AL_COMPRAR -> alertInfo.viewAlert("ERROR", "ERROR", "No se pudo procesar la compra",
                    "Ocurrió un error al registrar la compra. Intenta de nuevo.");
        }
    }

    @FXML
    public void onVolver(MouseEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
