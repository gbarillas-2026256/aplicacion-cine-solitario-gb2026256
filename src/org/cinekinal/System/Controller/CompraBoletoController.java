package org.cinekinal.system.controller;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.cinekinal.system.model.Asiento;
import org.cinekinal.system.model.BoletoCompraStatus;
import org.cinekinal.system.model.Cliente;
import org.cinekinal.system.model.Funcion;
import org.cinekinal.system.repository.AsientoRepository;
import org.cinekinal.system.repository.FuncionRepository;
import org.cinekinal.system.service.BoletoService;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

/**
 * Flujo de compra: 1) elegir una funcion de la cartelera, 2) elegir un
 * asiento libre en el mapa (los ocupados salen deshabilitados y en
 * rojo), 3) comprar. Es solo para cuentas de Cliente.
 */
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

    private final FuncionRepository funcionRepo = new FuncionRepository();
    private final AsientoRepository asientoRepo = new AsientoRepository();
    private final org.cinekinal.system.repository.BoletoRepository boletoRepo = new org.cinekinal.system.repository.BoletoRepository();
    private final BoletoService boletoService = new BoletoService();
    private final AlertInformation alertInfo = new AlertInformation();

    private final Map<String, Button> botonesPorAsiento = new HashMap<>();
    private Funcion funcionSeleccionada;
    private Asiento asientoSeleccionado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!Session.esCliente()) {
            alertInfo.viewAlert("ERROR", "SOLO CLIENTES", "Acceso restringido",
                    "Esta sección es solo para cuentas de cliente.");
            new ViewFactory().viewMainMenu();
            return;
        }

        colPelicula.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTituloPelicula()));
        colSala.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreSala()));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getFecha())));
        colHora.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getHora())));
        colPrecio.setCellValueFactory(d -> new SimpleStringProperty("Q" + d.getValue().getPrecioBase()));

        tablaCartelera.setItems(FXCollections.observableArrayList(funcionRepo.obtenerCartelera()));

        tablaCartelera.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionada) -> {
            if (seleccionada != null) {
                cargarMapaAsientos(seleccionada);
            }
        });
    }

    private void cargarMapaAsientos(Funcion funcion) {
        funcionSeleccionada = funcion;
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
        lblSeleccion.setText("Asiento seleccionado: " + asiento.getEtiqueta());
    }

    @FXML
    public void onComprar(MouseEvent event) {
        if (funcionSeleccionada == null || asientoSeleccionado == null) {
            alertInfo.viewAlert("WARNING", "FALTAN DATOS", "Elige función y asiento",
                    "Selecciona una función de la cartelera y un asiento libre antes de comprar.");
            return;
        }

        Cliente cliente = Session.getClienteActual();
        BoletoCompraStatus resultado = boletoService.comprar(
                funcionSeleccionada.getIdFuncion(), cliente.getIdCliente(),
                asientoSeleccionado.getIdAsiento(), funcionSeleccionada.getPrecioBase());

        switch (resultado) {
            case COMPRA_EXITOSA -> {
                alertInfo.viewAlert("INFORMATION", "COMPRA EXITOSA", "¡Listo!",
                        "Tu boleto para \"" + funcionSeleccionada.getTituloPelicula()
                        + "\" en el asiento " + asientoSeleccionado.getEtiqueta() + " quedó reservado.");
                cargarMapaAsientos(funcionSeleccionada);
            }
            case ASIENTO_YA_VENDIDO -> {
                alertInfo.viewAlert("WARNING", "ASIENTO NO DISPONIBLE", "Alguien más lo compró primero",
                        "Elige otro asiento -- este ya se vendió mientras decidías.");
                cargarMapaAsientos(funcionSeleccionada);
            }
            case ERROR_AL_COMPRAR -> alertInfo.viewAlert("ERROR", "ERROR", "No se pudo completar la compra",
                    "Ocurrió un error al procesar tu compra. Intenta de nuevo.");
        }
    }

    @FXML
    public void onVolver(MouseEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
