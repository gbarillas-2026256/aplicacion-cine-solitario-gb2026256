package org.cinekinal.system.model;

/**
 * Theater / auditorium model.
 */
public class Theater extends Sala {

    public Theater() {
        super();
    }

    public Theater(String idTheater, String name, String type, int rows, int columns) {
        super(idTheater, name, type, rows, columns);
    }

    public String getIdTheater() {
        return getIdSala();
    }

    public void setIdTheater(String idTheater) {
        setIdSala(idTheater);
    }

    public String getName() {
        return getNombreSala();
    }

    public void setName(String name) {
        setNombreSala(name);
    }

    public String getType() {
        return getTipoSala();
    }

    public void setType(String type) {
        setTipoSala(type);
    }

    public int getRows() {
        return getFilas();
    }

    public void setRows(int rows) {
        setFilas(rows);
    }

    public int getColumns() {
        return getColumnas();
    }

    public void setColumns(int columns) {
        setColumnas(columns);
    }

    public int getCapacity() {
        return getFilas() * getColumnas();
    }
}
