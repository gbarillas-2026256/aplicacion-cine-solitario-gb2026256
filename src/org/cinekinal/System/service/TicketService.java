package org.cinekinal.system.service;

import java.math.BigDecimal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import org.cinekinal.system.model.Ticket;
import org.cinekinal.system.model.TicketPurchaseStatus;
import org.cinekinal.system.repository.TicketRepository;

public class TicketService {

    private final TicketRepository ticketRepo = new TicketRepository();

    public TicketPurchaseStatus purchase(String idShowtime, String idCustomer, String idSeat, BigDecimal finalPrice) {
        try {
            ticketRepo.purchase(idShowtime, idCustomer, idSeat, finalPrice);
            return TicketPurchaseStatus.PURCHASE_COMPLETED;
        } catch (RuntimeException e) {
            // uq_boletos_asiento_funcion: someone else purchased that seat first
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                return TicketPurchaseStatus.SEAT_ALREADY_SOLD;
            }
            return TicketPurchaseStatus.PURCHASE_ERROR;
        }
    }

    public List<Ticket> getTicketsByCustomer(String idCustomer) {
        return ticketRepo.getTicketsByCustomer(idCustomer);
    }
}
