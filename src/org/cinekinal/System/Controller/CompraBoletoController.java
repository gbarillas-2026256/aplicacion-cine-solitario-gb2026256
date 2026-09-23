package org.cinekinal.system.controller;

import java.awt.Desktop;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.cinekinal.system.model.Asiento;
import org.cinekinal.system.model.Boleto;
import org.cinekinal.system.model.BoletoCompraStatus;
import org.cinekinal.system.model.Cliente;
import org.cinekinal.system.model.Funcion;
import org.cinekinal.system.repository.AsientoRepository;
import org.cinekinal.system.repository.BoletoRepository;
import org.cinekinal.system.repository.FuncionRepository;
import org.cinekinal.system.service.BoletoService;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.QRCodeGenerator;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

public class CompraBoletoController implements Initializable {

    // Búsqueda y Filtros
    @FXML
    private TextField txtBuscarPelicula;
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

    // Panel 1: Detalle de Película
    @FXML
    private VBox panelFichaDetalle;
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

    // Panel 2: Ticket con Código QR
    @FXML
    private VBox panelTicket;
    @FXML
    private ImageView imgQR;
    @FXML
    private Label lblTicketPelicula;
    @FXML
    private Label lblTicketSalaHorario;
    @FXML
    private Label lblTicketAsiento;
    @FXML
    private Label lblTicketCliente;
    @FXML
    private Label lblTicketPago;
    @FXML
    private Label lblTicketId;

    private final FuncionRepository funcionRepo = new FuncionRepository();
    private final AsientoRepository asientoRepo = new AsientoRepository();
    private final BoletoRepository boletoRepo = new BoletoRepository();
    private final BoletoService boletoService = new BoletoService();
    private final AlertInformation alertInfo = new AlertInformation();

    private final Map<String, Button> botonesPorAsiento = new HashMap<>();
    private Funcion funcionSeleccionada;
    private Asiento asientoSeleccionado;

    private record DatosTarjeta(String ultimos4Digitos, String nombreTitular) {}

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colPelicula.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTituloPelicula()));
        colSala.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreSala()));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getFecha())));
        colHora.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getHora())));
        colPrecio.setCellValueFactory(d -> new SimpleStringProperty("Q" + d.getValue().getPrecioBase()));

        btnTrailer.setDisable(true);

        // 1. Configurar listener de selección PRIMERO
        tablaCartelera.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionada) -> {
            if (seleccionada != null) {
                seleccionarFuncion(seleccionada);
            }
        });

        // 2. Configurar clic directo sobre la tabla para asegurar respuesta aunque ya estuviera seleccionada
        tablaCartelera.setOnMouseClicked(event -> {
            Funcion seleccionada = tablaCartelera.getSelectionModel().getSelectedItem();
            if (seleccionada != null) {
                seleccionarFuncion(seleccionada);
            }
        });

        if (txtBuscarPelicula != null) {
            txtBuscarPelicula.setOnAction(e -> onBuscar(null));
        }

        // 3. Cargar funciones iniciales
        cargarCartelera(funcionRepo.obtenerCartelera());
    }

    private void seleccionarFuncion(Funcion seleccionada) {
        if (seleccionada == null) {
            limpiarFichaDetalle();
            return;
        }
        this.funcionSeleccionada = seleccionada;
        mostrarDetallePelicula(seleccionada);
        cargarMapaAsientos(seleccionada);
        mostrarPanelDetalle();
    }

    private void cargarCartelera(List<Funcion> funciones) {
        tablaCartelera.setItems(FXCollections.observableArrayList(funciones));
        if (!funciones.isEmpty()) {
            tablaCartelera.getSelectionModel().select(0);
            Funcion primera = tablaCartelera.getSelectionModel().getSelectedItem();
            if (primera == null) {
                primera = funciones.get(0);
            }
            seleccionarFuncion(primera);
        } else {
            limpiarFichaDetalle();
        }
    }

    @FXML
    public void onBuscar(MouseEvent event) {
        String query = txtBuscarPelicula.getText();
        if (query == null || query.isBlank()) {
            cargarCartelera(funcionRepo.obtenerCartelera());
            return;
        }
        List<Funcion> resultados = funcionRepo.buscarPorTitulo(query.trim());
        cargarCartelera(resultados);
        if (resultados.isEmpty()) {
            alertInfo.viewAlert("INFORMATION", "BÚSQUEDA DE PELÍCULAS", "Sin resultados",
                    "No se encontraron funciones para \"" + query.trim() + "\".");
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

        cargarPosterAsync(funcion.getPosterUrl());

        btnTrailer.setDisable(funcion.getTrailerUrl() == null || funcion.getTrailerUrl().isBlank());
    }

    private void cargarPosterAsync(String urlStr) {
        if (urlStr == null || urlStr.isBlank()) {
            imgPoster.setImage(null);
            return;
        }
        CompletableFuture.supplyAsync(() -> {
            try {
                if (urlStr.startsWith("http://") || urlStr.startsWith("https://")) {
                    URL url = URI.create(urlStr).toURL();
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                    conn.setConnectTimeout(4000);
                    conn.setReadTimeout(5000);
                    if (conn.getResponseCode() == 200) {
                        try (InputStream in = conn.getInputStream()) {
                            return new Image(in);
                        }
                    }
                } else {
                    return new Image(urlStr);
                }
            } catch (Exception ignored) {
            }
            return null;
        }).thenAccept(image -> Platform.runLater(() -> imgPoster.setImage(image)));
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
        String nombreClienteParaBoleto;
        if (Session.esCliente() && Session.getClienteActual() != null) {
            Cliente clienteActual = Session.getClienteActual();
            idCliente = clienteActual.getIdCliente();
            nombreClienteParaBoleto = clienteActual.getNombres() + " " + clienteActual.getApellidos();
        } else if (Session.esEmpleado()) {
            alertInfo.viewAlert("WARNING", "MODO ADMINISTRADOR", "Venta en Taquilla",
                    "Estás conectado como empleado. Para registrar ventas desde recepción, "
                    + "utiliza la opción 'Registrar Venta / Taquilla'.");
            return;
        } else {
            alertInfo.viewAlert("ERROR", "SIN SESIÓN", "Acceso no válido", "Debes iniciar sesión para comprar boletos.");
            return;
        }

        // 1. Confirmación de compra
        if (!confirmarCompra(funcionSeleccionada, asientoSeleccionado)) {
            return;
        }

        // 2. Modal realista de pago con tarjeta
        Optional<DatosTarjeta> tarjetaOpt = pedirDatosTarjeta(nombreClienteParaBoleto);
        if (tarjetaOpt.isEmpty()) {
            return; // Cancelado por el cliente
        }

        DatosTarjeta tarjeta = tarjetaOpt.get();

        // 3. Procesar compra en base de datos
        BoletoCompraStatus resultado = boletoService.comprar(
                funcionSeleccionada.getIdFuncion(), idCliente,
                asientoSeleccionado.getIdAsiento(), funcionSeleccionada.getPrecioBase());

        switch (resultado) {
            case COMPRA_EXITOSA -> {
                // Obtener el boleto recién comprado de la BD
                Boleto boleto = boletoRepo.obtenerUltimoBoletoComprado(
                        funcionSeleccionada.getIdFuncion(), asientoSeleccionado.getIdAsiento());

                String idBoleto = boleto != null ? boleto.getIdBoleto() : UUID.randomUUID().toString();
                String nombreMostrar = (boleto != null && boleto.getNombreCliente() != null)
                        ? boleto.getNombreCliente() : nombreClienteParaBoleto;

                // 4. Construir contenido y generar Código QR
                String contenidoQR = construirContenidoQR(idBoleto, funcionSeleccionada, asientoSeleccionado, nombreMostrar);
                Image qrImage = QRCodeGenerator.generar(contenidoQR, 220);

                // 5. Mostrar pantalla de Ticket con QR
                mostrarTicket(idBoleto, funcionSeleccionada, asientoSeleccionado, nombreMostrar, tarjeta, qrImage);
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

    private boolean confirmarCompra(Funcion funcion, Asiento asiento) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("CONFIRMAR COMPRA DE BOLETO");
        confirmacion.setHeaderText("¿Confirmas la compra de este boleto?");
        confirmacion.setContentText("🎬 Película: " + funcion.getTituloPelicula()
                + "\n🏛️ Sala: " + funcion.getNombreSala() + " (" + funcion.getTipoSala() + ")"
                + "\n📅 Horario: " + funcion.getFecha() + " " + funcion.getHora()
                + "\n🪑 Asiento: " + asiento.getEtiqueta()
                + "\n💰 Total a pagar: Q" + funcion.getPrecioBase());
        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.OK;
    }

    private Optional<DatosTarjeta> pedirDatosTarjeta(String nombrePorDefecto) {
        Dialog<DatosTarjeta> dialog = new Dialog<>();
        dialog.setTitle("PAGO CON TARJETA DE CRÉDITO / DÉBITO");
        dialog.setHeaderText("Simulación de Cobro Seguro · Ingrese los datos de su tarjeta");

        ButtonType botonPagar = new ButtonType("Pagar Q" + funcionSeleccionada.getPrecioBase(), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(botonPagar, ButtonType.CANCEL);

        TextField txtNumero = new TextField();
        txtNumero.setPromptText("1234 5678 9012 3456");
        TextField txtVencimiento = new TextField();
        txtVencimiento.setPromptText("MM/AA (ej. 08/28)");
        PasswordField txtCvv = new PasswordField();
        txtCvv.setPromptText("3 o 4 dígitos");
        TextField txtNombreTarjeta = new TextField();
        txtNombreTarjeta.setPromptText("Nombre como figura en el plástico");
        if (nombrePorDefecto != null && !nombrePorDefecto.isBlank()) {
            txtNombreTarjeta.setText(nombrePorDefecto);
        }

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #ff4d4d; -fx-font-size: 11px; -fx-font-weight: bold;");
        lblError.setWrapText(true);
        lblError.setMaxWidth(300);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(14, 18, 14, 18));
        grid.addRow(0, new Label("Número de tarjeta (16 dígitos):"), txtNumero);
        grid.addRow(1, new Label("Vencimiento (MM/AA):"), txtVencimiento);
        grid.addRow(2, new Label("Código de seguridad CVV:"), txtCvv);
        grid.addRow(3, new Label("Nombre en la tarjeta:"), txtNombreTarjeta);
        grid.add(lblError, 1, 4);
        dialog.getDialogPane().setContent(grid);

        Button botonPagarNodo = (Button) dialog.getDialogPane().lookupButton(botonPagar);
        botonPagarNodo.addEventFilter(ActionEvent.ACTION, event -> {
            String error = validarTarjeta(txtNumero.getText(), txtVencimiento.getText(),
                    txtCvv.getText(), txtNombreTarjeta.getText());
            if (error != null) {
                lblError.setText(error);
                event.consume();
            }
        });

        dialog.setResultConverter(boton -> {
            if (boton == botonPagar) {
                String soloDigitos = txtNumero.getText().replaceAll("[^0-9]", "");
                String ultimos4 = soloDigitos.length() >= 4 ? soloDigitos.substring(soloDigitos.length() - 4) : "0000";
                return new DatosTarjeta(ultimos4, txtNombreTarjeta.getText().trim());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private String validarTarjeta(String numero, String vencimiento, String cvv, String nombre) {
        String soloDigitos = numero == null ? "" : numero.replaceAll("[^0-9]", "");
        if (soloDigitos.length() != 16) {
            return "El número de tarjeta debe tener exactamente 16 dígitos.";
        }
        if (vencimiento == null || !vencimiento.trim().matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
            return "El vencimiento debe tener formato MM/AA (ej. 08/28).";
        }
        if (cvv == null || !cvv.trim().matches("[0-9]{3,4}")) {
            return "El código CVV debe tener 3 o 4 dígitos numéricos.";
        }
        if (nombre == null || nombre.isBlank()) {
            return "Ingresa el nombre del titular como aparece en la tarjeta.";
        }
        return null;
    }

    private String construirContenidoQR(String idBoleto, Funcion funcion, Asiento asiento, String nombreCliente) {
        return "CINE KINAL · TICKET DE ENTRADA\n"
                + "ID Boleto: " + idBoleto + "\n"
                + "Película: " + funcion.getTituloPelicula() + "\n"
                + "Sala: " + funcion.getNombreSala() + " (" + funcion.getTipoSala() + ")\n"
                + "Fecha: " + funcion.getFecha() + " " + funcion.getHora() + "\n"
                + "Asiento: " + asiento.getEtiqueta() + "\n"
                + "Cliente: " + nombreCliente + "\n"
                + "Precio: Q" + funcion.getPrecioBase();
    }

    private void mostrarTicket(String idBoleto, Funcion funcion, Asiento asiento,
                               String nombreCliente, DatosTarjeta tarjeta, Image qrImage) {
        panelFichaDetalle.setVisible(false);
        panelFichaDetalle.setManaged(false);
        panelTicket.setVisible(true);
        panelTicket.setManaged(true);

        imgQR.setImage(qrImage);
        lblTicketPelicula.setText("🎬 " + funcion.getTituloPelicula());
        lblTicketSalaHorario.setText("🏛️ " + funcion.getNombreSala() + " (" + funcion.getTipoSala() + ") · " + funcion.getFecha() + " " + funcion.getHora());
        lblTicketAsiento.setText("🪑 Asiento: " + asiento.getEtiqueta());
        lblTicketCliente.setText("👤 Cliente: " + nombreCliente);
        lblTicketPago.setText("💳 Total Pagado: Q" + funcion.getPrecioBase() + " (Tarjeta **** " + tarjeta.ultimos4Digitos() + ")");
        lblTicketId.setText("UUID / Código de Entrada: " + idBoleto);
    }

    @FXML
    public void onNuevaCompra(MouseEvent event) {
        mostrarPanelDetalle();
        if (funcionSeleccionada != null) {
            cargarMapaAsientos(funcionSeleccionada);
        }
    }

    private void mostrarPanelDetalle() {
        if (panelTicket != null) {
            panelTicket.setVisible(false);
            panelTicket.setManaged(false);
        }
        if (panelFichaDetalle != null) {
            panelFichaDetalle.setVisible(true);
            panelFichaDetalle.setManaged(true);
        }
    }

    @FXML
    public void onIrAMisBoletos(MouseEvent event) {
        new ViewFactory().viewMisBoletos();
    }

    @FXML
    public void onVolver(MouseEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
