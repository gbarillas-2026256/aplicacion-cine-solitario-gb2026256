package org.cinekinal.system.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Collections;
import java.util.List;
import org.cinekinal.system.model.CashClosing;
import org.cinekinal.system.model.CashClosingDetail;
import org.cinekinal.system.model.CashClosingSaveStatus;
import org.cinekinal.system.repository.CashClosingRepository;

public class CashClosingService {

    private final CashClosingRepository closingRepo = new CashClosingRepository();

    public CashClosingRepository.DailyTicketsSummary getDailyTicketsSummary(Date date) {
        try {
            return closingRepo.getDailyTicketsSummary(date);
        } catch (Exception e) {
            return new CashClosingRepository.DailyTicketsSummary(0, BigDecimal.ZERO);
        }
    }

    public List<CashClosingRepository.TicketsPerMovie> getTicketsPerMovie(Date date) {
        try {
            return closingRepo.getTicketsPerMovie(date);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public CashClosingSaveStatus saveClosing(String idEmployee, Date closingDate,
                                           BigDecimal totalTickets, int ticketsSold,
                                           String notes, List<CashClosingDetail> details) {
        try {
            if (closingRepo.closingExistsToday(idEmployee, closingDate)) {
                return CashClosingSaveStatus.ALREADY_EXISTS_TODAY;
            }

            String idClosing = closingRepo.create(idEmployee, closingDate, totalTickets,
                    ticketsSold, notes);
            if (idClosing == null) {
                return CashClosingSaveStatus.SAVE_ERROR;
            }

            for (CashClosingDetail detail : details) {
                closingRepo.addDetail(idClosing, detail);
            }
            return CashClosingSaveStatus.SAVED;

        } catch (Exception e) {
            return CashClosingSaveStatus.SAVE_ERROR;
        }
    }

    public List<CashClosing> getByDateRange(Date startDate, Date endDate) {
        try {
            return closingRepo.getByDateRange(startDate, endDate);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<CashClosingDetail> getDetails(String idClosing) {
        try {
            return closingRepo.getDetails(idClosing);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
