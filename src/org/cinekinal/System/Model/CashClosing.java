package org.cinekinal.system.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * Daily cash closing model.
 */
public class CashClosing extends CorteCaja {

    public CashClosing() {
        super();
    }

    public String getIdClosing() {
        return getIdCorte();
    }

    public void setIdClosing(String idClosing) {
        setIdCorte(idClosing);
    }

    public Date getClosingDate() {
        return getFechaCorte();
    }

    public void setClosingDate(Date closingDate) {
        setFechaCorte(closingDate);
    }

    public BigDecimal getTotalTickets() {
        return getTotalEntradas();
    }

    public void setTotalTickets(BigDecimal totalTickets) {
        setTotalEntradas(totalTickets);
    }

    public int getTicketsSold() {
        return getBoletosVendidos();
    }

    public void setTicketsSold(int ticketsSold) {
        setBoletosVendidos(ticketsSold);
    }

    public BigDecimal getTotalConcessions() {
        return getTotalDulceria();
    }

    public void setTotalConcessions(BigDecimal totalConcessions) {
        setTotalDulceria(totalConcessions);
    }

    public BigDecimal getGrandTotal() {
        return getTotalGeneral();
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        setTotalGeneral(grandTotal);
    }

    public String getNotes() {
        return getObservaciones();
    }

    public void setNotes(String notes) {
        setObservaciones(notes);
    }

    public Timestamp getCreatedAt() {
        return getFechaRegistro();
    }

    public void setCreatedAt(Timestamp createdAt) {
        setFechaRegistro(createdAt);
    }

    public String getEmployeeFirstName() {
        return getEmpleadoNombres();
    }

    public void setEmployeeFirstName(String employeeFirstName) {
        setEmpleadoNombres(employeeFirstName);
    }

    public String getEmployeeLastName() {
        return getEmpleadoApellidos();
    }

    public void setEmployeeLastName(String employeeLastName) {
        setEmpleadoApellidos(employeeLastName);
    }

    public String getEmployeePosition() {
        return getEmpleadoPuesto();
    }

    public void setEmployeePosition(String employeePosition) {
        setEmpleadoPuesto(employeePosition);
    }

    public String getEmployeeFullName() {
        return getEmpleadoNombreCompleto();
    }
}
