package org.cinekinal.system.repository;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.cinekinal.system.config.ConexionDB;
import org.cinekinal.system.model.Boleto;

public class BoletoRepository {

    private final ConexionDB conexionDB = ConexionDB.getInstanciaConexionDB();

    /** ids de asiento ya vendidos para esa funcion, para pintarlos ocupados en el mapa. */
    public Set<String> obtenerAsientosOcupados(String idFuncion) {
        Set<String> ocupados = new HashSet<>();
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_obtener_asientos_ocupados(?)}")) {
            callSP.setString(1, idFuncion);
            try (ResultSet resultado = callSP.executeQuery()) {
                while (resultado.next()) {
                    ocupados.add(resultado.getString("id_asiento"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener asientos ocupados: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return ocupados;
    }

    /**
     * Si el asiento ya se vendio para esta funcion, el UNIQUE(id_funcion,
     * id_asiento) de la tabla Boletos hace que MySQL rechace el insert
     * con SQLIntegrityConstraintViolationException -- BoletoService la
     * traduce a BoletoCompraStatus.ASIENTO_YA_VENDIDO.
     */
    public void comprar(String idFuncion, String idCliente, String idAsiento, BigDecimal precioFinal) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_comprar_boleto(?,?,?,?)}")) {
            callSP.setString(1, idFuncion);
            callSP.setString(2, idCliente);
            callSP.setString(3, idAsiento);
            callSP.setBigDecimal(4, precioFinal);
            callSP.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al comprar boleto: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Boleto> obtenerPorCliente(String idCliente) {
        List<Boleto> boletos = new ArrayList<>();
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_obtener_boletos_por_cliente(?)}")) {
            callSP.setString(1, idCliente);
            try (ResultSet resultado = callSP.executeQuery()) {
                while (resultado.next()) {
                    Boleto boleto = new Boleto();
                    boleto.setIdBoleto(resultado.getString("id_boleto"));
                    boleto.setTituloPelicula(resultado.getString("titulo"));
                    boleto.setNombreSala(resultado.getString("nombre_sala"));
                    boleto.setFecha(resultado.getDate("fecha"));
                    boleto.setHora(resultado.getTime("hora"));
                    boleto.setFila(resultado.getString("fila"));
                    boleto.setNumero(resultado.getInt("numero"));
                    boleto.setPrecioFinal(resultado.getBigDecimal("precio_final"));
                    boleto.setFechaCompra(resultado.getTimestamp("fecha_compra"));
                    boletos.add(boleto);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener boletos del cliente: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return boletos;
    }

    private static final Set<String> BOLETOS_INGRESADOS = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public Boleto buscarBoletoPorId(String idBoleto) {
        if (idBoleto == null || idBoleto.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT b.id_boleto, p.titulo, s.nombre_sala, f.fecha, f.hora, "
                + "a.fila, a.numero, b.precio_final, b.fecha_compra, "
                + "CONCAT(c.nombres, ' ', c.apellidos) AS nombre_cliente "
                + "FROM Boletos b "
                + "INNER JOIN Funciones f ON f.id_funcion = b.id_funcion "
                + "INNER JOIN Peliculas p ON p.id_pelicula = f.id_pelicula "
                + "INNER JOIN Salas s ON s.id_sala = f.id_sala "
                + "INNER JOIN Asientos a ON a.id_asiento = b.id_asiento "
                + "LEFT JOIN Clientes c ON c.id_cliente = b.id_cliente "
                + "WHERE b.id_boleto = ? OR b.id_boleto LIKE ?";
        try (java.sql.PreparedStatement ps = conexionDB.getConnection().prepareStatement(sql)) {
            String trimmed = idBoleto.trim();
            ps.setString(1, trimmed);
            ps.setString(2, trimmed + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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
                    String cliente = rs.getString("nombre_cliente");
                    boleto.setNombreCliente(cliente != null && !cliente.trim().isEmpty() ? cliente : "Público General");
                    boleto.setUsado(BOLETOS_INGRESADOS.contains(boleto.getIdBoleto()));
                    return boleto;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar boleto por id: " + e.getMessage());
        }
        return null;
    }

    public boolean marcarBoletoIngresado(String idBoleto) {
        if (idBoleto == null || idBoleto.trim().isEmpty()) {
            return false;
        }
        return BOLETOS_INGRESADOS.add(idBoleto.trim());
    }

    public boolean estaBoletoIngresado(String idBoleto) {
        return idBoleto != null && BOLETOS_INGRESADOS.contains(idBoleto.trim());
    }
}
