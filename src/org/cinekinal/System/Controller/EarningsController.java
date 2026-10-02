package org.cinekinal.system.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.cinekinal.system.model.CashClosing;
import org.cinekinal.system.model.CashClosingDetail;
import org.cinekinal.system.repository.ReportRepository;
import org.cinekinal.system.repository.ReportRepository.ReportRow;
import org.cinekinal.system.repository.ReportRepository.TodaySummary;
import org.cinekinal.system.service.CashClosingService;
import org.cinekinal.system.utils.ViewFactory;

/**
 * Controller for cinema earnings and financial analytics.
 */
public class EarningsController implements Initializable {

    @FXML
    private Label lblTodayIncome;
    @FXML
    private Label lblTodayTickets;
    @FXML
    private Label lblAverageTicket;

    // Tab 1: Financial Charts
    @FXML
    private PieChart chartDistribution;
    @FXML
    private BarChart<String, Number> chartMovies;
    @FXML
    private CategoryAxis axisMovieX;
    @FXML
    private NumberAxis axisMovieY;

    // Tab 2: Income by Date Range
    @FXML
    private DatePicker dpStart;
    @FXML
    private DatePicker dpEnd;
    @FXML
    private Label lblTotalPeriod;
    @FXML
    private TableView<ReportRow> tableIncome;
    @FXML
    private TableColumn<ReportRow, String> colIncomeDate;
    @FXML
    private TableColumn<ReportRow, String> colIncomeTickets;
    @FXML
    private TableColumn<ReportRow, String> colIncomeTotal;

    // Tab 3: Income by Movie
    @FXML
    private TableView<ReportRow> tableMovieIncome;
    @FXML
    private TableColumn<ReportRow, String> colMovieTitle;
    @FXML
    private TableColumn<ReportRow, String> colMovieTickets;
    @FXML
    private TableColumn<ReportRow, String> colMovieIncome;

    // Tab 4: Cash closings submitted by employees
    @FXML
    private DatePicker dpClosingStart;
    @FXML
    private DatePicker dpClosingEnd;
    @FXML
    private Label lblTotalClosings;
    @FXML
    private Label lblClosingNotes;
    @FXML
    private TableView<CashClosing> tableClosings;
    @FXML
    private TableColumn<CashClosing, String> colClosingDate;
    @FXML
    private TableColumn<CashClosing, String> colClosingEmployee;
    @FXML
    private TableColumn<CashClosing, String> colClosingPosition;
    @FXML
    private TableColumn<CashClosing, String> colClosingTickets;
    @FXML
    private TableColumn<CashClosing, String> colClosingAdmissions;
    @FXML
    private TableColumn<CashClosing, String> colClosingConcessions;
    @FXML
    private TableColumn<CashClosing, String> colClosingTotal;
    @FXML
    private TableView<CashClosingDetail> tableClosingDetails;
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dpStart.setValue(LocalDate.now().minusMonths(1));
        dpEnd.setValue(LocalDate.now().plusDays(1));

        colIncomeDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn1()));
        colIncomeTickets.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn2()));
        colIncomeTotal.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn3()));

        colMovieTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn1()));
        colMovieTickets.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn2()));
        colMovieIncome.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColumn3()));

        initClosingsTable();

        loadTodaySummary();
        onFilterIncome(null);
        loadMovieIncome();
        onFilterClosings(null);
        updateCharts();
    }

    private void initClosingsTable() {
        dpClosingStart.setValue(LocalDate.now().minusMonths(1));
        dpClosingEnd.setValue(LocalDate.now());

        colClosingDate.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getClosingDate())));
        colClosingEmployee.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmployeeFullName()));
        colClosingPosition.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmployeePosition()));
        colClosingTickets.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getTicketsSold())));
        colClosingAdmissions.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getTotalTickets())));
        colClosingConcessions.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getTotalConcessions())));
        colClosingTotal.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getGrandTotal())));

        colDetCategory.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory()));
        colDetDescription.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        colDetQuantity.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));
        colDetPrice.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getUnitPrice())));
        colDetSubtotal.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getSubtotal())));

        tableClosings.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                tableClosingDetails.getItems().clear();
                lblClosingNotes.setText("Observaciones: —");
                return;
            }
            tableClosingDetails.setItems(FXCollections.observableArrayList(
                    closingService.getDetails(newVal.getIdClosing())));
            String obsText = newVal.getNotes();
            lblClosingNotes.setText("Observaciones: "
                    + (obsText == null || obsText.isBlank() ? "—" : obsText));
        });
    }

    @FXML
    public void onFilterClosings(ActionEvent event) {
        LocalDate start = dpClosingStart.getValue();
        LocalDate end = dpClosingEnd.getValue();
        if (start == null || end == null) {
            return;
        }

        List<CashClosing> closings = closingService.getByDateRange(Date.valueOf(start), Date.valueOf(end));
        tableClosings.setItems(FXCollections.observableArrayList(closings));

        BigDecimal total = BigDecimal.ZERO;
        for (CashClosing closing : closings) {
            if (closing.getGrandTotal() != null) {
                total = total.add(closing.getGrandTotal());
            }
        }
        lblTotalClosings.setText(String.format("Total Cortes: Q %.2f", total));

        tableClosingDetails.getItems().clear();
        lblClosingNotes.setText("Observaciones: —");
    }

    private void loadTodaySummary() {
        TodaySummary today = reportRepo.getTodaySummary();
        lblTodayIncome.setText("Q " + today.incomeToday);
        lblTodayTickets.setText(today.ticketsToday + " boletos");

        if (today.ticketsToday > 0) {
            BigDecimal average = today.incomeToday.divide(BigDecimal.valueOf(today.ticketsToday), 2, RoundingMode.HALF_UP);
            lblAverageTicket.setText("Q " + average);
        } else {
            lblAverageTicket.setText("Q 0.00");
        }
    }

    @FXML
    public void onFilterIncome(ActionEvent event) {
        LocalDate start = dpStart.getValue();
        LocalDate end = dpEnd.getValue();
        Date sqlStart = start != null ? Date.valueOf(start) : null;
        Date sqlEnd = end != null ? Date.valueOf(end) : null;

        List<ReportRow> rows = reportRepo.getDailyIncome(sqlStart, sqlEnd);
        tableIncome.setItems(FXCollections.observableArrayList(rows));

        BigDecimal totalPeriod = BigDecimal.ZERO;
        for (ReportRow r : rows) {
            try {
                String str = r.getColumn3().replace("Q", "").trim();
                totalPeriod = totalPeriod.add(new BigDecimal(str));
            } catch (Exception ignored) {}
        }
        lblTotalPeriod.setText(String.format("Total Período: Q %.2f", totalPeriod));
    }

    private void loadMovieIncome() {
        tableMovieIncome.setItems(FXCollections.observableArrayList(
                reportRepo.getMovieIncome(null, null)));
    }

    @FXML
    public void onUpdateCharts(ActionEvent event) {
        updateCharts();
    }

    private void updateCharts() {
        // 1. Distribution chart (Tickets vs Concessions)
        List<CashClosing> closings = closingService.getByDateRange(
                Date.valueOf(LocalDate.now().minusMonths(6)),
                Date.valueOf(LocalDate.now().plusDays(1)));
        BigDecimal totalAdmissions = BigDecimal.ZERO;
        BigDecimal totalConcessions = BigDecimal.ZERO;

        for (CashClosing c : closings) {
            if (c.getTotalTickets() != null) {
                totalAdmissions = totalAdmissions.add(c.getTotalTickets());
            }
            if (c.getTotalConcessions() != null) {
                totalConcessions = totalConcessions.add(c.getTotalConcessions());
            }
        }

        BigDecimal historicalBoxOffice = BigDecimal.ZERO;
        for (ReportRow row : reportRepo.getMovieIncome(null, null)) {
            try {
                historicalBoxOffice = historicalBoxOffice.add(new BigDecimal(row.getColumn3().replace("Q", "").trim()));
            } catch (Exception ignored) {}
        }
        if (historicalBoxOffice.compareTo(totalAdmissions) > 0) {
            totalAdmissions = historicalBoxOffice;
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        if (totalAdmissions.compareTo(BigDecimal.ZERO) > 0 || totalConcessions.compareTo(BigDecimal.ZERO) > 0) {
            pieData.add(new PieChart.Data(String.format("🎟️ Boletos (Q %.2f)", totalAdmissions), totalAdmissions.doubleValue()));
            pieData.add(new PieChart.Data(String.format("🍿 Dulcería (Q %.2f)", totalConcessions), totalConcessions.doubleValue()));
        } else {
            pieData.add(new PieChart.Data("Sin ventas aún", 1));
        }
        chartDistribution.setData(pieData);

        // 2. Movie revenue bar chart
        chartMovies.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Recaudación (Q)");

        List<ReportRow> movies = reportRepo.getMovieIncome(null, null);
        for (ReportRow p : movies) {
            try {
                double amount = Double.parseDouble(p.getColumn3().replace("Q", "").trim());
                String title = p.getColumn1();
                if (title.length() > 18) {
                    title = title.substring(0, 16) + "..";
                }
                series.getData().add(new XYChart.Data<>(title, amount));
            } catch (Exception ignored) {}
        }
        chartMovies.getData().add(series);
    }

    @FXML
    public void onBack(ActionEvent event) {
        new ViewFactory().viewMainMenu();
    }
}
