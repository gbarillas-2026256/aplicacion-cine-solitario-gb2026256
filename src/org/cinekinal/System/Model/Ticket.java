package org.cinekinal.system.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Ticket entity representing a purchased movie seat ticket.
 */
public class Ticket {
    private String idTicket;
    private String movieTitle;
    private String theaterName;
    private Date showDate;
    private Time showTime;
    private String row;
    private int seatNumber;
    private BigDecimal finalPrice;
    private Timestamp purchaseDate;
    private String customerName;
    private boolean entryUsed;
    private Timestamp entryTime;
    private String showtimeId;
    private String seatId;
    private String customerId;

    public Ticket() {
    }

    public String getIdTicket() {
        return idTicket;
    }

    public void setIdTicket(String idTicket) {
        this.idTicket = idTicket;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getTheaterName() {
        return theaterName;
    }

    public void setTheaterName(String theaterName) {
        this.theaterName = theaterName;
    }

    public Date getShowDate() {
        return showDate;
    }

    public void setShowDate(Date showDate) {
        this.showDate = showDate;
    }

    public Date getDate() { return showDate; }
    public void setDate(Date date) { this.showDate = date; }

    public Time getShowTime() {
        return showTime;
    }

    public void setShowTime(Time showTime) {
        this.showTime = showTime;
    }

    public Time getTime() { return showTime; }
    public void setTime(Time time) { this.showTime = time; }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public int getNumber() { return seatNumber; }
    public void setNumber(int number) { this.seatNumber = number; }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public Timestamp getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Timestamp purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public boolean isEntryUsed() {
        return entryUsed;
    }

    public void setEntryUsed(boolean entryUsed) {
        this.entryUsed = entryUsed;
    }

    public boolean isUsed() {
        return entryUsed;
    }

    public void setUsed(boolean used) {
        this.entryUsed = used;
    }

    public String getFormattedSeat() {
        return (row != null ? row : "") + seatNumber;
    }

    public Timestamp getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(Timestamp entryTime) {
        this.entryTime = entryTime;
    }

    public String getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(String showtimeId) {
        this.showtimeId = showtimeId;
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    // Compatibility
    public String getIdBoleto() { return idTicket; }
    public void setIdBoleto(String id) { this.idTicket = id; }
    public String getTituloPelicula() { return movieTitle; }
    public void setTituloPelicula(String t) { this.movieTitle = t; }
    public String getNombreSala() { return theaterName; }
    public void setNombreSala(String n) { this.theaterName = n; }
    public Date getFecha() { return showDate; }
    public void setFecha(Date f) { this.showDate = f; }
    public Time getHora() { return showTime; }
    public void setHora(Time h) { this.showTime = h; }
    public String getFila() { return row; }
    public void setFila(String f) { this.row = f; }
    public int getNumero() { return seatNumber; }
    public void setNumero(int n) { this.seatNumber = n; }
    public BigDecimal getPrecioFinal() { return finalPrice; }
    public void setPrecioFinal(BigDecimal p) { this.finalPrice = p; }
    public Timestamp getFechaCompra() { return purchaseDate; }
    public void setFechaCompra(Timestamp f) { this.purchaseDate = f; }
    public String getNombreCliente() { return customerName; }
    public void setNombreCliente(String n) { this.customerName = n; }
    public boolean isUsado() { return entryUsed; }
    public void setUsado(boolean u) { this.entryUsed = u; }
    public Timestamp getHoraEntrada() { return entryTime; }
    public void setHoraEntrada(Timestamp h) { this.entryTime = h; }
}
