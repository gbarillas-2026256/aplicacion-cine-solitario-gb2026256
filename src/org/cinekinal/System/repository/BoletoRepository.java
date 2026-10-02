package org.cinekinal.system.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.cinekinal.system.model.Boleto;
import org.cinekinal.system.utils.Session;

public class BoletoRepository {

    private static final String SELECT_BOLETO_CON_JOINS =
            "SELECT b.id_boleto, p.titulo, s.nombre_sala, f.fecha, f.hora, "
            + "a.fila, a.numero, b.precio_final, b.fecha_compra, "
            + "CONCAT(c.nombres, ' ', c.apellidos) AS nombre_cliente "
            + "FROM Boletos b "
            + "INNER JOIN Funciones f ON f.id_funcion = b.id_funcion "
            + "INNER JOIN Peliculas p ON p.id_pelicula = f.id_pelicula "
            + "INNER JOIN Salas s ON s.id_sala = f.id_sala "
            + "INNER JOIN Asientos a ON a.id_asiento = b.id_asiento "
            + "LEFT JOIN Clientes c ON c.id_cliente = b.id_cliente ";

    /** ids de asiento ya vendidos para esa funcion, para pintarlos ocupados en el mapa. */
    public Set<String> obtenerAsientosOcupados(String idFuncion) {
        return Set.copyOf(Db.list("{call sp_obtener_asientos_ocupados(?)}",
                rs -> rs.getString("id_asiento"), idFuncion));
    }

    /**
     * Si el asiento ya se vendio para esta funcion, el UNIQUE(id_funcion,
     * id_asiento) de la tabla Boletos hace que MySQL rechace el insert
     * con SQLIntegrityConstraintViolationException -- BoletoService la
     * traduce a BoletoCompraStatus.ASIENTO_YA_VENDIDO.
     */
    public void comprar(String idFuncion, String idCliente, String idAsiento, BigDecimal precioFinal) {
        Db.update("{call sp_comprar_boleto(?,?,?,?)}", idFuncion, idCliente, idAsiento, precioFinal);
    }

    public Boleto obtenerUltimoBoletoComprado(String idFuncion, String idAsiento) {
        return Db.one(SELECT_BOLETO_CON_JOINS + "WHERE b.id_funcion = ? AND b.id_asiento = ?",
                rs -> mapear(rs, "Cliente"), idFuncion, idAsiento);
    }

    public List<Boleto> obtenerPorCliente(String idCliente) {
        return Db.list("{call sp_obtener_boletos_por_cliente(?)}", rs -> {
            Boleto boleto = mapear(rs, null);
            boleto.setUsado(estaBoletoIngresado(boleto.getIdBoleto()));
            if (Session.isCustomer() && Session.getCurrentCustomer() != null) {
                var c = Session.getCurrentCustomer();
                boleto.setNombreCliente(c.getFirstName() + " " + c.getLastName());
            }
            return boleto;
        }, idCliente);
    }

    /** Boletos "ingresados" (escaneados en la puerta) solo viven en memoria mientras la app esta abierta. */
    private static final Set<String> BOLETOS_INGRESADOS = ConcurrentHashMap.newKeySet();

    public Boleto buscarBoletoPorId(String idBoleto) {
        if (idBoleto == null || idBoleto.isBlank()) {
            return null;
        }
        String trimmed = idBoleto.trim();
        Boleto boleto = Db.one(SELECT_BOLETO_CON_JOINS + "WHERE b.id_boleto = ? OR b.id_boleto LIKE ?",
                rs -> mapear(rs, "Público General"), trimmed, trimmed + "%");
        if (boleto != null) {
            boleto.setUsado(BOLETOS_INGRESADOS.contains(boleto.getIdBoleto()));
        }
        return boleto;
    }

    public boolean marcarBoletoIngresado(String idBoleto) {
        return idBoleto != null && !idBoleto.isBlank() && BOLETOS_INGRESADOS.add(idBoleto.trim());
    }

    public boolean estaBoletoIngresado(String idBoleto) {
        return idBoleto != null && BOLETOS_INGRESADOS.contains(idBoleto.trim());
    }

    /** nombreClientePorDefecto se usa solo si el boleto no tiene cliente asociado (venta en taquilla). */
    private static Boleto mapear(java.sql.ResultSet rs, String nombreClientePorDefecto) throws java.sql.SQLException {
        Boleto boleto = new Boleto();
        boleto.setIdBoleto(rs.getString("id_boleto"));
        boleto.setTituloPelicula(rs.getString("titulo"));
        boleto.setNombreSala(rs.getString("nombre_sala"));
        boleto.setFecha(rs.getDate("fecha"));
        boleto.setHora(rs.getTime("hora"));
        boleto.setFila(rs.getString("fila"));
        boleto.setNumero(rs.getInt("numero"));
        boleto.setPrecioFinal(rs.getBigDecimal("precio_final"));
        boleto.setFechaCompra(rs.getTimestamp("fecha_compra"));
        if (nombreClientePorDefecto != null) {
            String cliente = rs.getString("nombre_cliente");
            boleto.setNombreCliente(cliente != null && !cliente.isBlank() ? cliente : nombreClientePorDefecto);
        }
        return boleto;
    }
}
