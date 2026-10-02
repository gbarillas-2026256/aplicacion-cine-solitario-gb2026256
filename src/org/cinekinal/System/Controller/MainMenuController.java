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
        addActionButton(employee, Action.VIEW_BILLBOARD, () -> new ViewFactory().viewBuyTickets());
        addActionButton(employee, Action.VERIFY_ENTRY, () -> new ViewFactory().viewVerifyEntry());
        addActionButton(employee, Action.BOX_OFFICE_SALE, () -> new ViewFactory().viewBoxOfficeSale());

        if (employee.getHierarchyLevel() > 1) {
            addActionButton(employee, Action.CASH_CLOSING, () -> new ViewFactory().viewCashClosing());
        }

        addActionButton(employee, Action.MANAGE_SHOWTIMES_THEATERS, () -> new ViewFactory().viewManageShowtimesTheaters());
        addActionButton(employee, Action.MANAGE_MOVIES, () -> new ViewFactory().viewManageMovies());
        addActionButton(employee, Action.VIEW_REPORTS, () -> new ViewFactory().viewReports());
        addActionButton(employee, Action.VIEW_EARNINGS, () -> new ViewFactory().viewEarnings());

        if (permissionService.canView(employee, Action.CHANGE_PRICE)) {
            Button btnChangePrice = createSidebarButton(Action.CHANGE_PRICE.getDescription());
            btnChangePrice.setOnAction(e -> new ViewFactory().viewChangePrice());
            vboxSidebar.getChildren().add(btnChangePrice);
        }

        if (employee.getHierarchyLevel() == 1) {
            Button btnManageUsers = createSidebarButton("Gestión de empleados");
            btnManageUsers.setOnAction(e -> new ViewFactory().viewManageUsers());
            vboxSidebar.getChildren().add(btnManageUsers);

            Button btnRequests = createSidebarButton("Solicitudes pendientes");
            btnRequests.setOnAction(e -> new ViewFactory().viewRequests());
            vboxSidebar.getChildren().add(btnRequests);
        }

        Button btnMyRequests = createSidebarButton("Mis solicitudes");
        btnMyRequests.setOnAction(e -> new ViewFactory().viewMyRequests());
        vboxSidebar.getChildren().add(btnMyRequests);

        Button btnMessages = createSidebarButton("Mensajes");
        btnMessages.setOnAction(e -> new ViewFactory().viewMessages());
        vboxSidebar.getChildren().add(btnMessages);
    }

    private void buildCustomerMenu() {
        Button btnBuy = createSidebarButton("Comprar boletos");
        btnBuy.setOnAction(e -> new ViewFactory().viewBuyTickets());
        vboxSidebar.getChildren().add(btnBuy);

        Customer customer = Session.getCurrentCustomer();
        if (customer != null) {
            try {
                List<Ticket> tickets = ticketRepo.getTicketsByCustomer(customer.getIdCustomer());
                if (!tickets.isEmpty()) {
                    String buttonText = tickets.size() == 1 ? "🎟️ Ver mi Boleto" : "🎟️ Mis Boletos (" + tickets.size() + ")";
                    Button btnMyTickets = createSidebarButton(buttonText);
                    btnMyTickets.setOnAction(e -> new ViewFactory().viewMyTickets());
                    vboxSidebar.getChildren().add(btnMyTickets);
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
