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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.cinekinal.system.model.CashClosing;
import org.cinekinal.system.model.CashClosingDetail;
import org.cinekinal.system.repository.ReportRepository;
import org.cinekinal.system.repository.ReportRepository.ReportRow;
import org.cinekinal.system.repository.ReportRepository.TodaySummary;
import org.cinekinal.system.service.CashClosingService;
import org.cinekinal.system.utils.ViewFactory;

/**
 * Controller for cinema operational and shift reports.
 */
public class ReportsController implements Initializable {

    @FXML
    private Label lblTodayAttendance;
    @FXML
    private Label lblActiveShowtimes;

    // Occupancy Table
    @FXML
    private TableView<ReportRow> tableOccupancy;
    @FXML
    private TableColumn<ReportRow, String> colOccupancyMovie;
    @FXML
    private TableColumn<ReportRow, String> colOccupancyTheaterSchedule;
    @FXML
    private TableColumn<ReportRow, String> colOccupancySeats;
    @FXML
    private TableColumn<ReportRow, String> colOccupancyPercentage;

    // Attendance Ranking Table
    @FXML
    private TableView<ReportRow> tableTopAttendance;
    @FXML
    private TableColumn<ReportRow, String> colAttendanceRanking;
    @FXML
    private TableColumn<ReportRow, String> colAttendanceTickets;

    // Tab 3: Shift Reports and Cash Register Incidents
    @FXML
    private DatePicker dpShiftStart;
    @FXML
    private DatePicker dpShiftEnd;
    @FXML
    private TextField txtSearchShift;

    @FXML
    private TableView<CashClosing> tableShifts;
    @FXML
    private TableColumn<CashClosing, String> colShiftDate;
    @FXML
    private TableColumn<CashClosing, String> colShiftRecord;
    @FXML
    private TableColumn<CashClosing, String> colShiftEmployee;
    @FXML
    private TableColumn<CashClosing, String> colShiftPosition;
    @FXML
    private TableColumn<CashClosing, String> colShiftTickets;
    @FXML
    private TableColumn<CashClosing, String> colShiftTotal;
    @FXML
    private TableColumn<CashClosing, String> colShiftSummaryMessage;

    @FXML
    private Label lblShiftResponsibleBadge;
    @FXML
    private TextArea txtShiftFullMessage;
    @FXML
    private Label lblShiftAdmissions;
    @FXML
    private Label lblShiftConcessions;
    @FXML
    private Label lblShiftGrandTotal;

    @FXML
    private TableView<CashClosingDetail> tableShiftDetails;
    @FXML
    private TableColumn<CashClosingDetail, String> colDetCategory;
    @FXML
    private TableColumn<CashClosingDetail, String> colDetDescription;
    @FXML
    private TableColumn<CashClosingDetail, String> colDetQuantity;
    @FXML
    private TableColumn<CashClosingDetail, String> colDetPrice;
    @FXML
    private TableColumn<CashClosingDetail, String> colDetSubtotal;

    private final ReportRepository reportRepo = new ReportRepository();
    private final CashClosingService closingService = new CashClosingService();
    private List<CashClosing> currentShiftsList = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colOccupancyMovie.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn1()));
        colOccupancyTheaterSchedule.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn2()));
        colOccupancySeats.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn3()));
        colOccupancyPercentage.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn4()));

        colAttendanceRanking.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn1()));
        colAttendanceTickets.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn2()));

        initShiftsTable();

        loadData();
        onFilterShifts(null);
    }

    private void initShiftsTable() {
        dpShiftStart.setValue(LocalDate.now().minusMonths(1));
        dpShiftEnd.setValue(LocalDate.now().plusDays(1));

        colShiftDate.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getClosingDate())));
        colShiftRecord.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCreatedAt() != null ? d.getValue().getCreatedAt().toString() : "—"));
        colShiftEmployee.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmployeeFullName()));
        colShiftPosition.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmployeePosition()));
        colShiftTickets.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getTicketsSold())));
        colShiftTotal.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("Q %.2f", d.getValue().getGrandTotal() != null ? d.getValue().getGrandTotal() : BigDecimal.ZERO)));

        colShiftSummaryMessage.setCellValueFactory(d -> {
            String obs = d.getValue().getNotes();
            if (obs == null || obs.isBlank()) {
                return new SimpleStringProperty("— Sin incidencias reportadas —");
            }
            obs = obs.trim().replace("\n", " ");
            return new SimpleStringProperty(obs.length() > 45 ? obs.substring(0, 42) + "..." : obs);
        });

        colDetCategory.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory()));
        colDetDescription.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        colDetQuantity.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));
        colDetPrice.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("Q %.2f", d.getValue().getUnitPrice() != null ? d.getValue().getUnitPrice() : BigDecimal.ZERO)));
        colDetSubtotal.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("Q %.2f", d.getValue().getSubtotal() != null ? d.getValue().getSubtotal() : BigDecimal.ZERO)));

        tableShifts.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                lblShiftResponsibleBadge.setText("Empleado: —");
                txtShiftFullMessage.clear();
                lblShiftAdmissions.setText("Entradas: Q 0.00");
                lblShiftConcessions.setText("Dulcería: Q 0.00");
                lblShiftGrandTotal.setText("Total Turno: Q 0.00");
                tableShiftDetails.getItems().clear();
                return;
            }

            lblShiftResponsibleBadge.setText("Empleado: " + newVal.getEmployeeFullName() + " (" + newVal.getEmployeePosition() + ")");
            String obsText = newVal.getNotes();
            if (obsText != null && !obsText.isBlank()) {
                txtShiftFullMessage.setText(obsText);
            } else {
                txtShiftFullMessage.setText("(El empleado no registró notas ni incidencias en este turno. El cuadre de caja fue cerrado con normalidad).");
            }

            lblShiftAdmissions.setText(String.format("Entradas: Q %.2f", newVal.getTotalTickets() != null ? newVal.getTotalTickets() : BigDecimal.ZERO));
            lblShiftConcessions.setText(String.format("Dulcería: Q %.2f", newVal.getTotalConcessions() != null ? newVal.getTotalConcessions() : BigDecimal.ZERO));
            lblShiftGrandTotal.setText(String.format("Total Turno: Q %.2f", newVal.getGrandTotal() != null ? newVal.getGrandTotal() : BigDecimal.ZERO));

            tableShiftDetails.setItems(FXCollections.observableArrayList(closingService.getDetails(newVal.getIdClosing())));
        });

        txtSearchShift.textProperty().addListener((observable, oldValue, newValue) -> filterShiftsInMemory(newValue));
    }

    @FXML
    public void onFilterShifts(ActionEvent event) {
        LocalDate start = dpShiftStart.getValue();
        LocalDate end = dpShiftEnd.getValue();
        if (start == null || end == null) {
            return;
        }

        currentShiftsList = closingService.getByDateRange(Date.valueOf(start), Date.valueOf(end));
        filterShiftsInMemory(txtSearchShift.getText());

        tableShiftDetails.getItems().clear();
        txtShiftFullMessage.clear();
        lblShiftResponsibleBadge.setText("Empleado: —");
        lblShiftAdmissions.setText("Entradas: Q 0.00");
        lblShiftConcessions.setText("Dulcería: Q 0.00");
        lblShiftGrandTotal.setText("Total Turno: Q 0.00");
    }

    private void filterShiftsInMemory(String search) {
        if (search == null || search.isBlank()) {
            tableShifts.setItems(FXCollections.observableArrayList(currentShiftsList));
            return;
        }
        String filter = search.toLowerCase().trim();
        List<CashClosing> filtered = new ArrayList<>();
        for (CashClosing c : currentShiftsList) {
            boolean matchEmployee = c.getEmployeeFullName() != null && c.getEmployeeFullName().toLowerCase().contains(filter);
            boolean matchPosition = c.getEmployeePosition() != null && c.getEmployeePosition().toLowerCase().contains(filter);
            boolean matchNotes = c.getNotes() != null && c.getNotes().toLowerCase().contains(filter);
            if (matchEmployee || matchPosition || matchNotes) {
                filtered.add(c);
            }
        }
        tableShifts.setItems(FXCollections.observableArrayList(filtered));
    }

    private void loadData() {
        TodaySummary today = reportRepo.getTodaySummary();
        lblTodayAttendance.setText(today.ticketsToday + " espectadores");

        List<ReportRow> occupancy = reportRepo.getBillboardOccupancy();
        tableOccupancy.setItems(FXCollections.observableArrayList(occupancy));
        lblActiveShowtimes.setText(occupancy.size() + " funciones registradas");

        List<ReportRow> topMovies = reportRepo.getTopMovies(null, null, 10);
        tableTopAttendance.setItems(FXCollections.observableArrayList(topMovies));
    }

    @FXML
    public void onRefreshOccupancy(ActionEvent event) {
        loadData();
    }

    @FXML
    public void onBack(ActionEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
