package org.cinekinal.system.model;

/**
 * Seat entity representing an individual chair in a theater hall.
 */
public class Seat {
    private String idSeat;
    private String row;
    private int number;

    public Seat() {
    }

    public Seat(String idSeat, String row, int number) {
        this.idSeat = idSeat;
        this.row = row;
        this.number = number;
    }

    public String getIdSeat() {
        return idSeat;
    }

    public void setIdSeat(String idSeat) {
        this.idSeat = idSeat;
    }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getLabel() {
        return row + number;
    }

    public String getShortLabel() {
        return row + number;
    }

}
