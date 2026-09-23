package org.cinekinal.system.model;

import java.math.BigDecimal;

/**
 * Una linea de dulceria dentro de un corte de caja: un combo, una
 * palomera especial o un comestible por aparte.
 *
 * El subtotal se calcula aqui al construir la linea, y el
 * procedimiento sp_corte_agregar_detalle lo vuelve a calcular en la
 * base de datos -- asi el total guardado siempre coincide con la suma
 * de sus detalles, sin depender de que Java lo haya hecho bien.
 */
public class CorteDetalle {

    private String idDetalle;
    private String categoria;
    private String descripcion;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public CorteDetalle() {
    }

    public CorteDetalle(String categoria, String descripcion, int cantidad, BigDecimal precioUnitario) {
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public String getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(String idDetalle) {
        this.idDetalle = idDetalle;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
