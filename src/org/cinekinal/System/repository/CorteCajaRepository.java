package org.cinekinal.system.repository;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.cinekinal.system.config.ConexionDB;
import org.cinekinal.system.model.CorteCaja;
import org.cinekinal.system.model.CorteDetalle;

public class CorteCajaRepository {

    private final ConexionDB conexionDB = ConexionDB.getInstanciaConexionDB();

    /** Lo vendido en taquilla ese dia, calculado de la tabla Boletos. */
    public static class EntradasDelDia {
        public final int boletosVendidos;
        public final BigDecimal totalEntradas;

        public EntradasDelDia(int boletosVendidos, BigDecimal totalEntradas) {
            this.boletosVendidos = boletosVendidos;
            this.totalEntradas = totalEntradas != null ? totalEntradas : BigDecimal.ZERO;
        }
    }

    /** Una linea del desglose "entradas por pelicula" de ese dia. */
    public static class EntradasPorPelicula {
        private final String titulo;
        private final int boletosVendidos;
        private final BigDecimal total;

        public EntradasPorPelicula(String titulo, int boletosVendidos, BigDecimal total) {
            this.titulo = titulo;
            this.boletosVendidos = boletosVendidos;
            this.total = total != null ? total : BigDecimal.ZERO;
        }

        public String getTitulo() { return titulo; }
        public int getBoletosVendidos() { return boletosVendidos; }
        public BigDecimal getTotal() { return total; }
    }

    public EntradasDelDia obtenerEntradasDelDia(Date fecha) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_corte_entradas_del_dia(?)}")) {
            callSP.setDate(1, fecha);
            try (ResultSet rs = callSP.executeQuery()) {
                if (rs.next()) {
                    return new EntradasDelDia(rs.getInt("boletos_vendidos"),
                            rs.getBigDecimal("total_entradas"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener entradas del dia: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return new EntradasDelDia(0, BigDecimal.ZERO);
    }

    public List<EntradasPorPelicula> obtenerEntradasPorPelicula(Date fecha) {
        List<EntradasPorPelicula> lista = new ArrayList<>();
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_corte_entradas_por_pelicula(?)}")) {
            callSP.setDate(1, fecha);
            try (ResultSet rs = callSP.executeQuery()) {
                while (rs.next()) {
                    lista.add(new EntradasPorPelicula(
                            rs.getString("titulo"),
                            rs.getInt("boletos_vendidos"),
                            rs.getBigDecimal("total")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener entradas por pelicula: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return lista;
    }

    public boolean yaExisteCorte(String idEmpleado, Date fecha) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_corte_existe(?,?)}")) {
            callSP.setString(1, idEmpleado);
            callSP.setDate(2, fecha);
            try (ResultSet rs = callSP.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ya_existe") > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al verificar si el corte existe: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return false;
    }

    /** Crea la cabecera y devuelve el id del corte recien creado. */
    public String crear(String idEmpleado, Date fechaCorte, BigDecimal totalEntradas,
                         int boletosVendidos, String observaciones) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_corte_crear(?,?,?,?,?)}")) {
            callSP.setString(1, idEmpleado);
            callSP.setDate(2, fechaCorte);
            callSP.setBigDecimal(3, totalEntradas);
            callSP.setInt(4, boletosVendidos);
            callSP.setString(5, observaciones);
            try (ResultSet rs = callSP.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("id_corte");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al crear el corte: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }

    public void agregarDetalle(String idCorte, CorteDetalle detalle) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_corte_agregar_detalle(?,?,?,?,?)}")) {
            callSP.setString(1, idCorte);
            callSP.setString(2, detalle.getCategoria());
            callSP.setString(3, detalle.getDescripcion());
            callSP.setInt(4, detalle.getCantidad());
            callSP.setBigDecimal(5, detalle.getPrecioUnitario());
            callSP.execute();
        } catch (SQLException e) {
            System.out.println("Error al agregar detalle al corte: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<CorteCaja> obtenerPorFecha(Date fechaInicio, Date fechaFin) {
        List<CorteCaja> lista = new ArrayList<>();
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_corte_obtener_por_fecha(?,?)}")) {
            callSP.setDate(1, fechaInicio);
            callSP.setDate(2, fechaFin);
            try (ResultSet rs = callSP.executeQuery()) {
                while (rs.next()) {
                    CorteCaja corte = new CorteCaja();
                    corte.setIdCorte(rs.getString("id_corte"));
                    corte.setFechaCorte(rs.getDate("fecha_corte"));
                    corte.setTotalEntradas(rs.getBigDecimal("total_entradas"));
                    corte.setBoletosVendidos(rs.getInt("boletos_vendidos"));
                    corte.setTotalDulceria(rs.getBigDecimal("total_dulceria"));
                    corte.setTotalGeneral(rs.getBigDecimal("total_general"));
                    corte.setObservaciones(rs.getString("observaciones"));
                    corte.setFechaRegistro(rs.getTimestamp("fecha_registro"));
                    corte.setEmpleadoNombres(rs.getString("empleado_nombres"));
                    corte.setEmpleadoApellidos(rs.getString("empleado_apellidos"));
                    corte.setEmpleadoPuesto(rs.getString("empleado_puesto"));
                    lista.add(corte);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener cortes: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return lista;
    }

    public List<CorteDetalle> obtenerDetalles(String idCorte) {
        List<CorteDetalle> lista = new ArrayList<>();
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_corte_obtener_detalles(?)}")) {
            callSP.setString(1, idCorte);
            try (ResultSet rs = callSP.executeQuery()) {
                while (rs.next()) {
                    CorteDetalle detalle = new CorteDetalle();
                    detalle.setCategoria(rs.getString("categoria"));
                    detalle.setDescripcion(rs.getString("descripcion"));
                    detalle.setCantidad(rs.getInt("cantidad"));
                    detalle.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    detalle.setSubtotal(rs.getBigDecimal("subtotal"));
                    lista.add(detalle);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener detalles del corte: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return lista;
    }
}
