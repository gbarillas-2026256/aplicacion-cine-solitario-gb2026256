package org.cinekinal.system.controller;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.cinekinal.system.model.CashClosingDetail;
import org.cinekinal.system.model.CashClosingSaveStatus;
import org.cinekinal.system.model.Employee;
import org.cinekinal.system.repository.CashClosingRepository;
import org.cinekinal.system.service.CashClosingService;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

/**
 * Cash closing controller for cashier employees at shift end.
 */
public class CashClosingController implements Initializable {

    @FXML
    private Label lblEmployee;
    @FXML
    private Label lblDate;
    @FXML
    private Label lblTicketsSold;
    @FXML
    private Label lblTotalAdmissions;
    @FXML
    private Label lblTotalConcessions;
    @FXML
    private Label lblTotalGrand;

    @FXML
    private TableView<CashClosingRepository.TicketsPerMovie> tableAdmissions;
    @FXML
    private TableColumn<CashClosingRepository.TicketsPerMovie, String> colMovie;
    @FXML
    private TableColumn<CashClosingRepository.TicketsPerMovie, String> colTickets;
    @FXML
    private TableColumn<CashClosingRepository.TicketsPerMovie, String> colMovieTotal;

    @FXML
    private ComboBox<String> cmbCategory;
    @FXML
    private TextField txtDescription;
    @FXML
    private TextField txtQuantity;
    @FXML
    private TextField txtUnitPrice;

    @FXML
    private TableView<CashClosingDetail> tableConcessions;
    @FXML
    private TableColumn<CashClosingDetail, String> colCategory;
    @FXML
    private TableColumn<CashClosingDetail, String> colDescription;
    @FXML
    private TableColumn<CashClosingDetail, String> colQuantity;
    @FXML
    private TableColumn<CashClosingDetail, String> colUnitPrice;
    @FXML
    private TableColumn<CashClosingDetail, String> colSubtotal;

    @FXML
    private TextArea txtNotes;

    private final CashClosingService closingService = new CashClosingService();
    private final AlertInformation alertInfo = new AlertInformation();

    private final List<CashClosingDetail> details = new ArrayList<>();
    private Date closingDate;
    private BigDecimal totalAdmissions = BigDecimal.ZERO;
    private int ticketsSold = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!Session.isEmployee()) {
            alertInfo.viewAlert("ERROR", "SOLO EMPLEADOS", "Acceso restringido",
                    "El corte de caja lo realiza el personal del cine.");
            new ViewFactory().viewMainMenu();
            return;
        }

        Employee employee = Session.getCurrentEmployee();
        if (employee.getHierarchyLevel() == 1) {
            alertInfo.viewAlert("INFORMATION", "FUNCIÓN OPERATIVA", "Corte exclusivo de empleados de turno",
                    "El corte de caja es realizado por los empleados de taquilla/dulcería al cerrar su turno.\nComo Dueño, puedes consultar todos los cortes y reportes en 'Ver ganancias'.");
            new ViewFactory().viewMainMenu();
            return;
        }
        closingDate = Date.valueOf(LocalDate.now());

        lblEmployee.setText(employee.getFirstName() + " " + employee.getLastName()
                + " (" + employee.getPositionName() + ")");
        lblDate.setText(closingDate.toString());

        initTables();
        cmbCategory.setItems(FXCollections.observableArrayList(
                "Combo", "Palomitas", "Palomera coleccionable", "Bebidas", "Comestibles / Golosinas", "Promociones", "Otro"));
        cmbCategory.getSelectionModel().selectFirst();

        loadDailyAdmissions();
        updateTotals();
    }

    private void initTables() {
        colMovie.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        colTickets.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getTicketsSold())));
        colMovieTotal.setCellValueFactory(d -> new SimpleStringProperty(String.format("Q %.2f", d.getValue().getTotal())));

        colCategory.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory()));
        colDescription.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        colQuantity.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));
        colUnitPrice.setCellValueFactory(d -> new SimpleStringProperty(String.format("Q %.2f", d.getValue().getUnitPrice())));
        colSubtotal.setCellValueFactory(d -> new SimpleStringProperty(String.format("Q %.2f", d.getValue().getSubtotal())));
    }

    private void loadDailyAdmissions() {
        CashClosingRepository.DailyTicketsSummary summary = closingService.getDailyTicketsSummary(closingDate);
        totalAdmissions = summary.totalTickets;
        ticketsSold = summary.ticketsSold;

        lblTicketsSold.setText(String.valueOf(ticketsSold));
        tableAdmissions.setItems(FXCollections.observableArrayList(
                closingService.getTicketsPerMovie(closingDate)));
    }

    private BigDecimal calculateConcessionsTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CashClosingDetail d : details) {
            total = total.add(d.getSubtotal());
        }
        return total;
    }

    private void updateTotals() {
        BigDecimal concessions = calculateConcessionsTotal();
        lblTotalAdmissions.setText(String.format("Q %.2f", totalAdmissions));
        lblTotalConcessions.setText(String.format("Q %.2f", concessions));
        lblTotalGrand.setText(String.format("Q %.2f", totalAdmissions.add(concessions)));
        tableConcessions.setItems(FXCollections.observableArrayList(details));
    }

    @FXML
    public void onAddLine(ActionEvent event) {
        String category = cmbCategory.getValue();
        String description = txtDescription.getText().trim();
        String quantityStr = txtQuantity.getText().trim();
        String priceStr = txtUnitPrice.getText().trim();

        if (description.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty()) {
            alertInfo.viewAlert("WARNING", "CAMPOS INCOMPLETOS", "Faltan datos",
                    "Completa descripción, cantidad y precio unitario.");
            return;
        }

        int quantity;
        BigDecimal price;
        try {
            quantity = Integer.parseInt(quantityStr);
            price = new BigDecimal(priceStr);
        } catch (NumberFormatException e) {
            alertInfo.viewAlert("WARNING", "DATOS INVÁLIDOS", "Revisa los números",
                    "La cantidad debe ser un número entero y el precio un número (ej: 25.50).");
            return;
        }

        if (quantity <= 0 || price.compareTo(BigDecimal.ZERO) <= 0) {
            alertInfo.viewAlert("WARNING", "DATOS INVÁLIDOS", "Valores fuera de rango",
                    "La cantidad y el precio deben ser mayores a cero.");
            return;
        }

        details.add(new CashClosingDetail(category, description, quantity, price));
        clearLineForm();
        updateTotals();
    }

    @FXML
    public void onRemoveLine(ActionEvent event) {
        CashClosingDetail selected = tableConcessions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alertInfo.viewAlert("WARNING", "SIN SELECCIÓN", "Ninguna línea seleccionada",
                    "Selecciona una línea de la tabla para quitarla.");
            return;
        }
        details.remove(selected);
        updateTotals();
    }

    @FXML
    public void onCloseCash(ActionEvent event) {
        Employee employee = Session.getCurrentEmployee();

        CashClosingSaveStatus result = closingService.saveClosing(
                employee.getIdEmployee(), closingDate, totalAdmissions, ticketsSold,
                txtNotes.getText().trim(), details);

        switch (result) {
            case SAVED -> {
                alertInfo.viewAlert("INFORMATION", "CORTE Y REPORTE ENVIADO", "Cierre de turno registrado con éxito",
                        "Empleado: " + employee.getFirstName() + " " + employee.getLastName() + "\n"
                        + "Fecha: " + closingDate + "\n"
                        + "Entradas: " + lblTotalAdmissions.getText() + " (" + ticketsSold + " boletos)\n"
                        + "Dulcería: " + lblTotalConcessions.getText() + "\n"
                        + "TOTAL GENERAL DEL DÍA: " + lblTotalGrand.getText() + "\n\n"
                        + "El reporte de incidencias y el balance financiero ya están disponibles para el Dueño en 'Reportes' y 'Ganancias'.");
                new ViewFactory().viewMainMenu();
            }
            case ALREADY_EXISTS_TODAY -> alertInfo.viewAlert("WARNING", "CORTE YA REALIZADO",
                    "Ya cerraste caja hoy",
                    "Solo se puede hacer un corte por día. Si necesitas modificarlo, "
                    + "pídele al Dueño que lo revise.");
            case SAVE_ERROR -> alertInfo.viewAlert("ERROR", "ERROR AL GUARDAR",
                    "No se pudo guardar el corte",
                    "Ocurrió un error al guardar. Verifica la conexión a la base de datos.");
        }
    }

    private void clearLineForm() {
        txtDescription.clear();
        txtQuantity.clear();
        txtUnitPrice.clear();
        cmbCategory.getSelectionModel().selectFirst();
    }

    @FXML
    public void onBack(ActionEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
