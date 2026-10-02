package org.cinekinal.system.controller;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.cinekinal.system.model.Action;
import org.cinekinal.system.model.AttemptResult;
import org.cinekinal.system.model.Customer;
import org.cinekinal.system.model.Employee;
import org.cinekinal.system.model.Showtime;
import org.cinekinal.system.model.Ticket;
import org.cinekinal.system.repository.ShowtimeRepository;
import org.cinekinal.system.repository.TicketRepository;
import org.cinekinal.system.service.PermissionService;
import org.cinekinal.system.service.RequestService;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

/**
 * Hub view reached after user login.
 * The sidebar is built dynamically according to the user's role and hierarchy level.
 */
public class MainMenuController implements Initializable {

    @FXML
    private Label lblWelcome;
    @FXML
    private Label lblAccountType;
    @FXML
    private VBox vboxSidebar;

    private final PermissionService permissionService = new PermissionService();
    private final RequestService requestService = new RequestService();
    private final ShowtimeRepository showtimeRepo = new ShowtimeRepository();
    private final TicketRepository ticketRepo = new TicketRepository();
    private final AlertInformation alertInfo = new AlertInformation();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        vboxSidebar.getChildren().clear();

        if (Session.isEmployee()) {
            Employee employee = Session.getCurrentEmployee();
            lblWelcome.setText("Bienvenido, " + employee.getFirstName());
            lblAccountType.setText("Empleado · " + employee.getPositionName());
            buildEmployeeMenu(employee);
        } else if (Session.isCustomer()) {
            Customer customer = Session.getCurrentCustomer();
            lblWelcome.setText("Bienvenido, " + customer.getFirstName());
            lblAccountType.setText(customer.isVip() ? "Cliente VIP" : "Cliente");
            buildCustomerMenu();
        }
    }

    private void buildEmployeeMenu(Employee employee) {
        addActionButton(employee, Action.VIEW_BILLBOARD, () -> new ViewFactory().viewComprarBoletos());
        addActionButton(employee, Action.VERIFY_ENTRY, () -> new ViewFactory().viewVerificarEntrada());
        addActionButton(employee, Action.BOX_OFFICE_SALE, () -> new ViewFactory().viewRegistrarVenta());

        if (employee.getHierarchyLevel() > 1) {
            addActionButton(employee, Action.CASH_CLOSING, () -> new ViewFactory().viewCorteCaja());
        }

        addActionButton(employee, Action.MANAGE_SHOWTIMES_THEATERS, () -> new ViewFactory().viewAdministrarFuncionesSalas());
        addActionButton(employee, Action.MANAGE_MOVIES, () -> new ViewFactory().viewAdministrarPeliculas());
        addActionButton(employee, Action.VIEW_REPORTS, () -> new ViewFactory().viewReportes());
        addActionButton(employee, Action.VIEW_EARNINGS, () -> new ViewFactory().viewGanancias());

        if (permissionService.canView(employee, Action.CHANGE_PRICE)) {
            Button btnCambiarPrecio = createSidebarButton(Action.CHANGE_PRICE.getDescription());
            btnCambiarPrecio.setOnAction(e -> new ViewFactory().viewCambiarPrecio());
            vboxSidebar.getChildren().add(btnCambiarPrecio);
        }

        if (employee.getHierarchyLevel() == 1) {
            Button btnGestionUsuarios = createSidebarButton("Gestión de empleados");
            btnGestionUsuarios.setOnAction(e -> new ViewFactory().viewManageUsers());
            vboxSidebar.getChildren().add(btnGestionUsuarios);

            Button btnSolicitudes = createSidebarButton("Solicitudes pendientes");
            btnSolicitudes.setOnAction(e -> new ViewFactory().viewSolicitudes());
            vboxSidebar.getChildren().add(btnSolicitudes);
        }

        Button btnMisSolicitudes = createSidebarButton("Mis solicitudes");
        btnMisSolicitudes.setOnAction(e -> new ViewFactory().viewMisSolicitudes());
        vboxSidebar.getChildren().add(btnMisSolicitudes);

        Button btnMensajes = createSidebarButton("Mensajes");
        btnMensajes.setOnAction(e -> new ViewFactory().viewMensajes());
        vboxSidebar.getChildren().add(btnMensajes);
    }

    private void buildCustomerMenu() {
        Button btnComprar = createSidebarButton("Comprar boletos");
        btnComprar.setOnAction(e -> new ViewFactory().viewComprarBoletos());
        vboxSidebar.getChildren().add(btnComprar);

        Customer customer = Session.getCurrentCustomer();
        if (customer != null) {
            try {
                List<Ticket> tickets = ticketRepo.getTicketsByCustomer(customer.getIdCustomer());
                if (!tickets.isEmpty()) {
                    String buttonText = tickets.size() == 1 ? "🎟️ Ver mi Boleto" : "🎟️ Mis Boletos (" + tickets.size() + ")";
                    Button btnMisBoletos = createSidebarButton(buttonText);
                    btnMisBoletos.setOnAction(e -> new ViewFactory().viewMisBoletos());
                    vboxSidebar.getChildren().add(btnMisBoletos);
                }
            } catch (Exception e) {
                System.out.println("Aviso al consultar boletos del cliente: " + e.getMessage());
            }
        }
    }

    private void addActionButton(Employee employee, Action action, Runnable onExecute) {
        if (!permissionService.canView(employee, action)) {
            return;
        }
        Button button = createSidebarButton(action.getDescription());
        button.setOnAction(e -> handleSensitiveAction(employee, action, onExecute));
        vboxSidebar.getChildren().add(button);
    }

    private void handleSensitiveAction(Employee employee, Action action, Runnable onExecute) {
        String reason = "";

        if (permissionService.needsRequest(employee, action)) {
            Optional<String> promptResult = promptReason(action);
            if (promptResult.isEmpty()) {
                return;
            }
            reason = promptResult.get();
        }

        AttemptResult result = requestService.attempt(employee, action, reason, onExecute);
        switch (result) {
            case EXECUTED -> {
            }
            case REQUESTED -> alertInfo.viewAlert("INFORMATION", "SOLICITUD ENVIADA",
                    "Pendiente de aprobación del Dueño",
                    "Tu nivel actual no permite ejecutar \"" + action.getDescription()
                    + "\" directamente. Se envió tu solicitud con el motivo indicado -- puedes revisar "
                    + "su estado más tarde en \"Mis solicitudes\".");
            case UNAUTHORIZED -> alertInfo.viewAlert("ERROR", "SIN PERMISO",
                    "Acción no autorizada",
                    "No tienes permiso para realizar esta acción.");
        }
    }

    private Optional<String> promptReason(Action action) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("EXPLICA TU SOLICITUD");
        dialog.setHeaderText("\"" + action.getDescription() + "\" necesita aprobación del Dueño");
        dialog.setContentText("¿Por qué necesitas realizar esta acción?");

        Optional<String> response = dialog.showAndWait();
        if (response.isEmpty()) {
            return Optional.empty();
        }
        String reason = response.get().trim();
        if (reason.isEmpty()) {
            alertInfo.viewAlert("WARNING", "MOTIVO REQUERIDO", "Explica el motivo",
                    "Escribe una breve razón antes de enviar la solicitud.");
            return Optional.empty();
        }
        return Optional.of(reason);
    }

    private Button createSidebarButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setWrapText(true);
        button.getStyleClass().add("eva-button-ghost");
        return button;
    }

    @FXML
    public void onLogout(MouseEvent event) {
        Session.logout();
        new ViewFactory().viewLogin();
    }

    public void onCerrarSesion(MouseEvent event) {
        onLogout(event);
    }
}
