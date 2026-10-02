package org.cinekinal.system.model;

import java.math.BigDecimal;

/**
 * CashClosingDetail entity representing a concession line item in a cash closing.
 */
public class CashClosingDetail {
    private String idDetail;
    private String closingId;
    private String category;
    private String description;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public CashClosingDetail() {
    }

    public CashClosingDetail(String category, String description, int quantity, BigDecimal unitPrice) {
        this.category = category;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getIdDetail() {
        return idDetail;
    }

    public void setIdDetail(String idDetail) {
        this.idDetail = idDetail;
    }

    public String getClosingId() {
        return closingId;
    }

    public void setClosingId(String closingId) {
        this.closingId = closingId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        if (subtotal != null) {
            return subtotal;
        }
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    // Compatibility
    public String getIdDetalle() { return idDetail; }
    public void setIdDetalle(String id) { this.idDetail = id; }
    public String getIdCorte() { return closingId; }
    public void setIdCorte(String id) { this.closingId = id; }
    public String getCategoria() { return category; }
    public void setCategoria(String c) { this.category = c; }
    public String getDescripcion() { return description; }
    public void setDescripcion(String d) { this.description = d; }
    public int getCantidad() { return quantity; }
    public void setCantidad(int q) { this.quantity = q; }
    public BigDecimal getPrecioUnitario() { return unitPrice; }
    public void setPrecioUnitario(BigDecimal p) { this.unitPrice = p; }
}
