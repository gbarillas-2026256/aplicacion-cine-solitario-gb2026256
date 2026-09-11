package org.cinekinal.system.model;

public class Asiento {
    private String idAsiento;
    private String fila;
    private int numero;

    public Asiento() {
    }

    public String getIdAsiento() {
        return idAsiento;
    }

    public void setIdAsiento(String idAsiento) {
        this.idAsiento = idAsiento;
    }

    public String getFila() {
        return fila;
    }

    public void setFila(String fila) {
        this.fila = fila;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    /** Etiqueta corta para mostrar en el boton del asiento, ej. "A5". */
    public String getEtiqueta() {
        return fila + numero;
    }
}
