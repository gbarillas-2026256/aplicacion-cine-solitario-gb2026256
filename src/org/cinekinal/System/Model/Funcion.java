package org.cinekinal.system.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

/**
 * Representa una fila de la cartelera: la Funcion (horario de
 * proyeccion) ya con el titulo de la pelicula y el nombre de la sala
 * resueltos, tal como los devuelve sp_obtener_cartelera (viene de un
 * JOIN, por eso trae campos "aplanados" que en la base viven en otras
 * tablas -- igual que se hizo con Empleado y su Puesto).
 */
public class Funcion {
    private String idFuncion;
    private String idSala;
    private String tituloPelicula;
    private int duracionMin;
    private String nombreSala;
    private String tipoSala;
    private Date fecha;
    private Time hora;
    private BigDecimal precioBase;

    public Funcion() {
    }

    public String getIdFuncion() {
        return idFuncion;
    }

    public void setIdFuncion(String idFuncion) {
        this.idFuncion = idFuncion;
    }

    public String getIdSala() {
        return idSala;
    }

    public void setIdSala(String idSala) {
        this.idSala = idSala;
    }

    public String getTituloPelicula() {
        return tituloPelicula;
    }

    public void setTituloPelicula(String tituloPelicula) {
        this.tituloPelicula = tituloPelicula;
    }

    public int getDuracionMin() {
        return duracionMin;
    }

    public void setDuracionMin(int duracionMin) {
        this.duracionMin = duracionMin;
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

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Time getHora() {
        return hora;
    }

    public void setHora(Time hora) {
        this.hora = hora;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }
}
