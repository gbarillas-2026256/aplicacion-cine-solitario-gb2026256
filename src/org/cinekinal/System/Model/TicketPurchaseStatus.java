package org.cinekinal.system.model;

/**
 * Result of attempting to purchase a ticket.
 */
public enum TicketPurchaseStatus {
    PURCHASE_COMPLETED,
    SEAT_ALREADY_SOLD,
    PURCHASE_ERROR
}
