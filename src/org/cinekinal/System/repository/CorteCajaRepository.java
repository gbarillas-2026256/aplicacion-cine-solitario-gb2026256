package org.cinekinal.system.repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import org.cinekinal.system.model.CorteCaja;
import org.cinekinal.system.model.CorteDetalle;

public class CorteCajaRepository {

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
        EntradasDelDia resultado = Db.one("{call sp_corte_entradas_del_dia(?)}",
                rs -> new EntradasDelDia(rs.getInt("boletos_vendidos"), rs.getBigDecimal("total_entradas")), fecha);
        return resultado != null ? resultado : new EntradasDelDia(0, BigDecimal.ZERO);
    }

    public List<EntradasPorPelicula> obtenerEntradasPorPelicula(Date fecha) {
        return Db.list("{call sp_corte_entradas_por_pelicula(?)}", rs -> new EntradasPorPelicula(
                rs.getString("titulo"), rs.getInt("boletos_vendidos"), rs.getBigDecimal("total")), fecha);
    }

    public boolean yaExisteCorte(String idEmpleado, Date fecha) {
        Integer yaExiste = Db.one("{call sp_corte_existe(?,?)}", rs -> rs.getInt("ya_existe"), idEmpleado, fecha);
        return yaExiste != null && yaExiste > 0;
    }

    /** Crea la cabecera y devuelve el id del corte recien creado. */
    public String crear(String idEmpleado, Date fechaCorte, BigDecimal totalEntradas,
                         int boletosVendidos, String observaciones) {
        return Db.one("{call sp_corte_crear(?,?,?,?,?)}", rs -> rs.getString("id_corte"),
                idEmpleado, fechaCorte, totalEntradas, boletosVendidos, observaciones);
    }

    public void agregarDetalle(String idCorte, CorteDetalle detalle) {
        Db.update("{call sp_corte_agregar_detalle(?,?,?,?,?)}", idCorte, detalle.getCategoria(),
                detalle.getDescripcion(), detalle.getCantidad(), detalle.getPrecioUnitario());
    }

    public List<CorteCaja> obtenerPorFecha(Date fechaInicio, Date fechaFin) {
        return Db.list("{call sp_corte_obtener_por_fecha(?,?)}", rs -> {
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
            return corte;
        }, fechaInicio, fechaFin);
    }

    public List<CorteDetalle> obtenerDetalles(String idCorte) {
        return Db.list("{call sp_corte_obtener_detalles(?)}", rs -> {
            CorteDetalle detalle = new CorteDetalle();
            detalle.setCategoria(rs.getString("categoria"));
            detalle.setDescripcion(rs.getString("descripcion"));
            detalle.setCantidad(rs.getInt("cantidad"));
            detalle.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
            detalle.setSubtotal(rs.getBigDecimal("subtotal"));
            return detalle;
        }, idCorte);
    }
}
