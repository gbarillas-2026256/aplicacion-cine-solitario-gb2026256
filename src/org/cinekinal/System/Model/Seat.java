package org.cinekinal.system.model;

/**
 * Seat representation in a movie theater.
 */
public class Seat extends Asiento {

    public Seat() {
        super();
    }

    public Seat(String idSeat, String row, int number) {
        super(idSeat, row, number);
    }

    public String getIdSeat() {
        return getIdAsiento();
    }

    public void setIdSeat(String idSeat) {
        setIdAsiento(idSeat);
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

    public String getShortLabel() {
        return getEtiqueta();
    }
}
