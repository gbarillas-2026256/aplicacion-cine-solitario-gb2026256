package org.cinekinal.system.model;

/**
 * Theater entity representing an auditorium or screening hall.
 */
public class Theater {
    private String idTheater;
    private String theaterName;
    private String theaterType;
    private int rows;
    private int columns;

    public Theater() {
    }

    public Theater(String idTheater, String theaterName, String theaterType, int rows, int columns) {
        this.idTheater = idTheater;
        this.theaterName = theaterName;
        this.theaterType = theaterType;
        this.rows = rows;
        this.columns = columns;
    }

    public String getIdTheater() {
        return idTheater;
    }

    public void setIdTheater(String idTheater) {
        this.idTheater = idTheater;
    }

    public String getTheaterName() {
        return theaterName;
    }

    public void setTheaterName(String theaterName) {
        this.theaterName = theaterName;
    }

    public String getName() { return theaterName; }
    public void setName(String name) { this.theaterName = name; }

    public String getTheaterType() {
        return theaterType;
    }

    public void setTheaterType(String theaterType) {
        this.theaterType = theaterType;
    }

    public String getType() { return theaterType; }
    public void setType(String type) { this.theaterType = type; }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

}
