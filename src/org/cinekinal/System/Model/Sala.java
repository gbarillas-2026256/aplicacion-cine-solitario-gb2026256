package org.cinekinal.system.model;

public class Sala {
    private String idSala;
    private String nombreSala;
    private String tipoSala;
    private int filas;
    private int columnas;

    public Sala() {
    }

    public Sala(String idSala, String nombreSala, String tipoSala, int filas, int columnas) {
        this.idSala = idSala;
        this.nombreSala = nombreSala;
        this.tipoSala = tipoSala;
        this.filas = filas;
        this.columnas = columnas;
    }

    public String getIdSala() {
        return idSala;
    }

    public void setIdSala(String idSala) {
        this.idSala = idSala;
    }

    public String getNombreSala() {
        return nombreSala;
    }

    public void setNombreSala(String nombreSala) {
        this.nombreSala = nombreSala;
    }

    public String getTipoSala() {
        return tipoSala;
    }

    public void setTipoSala(String tipoSala) {
        this.tipoSala = tipoSala;
    }

    public int getFilas() {
        return filas;
    }

    public void setFilas(int filas) {
        this.filas = filas;
    }

    public int getColumnas() {
        return columnas;
    }

    public void setColumnas(int columnas) {
        this.columnas = columnas;
    }
}
