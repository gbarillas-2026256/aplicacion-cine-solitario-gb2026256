package org.cinekinal.system.repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import org.cinekinal.system.model.CashClosing;
import org.cinekinal.system.model.CashClosingDetail;

public class CashClosingRepository {

    public static class DailyTicketsSummary {
        public final int ticketsSold;
        public final BigDecimal totalTickets;

        public DailyTicketsSummary(int ticketsSold, BigDecimal totalTickets) {
            this.ticketsSold = ticketsSold;
            this.totalTickets = totalTickets != null ? totalTickets : BigDecimal.ZERO;
        }

        // Compatibility getters
        public int getBoletosVendidos() { return ticketsSold; }
        public BigDecimal getTotalEntradas() { return totalTickets; }
    }

    public static class TicketsPerMovie {
        private final String title;
        private final int ticketsSold;
        private final BigDecimal total;

        public TicketsPerMovie(String title, int ticketsSold, BigDecimal total) {
            this.title = title;
            this.ticketsSold = ticketsSold;
            this.total = total != null ? total : BigDecimal.ZERO;
        }

        public String getTitle() { return title; }
        public int getTicketsSold() { return ticketsSold; }
        public BigDecimal getTotal() { return total; }

        // Compatibility
        public String getTitulo() { return title; }
        public int getBoletosVendidos() { return ticketsSold; }
    }

    public DailyTicketsSummary getDailyTicketsSummary(Date date) {
        DailyTicketsSummary result = Db.one("{call sp_corte_entradas_del_dia(?)}",
                rs -> new DailyTicketsSummary(rs.getInt("boletos_vendidos"), rs.getBigDecimal("total_entradas")), date);
        return result != null ? result : new DailyTicketsSummary(0, BigDecimal.ZERO);
    }

    public List<TicketsPerMovie> getTicketsPerMovie(Date date) {
        return Db.list("{call sp_corte_entradas_por_pelicula(?)}", rs -> new TicketsPerMovie(
                rs.getString("titulo"), rs.getInt("boletos_vendidos"), rs.getBigDecimal("total")), date);
    }

    public boolean closingExistsToday(String idEmployee, Date date) {
        Integer exists = Db.one("{call sp_corte_existe(?,?)}", rs -> rs.getInt("ya_existe"), idEmployee, date);
        return exists != null && exists > 0;
    }

    public String create(String idEmployee, Date closingDate, BigDecimal totalTickets,
                         int ticketsSold, String notes) {
        return Db.one("{call sp_corte_crear(?,?,?,?,?)}", rs -> rs.getString("id_corte"),
                idEmployee, closingDate, totalTickets, ticketsSold, notes);
    }

    public void addDetail(String idClosing, CashClosingDetail detail) {
        Db.update("{call sp_corte_agregar_detalle(?,?,?,?,?)}", idClosing, detail.getCategory(),
                detail.getDescription(), detail.getQuantity(), detail.getUnitPrice());
    }

    public List<CashClosing> getByDateRange(Date startDate, Date endDate) {
        return Db.list("{call sp_corte_obtener_por_fecha(?,?)}", rs -> {
            CashClosing closing = new CashClosing();
            closing.setIdClosing(rs.getString("id_corte"));
            closing.setClosingDate(rs.getDate("fecha_corte"));
            closing.setTotalTickets(rs.getBigDecimal("total_entradas"));
            closing.setTicketsSold(rs.getInt("boletos_vendidos"));
            closing.setTotalConcessions(rs.getBigDecimal("total_dulceria"));
            closing.setGrandTotal(rs.getBigDecimal("total_general"));
            closing.setNotes(rs.getString("observaciones"));
            closing.setCreatedAt(rs.getTimestamp("fecha_registro"));
            closing.setEmployeeFirstName(rs.getString("empleado_nombres"));
            closing.setEmployeeLastName(rs.getString("empleado_apellidos"));
            closing.setEmployeePosition(rs.getString("empleado_puesto"));
            return closing;
        }, startDate, endDate);
    }

    public List<CashClosingDetail> getDetails(String idClosing) {
        return Db.list("{call sp_corte_obtener_detalles(?)}", rs -> {
            CashClosingDetail detail = new CashClosingDetail();
            detail.setIdDetail(rs.getString("id_detalle"));
            detail.setCategory(rs.getString("categoria"));
            detail.setDescription(rs.getString("descripcion"));
            detail.setQuantity(rs.getInt("cantidad"));
            detail.setUnitPrice(rs.getBigDecimal("precio_unitario"));
            detail.setSubtotal(rs.getBigDecimal("subtotal"));
            return detail;
        }, idClosing);
    }

    // Compatibility aliases
    public DailyTicketsSummary obtenerEntradasDelDia(Date f) { return getDailyTicketsSummary(f); }
    public List<TicketsPerMovie> obtenerEntradasPorPelicula(Date f) { return getTicketsPerMovie(f); }
    public boolean yaExisteCorte(String e, Date f) { return closingExistsToday(e, f); }
    public String crear(String e, Date f, BigDecimal t, int b, String o) { return create(e, f, t, b, o); }
    public void agregarDetalle(String id, CashClosingDetail d) { addDetail(id, d); }
    public List<CashClosing> obtenerPorFecha(Date i, Date f) { return getByDateRange(i, f); }
    public List<CashClosingDetail> obtenerDetalles(String id) { return getDetails(id); }
}
