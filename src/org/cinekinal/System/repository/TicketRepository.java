package org.cinekinal.system.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.cinekinal.system.model.Ticket;
import org.cinekinal.system.utils.Session;

public class TicketRepository {

    private static final String SELECT_TICKET_WITH_JOINS =
            "SELECT b.id_boleto, p.titulo, s.nombre_sala, f.fecha, f.hora, "
            + "a.fila, a.numero, b.precio_final, b.fecha_compra, "
            + "CONCAT(c.nombres, ' ', c.apellidos) AS nombre_cliente "
            + "FROM Boletos b "
            + "INNER JOIN Funciones f ON f.id_funcion = b.id_funcion "
            + "INNER JOIN Peliculas p ON p.id_pelicula = f.id_pelicula "
            + "INNER JOIN Salas s ON s.id_sala = f.id_sala "
            + "INNER JOIN Asientos a ON a.id_asiento = b.id_asiento "
            + "LEFT JOIN Clientes c ON c.id_cliente = b.id_cliente ";

    private static final Set<String> CHECKED_IN_TICKETS = ConcurrentHashMap.newKeySet();

    public Set<String> getOccupiedSeats(String idShowtime) {
        return Set.copyOf(Db.list("{call sp_obtener_asientos_ocupados(?)}",
                rs -> rs.getString("id_asiento"), idShowtime));
    }

    public void purchase(String idShowtime, String idCustomer, String idSeat, BigDecimal finalPrice) {
        Db.update("{call sp_comprar_boleto(?,?,?,?)}", idShowtime, idCustomer, idSeat, finalPrice);
    }

    public Ticket getLastPurchasedTicket(String idShowtime, String idSeat) {
        return Db.one(SELECT_TICKET_WITH_JOINS + "WHERE b.id_funcion = ? AND b.id_asiento = ?",
                rs -> map(rs, "Cliente"), idShowtime, idSeat);
    }

    public List<Ticket> getTicketsByCustomer(String idCustomer) {
        return Db.list("{call sp_obtener_boletos_por_cliente(?)}", rs -> {
            Ticket ticket = map(rs, null);
            ticket.setUsed(isTicketCheckedIn(ticket.getIdTicket()));
            if (Session.isCustomer() && Session.getCurrentCustomer() != null) {
                var c = Session.getCurrentCustomer();
                ticket.setCustomerName(c.getFullName());
            }
            return ticket;
        }, idCustomer);
    }

    public Ticket findTicketById(String idTicket) {
        if (idTicket == null || idTicket.isBlank()) {
            return null;
        }
        String trimmed = idTicket.trim();
        Ticket ticket = Db.one(SELECT_TICKET_WITH_JOINS + "WHERE b.id_boleto = ? OR b.id_boleto LIKE ?",
                rs -> map(rs, "Público General"), trimmed, trimmed + "%");
        if (ticket != null) {
            ticket.setUsed(CHECKED_IN_TICKETS.contains(ticket.getIdTicket()));
        }
        return ticket;
    }

    public boolean markTicketCheckedIn(String idTicket) {
        return idTicket != null && !idTicket.isBlank() && CHECKED_IN_TICKETS.add(idTicket.trim());
    }

    public boolean isTicketCheckedIn(String idTicket) {
        return idTicket != null && CHECKED_IN_TICKETS.contains(idTicket.trim());
    }

    public static Ticket map(ResultSet rs, String defaultCustomerName) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setIdTicket(rs.getString("id_boleto"));
        ticket.setMovieTitle(rs.getString("titulo"));
        ticket.setTheaterName(rs.getString("nombre_sala"));
        ticket.setDate(rs.getDate("fecha"));
        ticket.setTime(rs.getTime("hora"));
        ticket.setRow(rs.getString("fila"));
        ticket.setNumber(rs.getInt("numero"));
        ticket.setFinalPrice(rs.getBigDecimal("precio_final"));
        ticket.setPurchaseDate(rs.getTimestamp("fecha_compra"));
        if (defaultCustomerName != null) {
            String client = rs.getString("nombre_cliente");
            ticket.setCustomerName(client != null && !client.isBlank() ? client : defaultCustomerName);
        }
        return ticket;
    }
}
