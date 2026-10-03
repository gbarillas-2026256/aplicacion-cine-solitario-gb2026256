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

}
