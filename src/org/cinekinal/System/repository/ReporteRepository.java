package org.cinekinal.system.repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public class ReporteRepository {

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
        return intentar(() -> Db.one("{call sp_reporte_resumen_hoy()}",
                rs -> new ResumenHoy(rs.getInt("boletos_vendidos_hoy"), rs.getBigDecimal("ingresos_hoy"))),
                new ResumenHoy(0, BigDecimal.ZERO), "resumen de hoy");
    }

    public List<ReporteFila> obtenerIngresosPorDia(Date fechaInicio, Date fechaFin) {
        return intentar(() -> Db.list("{call sp_reporte_ingresos_por_dia(?,?)}", rs -> new ReporteFila(
                rs.getDate("fecha").toString(),
                rs.getInt("boletos_vendidos") + " boletos",
                "Q " + rs.getBigDecimal("ingresos"), "—"),
                fechaInicio, fechaFin), List.of(), "ingresos por dia");
    }

    public List<ReporteFila> obtenerTopPeliculas(Date fechaInicio, Date fechaFin, int limite) {
        return intentar(() -> {
            List<ReporteFila> filas = Db.list("{call sp_reporte_top_peliculas(?,?,?)}", rs -> new ReporteFila(
                    rs.getString("titulo"), rs.getInt("boletos_vendidos") + " boletos",
                    "Q " + rs.getBigDecimal("ingresos"), "—"),
                    fechaInicio, fechaFin, limite);
            for (int i = 0; i < filas.size(); i++) {
                ReporteFila f = filas.get(i);
                filas.set(i, new ReporteFila("#" + (i + 1) + " " + f.getColumna1(),
                        f.getColumna2(), f.getColumna3(), f.getColumna4()));
            }
            return filas;
        }, List.of(), "top peliculas");
    }

    public List<ReporteFila> obtenerOcupacionCartelera() {
        return intentar(() -> Db.list("{call sp_reporte_ocupacion_cartelera()}", rs -> new ReporteFila(
                rs.getString("titulo"),
                rs.getString("nombre_sala") + " · " + rs.getDate("fecha") + " " + rs.getTime("hora"),
                rs.getInt("asientos_vendidos") + " / " + rs.getInt("capacidad_total") + " butacas",
                rs.getDouble("porcentaje_ocupacion") + "%")),
                List.of(), "ocupacion de salas");
    }

    public List<ReporteFila> obtenerIngresosPorPelicula(Date fechaInicio, Date fechaFin) {
        return intentar(() -> Db.list("{call sp_reporte_ingresos_por_pelicula(?,?)}", rs -> new ReporteFila(
                rs.getString("titulo"), rs.getInt("boletos_vendidos") + " boletos",
                "Q " + rs.getBigDecimal("ingresos"), "—"),
                fechaInicio, fechaFin), List.of(), "ingresos por pelicula");
    }

    /** Los reportes son informativos: si uno falla, se avisa por consola y la pantalla sigue con datos vacios. */
    private <T> T intentar(java.util.function.Supplier<T> consulta, T porDefecto, String nombre) {
        try {
            return consulta.get();
        } catch (RuntimeException e) {
            System.out.println("Aviso al obtener " + nombre + ": " + e.getMessage());
            return porDefecto;
        }
    }
}
