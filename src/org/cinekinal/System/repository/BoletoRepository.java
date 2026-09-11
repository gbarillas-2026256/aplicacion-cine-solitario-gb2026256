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
}
