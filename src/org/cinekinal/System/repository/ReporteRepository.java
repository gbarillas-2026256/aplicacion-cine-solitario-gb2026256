package org.cinekinal.system.repository;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.cinekinal.system.config.ConexionDB;

public class ReporteRepository {

    private final ConexionDB conexionDB = ConexionDB.getInstanciaConexionDB();

    public static class ResumenHoy {
        public int boletosHoy;
        public BigDecimal ingresosHoy;

        public ResumenHoy(int boletosHoy, BigDecimal ingresosHoy) {
            this.boletosHoy = boletosHoy;
            this.ingresosHoy = ingresosHoy != null ? ingresosHoy : BigDecimal.ZERO;
        }
    }

    public static class ReporteFila {
        private final String columna1;
        private final String columna2;
        private final String columna3;
        private final String columna4;

        public ReporteFila(String c1, String c2, String c3, String c4) {
            this.columna1 = c1;
            this.columna2 = c2;
            this.columna3 = c3;
            this.columna4 = c4;
        }

        public String getColumna1() { return columna1; }
        public String getColumna2() { return columna2; }
        public String getColumna3() { return columna3; }
        public String getColumna4() { return columna4; }
    }

    public ResumenHoy obtenerResumenHoy() {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_reporte_resumen_hoy()}")) {
            try (ResultSet rs = callSP.executeQuery()) {
                if (rs.next()) {
                    return new ResumenHoy(rs.getInt("boletos_vendidos_hoy"), rs.getBigDecimal("ingresos_hoy"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Aviso al obtener resumen de hoy: " + e.getMessage());
        }
        return new ResumenHoy(0, BigDecimal.ZERO);
    }

    public List<ReporteFila> obtenerIngresosPorDia(Date fechaInicio, Date fechaFin) {
        List<ReporteFila> lista = new ArrayList<>();
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_reporte_ingresos_por_dia(?,?)}")) {
            callSP.setDate(1, fechaInicio);
            callSP.setDate(2, fechaFin);
            try (ResultSet rs = callSP.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ReporteFila(
                            rs.getDate("fecha").toString(),
                            rs.getInt("boletos_vendidos") + " boletos",
                            "Q " + rs.getBigDecimal("ingresos"),
                            "—"
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Aviso al obtener ingresos por dia: " + e.getMessage());
        }
        return lista;
    }

    public List<ReporteFila> obtenerTopPeliculas(Date fechaInicio, Date fechaFin, int limite) {
        List<ReporteFila> lista = new ArrayList<>();
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_reporte_top_peliculas(?,?,?)}")) {
            callSP.setDate(1, fechaInicio);
            callSP.setDate(2, fechaFin);
            callSP.setInt(3, limite);
            try (ResultSet rs = callSP.executeQuery()) {
                int pos = 1;
                while (rs.next()) {
                    lista.add(new ReporteFila(
                            "#" + pos + " " + rs.getString("titulo"),
                            rs.getInt("boletos_vendidos") + " boletos",
                            "Q " + rs.getBigDecimal("ingresos"),
                            "—"
                    ));
                    pos++;
                }
            }
        } catch (SQLException e) {
            System.out.println("Aviso al obtener top peliculas: " + e.getMessage());
        }
        return lista;
    }

    public List<ReporteFila> obtenerOcupacionCartelera() {
        List<ReporteFila> lista = new ArrayList<>();
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_reporte_ocupacion_cartelera()}")) {
            try (ResultSet rs = callSP.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ReporteFila(
                            rs.getString("titulo"),
                            rs.getString("nombre_sala") + " · " + rs.getDate("fecha") + " " + rs.getTime("hora"),
                            rs.getInt("asientos_vendidos") + " / " + rs.getInt("capacidad_total") + " butacas",
                            rs.getDouble("porcentaje_ocupacion") + "%"
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Aviso al obtener ocupacion de salas: " + e.getMessage());
        }
        return lista;
    }

    public List<ReporteFila> obtenerIngresosPorPelicula(Date fechaInicio, Date fechaFin) {
        List<ReporteFila> lista = new ArrayList<>();
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_reporte_ingresos_por_pelicula(?,?)}")) {
            callSP.setDate(1, fechaInicio);
            callSP.setDate(2, fechaFin);
            try (ResultSet rs = callSP.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ReporteFila(
                            rs.getString("titulo"),
                            rs.getInt("boletos_vendidos") + " boletos",
                            "Q " + rs.getBigDecimal("ingresos"),
                            "—"
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Aviso al obtener ingresos por pelicula: " + e.getMessage());
        }
        return lista;
    }
}
