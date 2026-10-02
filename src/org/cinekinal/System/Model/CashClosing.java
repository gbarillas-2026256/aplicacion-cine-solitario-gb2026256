package org.cinekinal.system.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * CashClosing entity representing a cashier shift closing and financial tally.
 */
public class CashClosing {
    private String idClosing;
    private Date closingDate;
    private BigDecimal totalTickets;
    private int ticketsSold;
    private BigDecimal totalConcessions;
    private BigDecimal grandTotal;
    private String notes;
    private Timestamp createdAt;
    private String employeeFirstName;
    private String employeeLastName;
    private String employeePosition;

    public CashClosing() {
    }

    public String getIdClosing() {
        return idClosing;
    }

    public void setIdClosing(String idClosing) {
        this.idClosing = idClosing;
    }

    public Date getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(Date closingDate) {
        this.closingDate = closingDate;
    }

    public BigDecimal getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(BigDecimal totalTickets) {
        this.totalTickets = totalTickets;
    }

    public int getTicketsSold() {
        return ticketsSold;
    }

    public void setTicketsSold(int ticketsSold) {
        this.ticketsSold = ticketsSold;
    }

    public BigDecimal getTotalConcessions() {
        return totalConcessions;
    }

    public void setTotalConcessions(BigDecimal totalConcessions) {
        this.totalConcessions = totalConcessions;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getEmployeeFirstName() {
        return employeeFirstName;
    }

    public void setEmployeeFirstName(String employeeFirstName) {
        this.employeeFirstName = employeeFirstName;
    }

    public String getEmployeeLastName() {
        return employeeLastName;
    }

    public void setEmployeeLastName(String employeeLastName) {
        this.employeeLastName = employeeLastName;
    }

    public String getEmployeePosition() {
        return employeePosition;
    }

    public void setEmployeePosition(String employeePosition) {
        this.employeePosition = employeePosition;
    }

    public String getEmployeeFullName() {
        return (employeeFirstName != null ? employeeFirstName : "") + " "
                + (employeeLastName != null ? employeeLastName : "").trim();
    }

    // Compatibility
    public String getIdCorte() { return idClosing; }
    public void setIdCorte(String id) { this.idClosing = id; }
    public Date getFechaCorte() { return closingDate; }
    public void setFechaCorte(Date d) { this.closingDate = d; }
    public BigDecimal getTotalEntradas() { return totalTickets; }
    public void setTotalEntradas(BigDecimal t) { this.totalTickets = t; }
    public int getBoletosVendidos() { return ticketsSold; }
    public void setBoletosVendidos(int b) { this.ticketsSold = b; }
    public BigDecimal getTotalDulceria() { return totalConcessions; }
    public void setTotalDulceria(BigDecimal t) { this.totalConcessions = t; }
    public BigDecimal getTotalGeneral() { return grandTotal; }
    public void setTotalGeneral(BigDecimal t) { this.grandTotal = t; }
    public String getObservaciones() { return notes; }
    public void setObservaciones(String o) { this.notes = o; }
    public Timestamp getFechaRegistro() { return createdAt; }
    public void setFechaRegistro(Timestamp f) { this.createdAt = f; }
    public String getEmpleadoNombreCompleto() { return getEmployeeFullName(); }
    public String getEmpleadoPuesto() { return employeePosition; }
}
