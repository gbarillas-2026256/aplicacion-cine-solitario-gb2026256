package org.cinekinal.system.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.cinekinal.system.model.Accion;
import org.cinekinal.system.model.Cliente;
import org.cinekinal.system.model.Empleado;
import org.cinekinal.system.model.Funcion;
import org.cinekinal.system.model.ResultadoIntento;
import org.cinekinal.system.repository.FuncionRepository;
import org.cinekinal.system.service.PermisoService;
import org.cinekinal.system.service.SolicitudService;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

/**
 * Vista provisional del "hub" al que se llega despues de iniciar
 * sesion. El sidebar NO esta escrito en el FXML: se arma aqui en
 * codigo segun el tipo de cuenta, para poder decidir boton por boton
 * si se muestra o no con PermisoService.puedeVer(...).
 *
 * Cada boton de una accion sensible de Empleado pasa por
 * SolicitudService.intentar(...) -- el mismo punto unico de
 * validacion que se usaria desde cualquier otro Controller. Asi,
 * aunque la pantalla de una seccion todavia no este construida (la
 * mayoria son un aviso de "en construccion" por ahora), el flujo de
 * permisos y de Solicitudes ya funciona de verdad de punta a punta.
 */
public class MainMenuController implements Initializable {

    @FXML
    private Label lblBienvenida;
    @FXML
    private Label lblTipoCuenta;
    @FXML
    private VBox vboxSidebar;

    private final PermisoService permisoService = new PermisoService();
    private final SolicitudService solicitudService = new SolicitudService();
    private final FuncionRepository funcionRepo = new FuncionRepository();
    private final AlertInformation alertInfo = new AlertInformation();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        vboxSidebar.getChildren().clear();

        if (Session.esEmpleado()) {
            Empleado empleado = Session.getEmpleadoActual();
            lblBienvenida.setText("Bienvenido, " + empleado.getNombres());
            lblTipoCuenta.setText("Empleado · " + empleado.getNombrePuesto());
            construirMenuEmpleado(empleado);
        } else if (Session.esCliente()) {
            Cliente cliente = Session.getClienteActual();
            lblBienvenida.setText("Bienvenido, " + cliente.getNombres());
            lblTipoCuenta.setText(cliente.isEsVip() ? "Cliente VIP" : "Cliente");
            construirMenuCliente();
        }
    }

    private void construirMenuEmpleado(Empleado empleado) {
        agregarBotonAccion(empleado, Accion.VER_CARTELERA, () -> new ViewFactory().viewComprarBoletos());
        agregarBotonAccion(empleado, Accion.VERIFICAR_ENTRADA, () -> new ViewFactory().viewVerificarEntrada());
        agregarBotonAccion(empleado, Accion.REGISTRAR_VENTA, () -> new ViewFactory().viewRegistrarVenta());
        agregarBotonAccion(empleado, Accion.ADMINISTRAR_FUNCIONES_SALAS, () -> new ViewFactory().viewAdministrarFuncionesSalas());
        agregarBotonAccion(empleado, Accion.ADMINISTRAR_PELICULAS, () -> new ViewFactory().viewAdministrarPeliculas());
        agregarBotonAccion(empleado, Accion.VER_REPORTES, () -> new ViewFactory().viewReportes());
        agregarBotonAccion(empleado, Accion.VER_GANANCIAS, () -> new ViewFactory().viewGanancias());
        agregarBotonAccion(empleado, Accion.CAMBIAR_PRECIO, () -> new ViewFactory().viewCambiarPrecio());
        // "Dar de baja a un empleado" ya NO es un boton aparte: vive dentro
        // de "Gestion de empleados" (mas abajo), donde tiene sentido junto
        // con crear y editar empleados en un solo lugar.

        // Solo el Dueño ve esto -- pantallas administrativas exclusivas de jerarquía 1
        if (empleado.getNivelJerarquico() == 1) {
            Button btnGestionUsuarios = crearBotonSidebar("Gestión de empleados");
            btnGestionUsuarios.setOnAction(e -> new ViewFactory().viewManageUsers());
            vboxSidebar.getChildren().add(btnGestionUsuarios);

            Button btnSolicitudes = crearBotonSidebar("Solicitudes pendientes");
            btnSolicitudes.setOnAction(e -> new ViewFactory().viewSolicitudes());
            vboxSidebar.getChildren().add(btnSolicitudes);
        }
    }

    private void construirMenuCliente() {
        Button btnComprar = crearBotonSidebar("Comprar boletos");
        btnComprar.setOnAction(e -> new ViewFactory().viewComprarBoletos());
        vboxSidebar.getChildren().add(btnComprar);
    }

    /**
     * Agrega el boton SOLO si el empleado puede verlo. Al hacer clic,
     * la accion pasa por SolicitudService.intentar(...): si su nivel
     * alcanza, corre 'siEjecuta' directo; si no, genera una Solicitud
     * en vez de ejecutar nada.
     */
    private void agregarBotonAccion(Empleado empleado, Accion accion, Runnable siEjecuta) {
        if (!permisoService.puedeVer(empleado, accion)) {
            return;
        }
        Button boton = crearBotonSidebar(accion.getDescripcion());
        boton.setOnAction(e -> manejarAccionSensible(empleado, accion, siEjecuta));
        vboxSidebar.getChildren().add(boton);
    }

    private void manejarAccionSensible(Empleado empleado, Accion accion, Runnable siEjecuta) {
        ResultadoIntento resultado = solicitudService.intentar(empleado, accion, siEjecuta);
        switch (resultado) {
            case EJECUTADA -> {
                // siEjecuta ya corrio y ya dio su propio feedback (alerta o navegacion)
            }
            case SOLICITADA -> alertInfo.viewAlert("INFORMATION", "SOLICITUD ENVIADA",
                    "Pendiente de aprobación del Dueño",
                    "Tu nivel actual no permite ejecutar \"" + accion.getDescripcion()
                    + "\" directamente. Se envió una solicitud para que el Dueño la apruebe.");
            case NO_AUTORIZADO -> alertInfo.viewAlert("ERROR", "SIN PERMISO",
                    "Acción no autorizada",
                    "No tienes permiso para realizar esta acción.");
        }
    }

    private Button crearBotonSidebar(String texto) {
        Button boton = new Button(texto);
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setWrapText(true);
        boton.getStyleClass().add("eva-button-ghost");
        return boton;
    }

    private void mostrarEnConstruccion(String nombreVista) {
        alertInfo.viewAlert("INFORMATION", "EN CONSTRUCCIÓN", nombreVista,
                "Esta sección todavía no tiene pantalla propia -- por ahora este botón "
                + "solo confirma que el permiso y/o la solicitud ya funcionan.");
    }

    /** "Ver cartelera" ya tiene datos reales detras (sp_obtener_cartelera), asi que
     *  en vez de un aviso generico mostramos la info real en el mismo Alert. */
    private void mostrarCarteleraRapida() {
        List<Funcion> cartelera = funcionRepo.obtenerCartelera();
        if (cartelera.isEmpty()) {
            alertInfo.viewAlert("INFORMATION", "CARTELERA", "Sin funciones", "No hay funciones programadas todavía.");
            return;
        }
        StringBuilder texto = new StringBuilder();
        for (Funcion f : cartelera) {
            texto.append(f.getTituloPelicula()).append("  -  ").append(f.getNombreSala())
                    .append("  -  ").append(f.getFecha()).append(" ").append(f.getHora())
                    .append("  -  Q").append(f.getPrecioBase()).append("\n");
        }
        alertInfo.viewAlert("INFORMATION", "CARTELERA", "Funciones programadas", texto.toString());
    }

    @FXML
    public void onCerrarSesion(MouseEvent event) {
        Session.cerrarSesion();
        new ViewFactory().viewLogin();
    }
}
