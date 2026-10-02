package org.cinekinal.system.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Purchased ticket model.
 */
public class Ticket extends Boleto {

    public Ticket() {
        super();
    }

    public String getIdTicket() {
        return getIdBoleto();
    }

    public void setIdTicket(String idTicket) {
        setIdBoleto(idTicket);
    }

    public String getMovieTitle() {
        return getTituloPelicula();
    }

    public void setMovieTitle(String movieTitle) {
        setTituloPelicula(movieTitle);
    }

    public String getTheaterName() {
        return getNombreSala();
    }

    public void setTheaterName(String theaterName) {
        setNombreSala(theaterName);
    }

    public Date getDate() {
        return getFecha();
    }

    public void setDate(Date date) {
        setFecha(date);
    }

    public Time getTime() {
        return getHora();
    }

    public void setTime(Time time) {
        setHora(time);
    }

    public String getRow() {
        return getFila();
    }

    public void setRow(String row) {
        setFila(row);
    }

    public int getNumber() {
        return getNumero();
    }

    public void setNumber(int number) {
        setNumero(number);
    }

    public BigDecimal getFinalPrice() {
        return getPrecioFinal();
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        setPrecioFinal(finalPrice);
    }

    public Timestamp getPurchaseDate() {
        return getFechaCompra();
    }

    public void setPurchaseDate(Timestamp purchaseDate) {
        setFechaCompra(purchaseDate);
    }

    public String getCustomerName() {
        return getNombreCliente();
    }

    public void setCustomerName(String customerName) {
        setNombreCliente(customerName);
    }

    public boolean isUsed() {
        return isUsado();
    }

    public void setUsed(boolean used) {
        setUsado(used);
    }

    public String getFormattedSeat() {
        return getAsientoFormateado();
    }
}
