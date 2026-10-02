package org.cinekinal.system.repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.function.Supplier;

public class ReportRepository {

    public static class TodaySummary {
        public int ticketsToday;
        public BigDecimal incomeToday;

        public TodaySummary(int ticketsToday, BigDecimal incomeToday) {
            this.ticketsToday = ticketsToday;
            this.incomeToday = incomeToday != null ? incomeToday : BigDecimal.ZERO;
        }

        // Compatibility
        public int getBoletosHoy() { return ticketsToday; }
        public BigDecimal getIngresosHoy() { return incomeToday; }
    }

    public static class ReportRow {
        private final String column1;
        private final String column2;
        private final String column3;
        private final String column4;

        public ReportRow(String c1, String c2, String c3, String c4) {
            this.column1 = c1;
            this.column2 = c2;
            this.column3 = c3;
            this.column4 = c4;
        }

        public String getColumn1() { return column1; }
        public String getColumn2() { return column2; }
        public String getColumn3() { return column3; }
        public String getColumn4() { return column4; }

        // Compatibility
        public String getColumna1() { return column1; }
        public String getColumna2() { return column2; }
        public String getColumna3() { return column3; }
        public String getColumna4() { return column4; }
    }

    public TodaySummary getTodaySummary() {
        return tryQuery(() -> Db.one("{call sp_reporte_resumen_hoy()}",
                rs -> new TodaySummary(rs.getInt("boletos_vendidos_hoy"), rs.getBigDecimal("ingresos_hoy"))),
                new TodaySummary(0, BigDecimal.ZERO), "resumen de hoy");
    }

    public List<ReportRow> getDailyIncome(Date startDate, Date endDate) {
        return tryQuery(() -> Db.list("{call sp_reporte_ingresos_por_dia(?,?)}", rs -> new ReportRow(
                rs.getDate("fecha").toString(),
                rs.getInt("boletos_vendidos") + " boletos",
                "Q " + rs.getBigDecimal("ingresos"), "—"),
                startDate, endDate), List.of(), "ingresos por dia");
    }

    public List<ReportRow> getTopMovies(Date startDate, Date endDate, int limit) {
        return tryQuery(() -> {
            List<ReportRow> rows = Db.list("{call sp_reporte_top_peliculas(?,?,?)}", rs -> new ReportRow(
                    rs.getString("titulo"), rs.getInt("boletos_vendidos") + " boletos",
                    "Q " + rs.getBigDecimal("ingresos"), "—"),
                    startDate, endDate, limit);
            for (int i = 0; i < rows.size(); i++) {
                ReportRow r = rows.get(i);
                rows.set(i, new ReportRow("#" + (i + 1) + " " + r.getColumn1(),
                        r.getColumn2(), r.getColumn3(), r.getColumn4()));
            }
            return rows;
        }, List.of(), "top peliculas");
    }

    public List<ReportRow> getBillboardOccupancy() {
        return tryQuery(() -> Db.list("{call sp_reporte_ocupacion_cartelera()}", rs -> new ReportRow(
                rs.getString("titulo"),
                rs.getString("nombre_sala") + " · " + rs.getDate("fecha") + " " + rs.getTime("hora"),
                rs.getInt("asientos_vendidos") + " / " + rs.getInt("capacidad_total") + " butacas",
                rs.getDouble("porcentaje_ocupacion") + "%")),
                List.of(), "ocupacion de salas");
    }

    public List<ReportRow> getMovieIncome(Date startDate, Date endDate) {
        return tryQuery(() -> Db.list("{call sp_reporte_ingresos_por_pelicula(?,?)}", rs -> new ReportRow(
                rs.getString("titulo"), rs.getInt("boletos_vendidos") + " boletos",
                "Q " + rs.getBigDecimal("ingresos"), "—"),
                startDate, endDate), List.of(), "ingresos por pelicula");
    }

    private <T> T tryQuery(Supplier<T> query, T defaultValue, String queryName) {
        try {
            return query.get();
        } catch (RuntimeException e) {
            return defaultValue;
        }
    }

    // Compatibility aliases
    public TodaySummary obtenerResumenHoy() { return getTodaySummary(); }
    public List<ReportRow> obtenerIngresosPorDia(Date s, Date e) { return getDailyIncome(s, e); }
    public List<ReportRow> obtenerTopPeliculas(Date s, Date e, int l) { return getTopMovies(s, e, l); }
    public List<ReportRow> obtenerOcupacionCartelera() { return getBillboardOccupancy(); }
    public List<ReportRow> obtenerIngresosPorPelicula(Date s, Date e) { return getMovieIncome(s, e); }
}
