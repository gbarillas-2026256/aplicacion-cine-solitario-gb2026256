package org.cinekinal.system.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * Cabecera de un corte de caja: el cierre del dia que hace un empleado.
 *
 * Las entradas (total y cantidad de boletos) NO las captura el empleado:
 * se calculan de la tabla Boletos al momento del corte y se congelan en
 * esta fila, para que el reporte historico no cambie si despues se
 * vende algo mas.
 */
public class CorteCaja {

    private String idCorte;
    private Date fechaCorte;
    private BigDecimal totalEntradas;
    private int boletosVendidos;
    private BigDecimal totalDulceria;
    private BigDecimal totalGeneral;
    private String observaciones;
    private Timestamp fechaRegistro;

    //Datos del empleado que hizo el corte, "aplanados" porque
    //sp_corte_obtener_por_fecha ya los trae con JOIN en la misma fila
    private String empleadoNombres;
    private String empleadoApellidos;
    private String empleadoPuesto;

    public CorteCaja() {
    }

    public String getIdCorte() {
        return idCorte;
    }

    public void setIdCorte(String idCorte) {
        this.idCorte = idCorte;
    }

    public Date getFechaCorte() {
        return fechaCorte;
    }

    public void setFechaCorte(Date fechaCorte) {
        this.fechaCorte = fechaCorte;
    }

    public BigDecimal getTotalEntradas() {
        return totalEntradas;
    }

    public void setTotalEntradas(BigDecimal totalEntradas) {
        this.totalEntradas = totalEntradas;
    }

    public int getBoletosVendidos() {
        return boletosVendidos;
    }

    public void setBoletosVendidos(int boletosVendidos) {
        this.boletosVendidos = boletosVendidos;
    }

    public BigDecimal getTotalDulceria() {
        return totalDulceria;
    }

    public void setTotalDulceria(BigDecimal totalDulceria) {
        this.totalDulceria = totalDulceria;
    }

    public BigDecimal getTotalGeneral() {
        return totalGeneral;
    }

    public void setTotalGeneral(BigDecimal totalGeneral) {
        this.totalGeneral = totalGeneral;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getEmpleadoNombres() {
        return empleadoNombres;
    }

    public void setEmpleadoNombres(String empleadoNombres) {
        this.empleadoNombres = empleadoNombres;
    }

    public String getEmpleadoApellidos() {
        return empleadoApellidos;
    }

    public void setEmpleadoApellidos(String empleadoApellidos) {
        this.empleadoApellidos = empleadoApellidos;
    }

    public String getEmpleadoPuesto() {
        return empleadoPuesto;
    }

    public void setEmpleadoPuesto(String empleadoPuesto) {
        this.empleadoPuesto = empleadoPuesto;
    }

    public String getEmpleadoNombreCompleto() {
        return empleadoNombres + " " + empleadoApellidos;
    }
}
