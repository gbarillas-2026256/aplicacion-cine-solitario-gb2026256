package org.cinekinal.system.model;

import java.math.BigDecimal;

/**
 * Line item within a cash closing.
 */
public class CashClosingDetail extends CorteDetalle {

    public CashClosingDetail() {
        super();
    }

    public CashClosingDetail(String category, String description, int quantity, BigDecimal unitPrice) {
        super(category, description, quantity, unitPrice);
    }

    public String getIdDetail() {
        return getIdDetalle();
    }

    public void setIdDetail(String idDetail) {
        setIdDetalle(idDetail);
    }

    public String getCategory() {
        return getCategoria();
    }

    public void setCategory(String category) {
        setCategoria(category);
    }

    public String getDescription() {
        return getDescripcion();
    }

    public void setDescription(String description) {
        setDescripcion(description);
    }

    public int getQuantity() {
        return getCantidad();
    }

    public void setQuantity(int quantity) {
        setCantidad(quantity);
    }

    public BigDecimal getUnitPrice() {
        return getPrecioUnitario();
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        setPrecioUnitario(unitPrice);
    }

    public BigDecimal getSubtotal() {
        return super.getSubtotal();
    }

    public void setSubtotal(BigDecimal subtotal) {
        super.setSubtotal(subtotal);
    }
}
