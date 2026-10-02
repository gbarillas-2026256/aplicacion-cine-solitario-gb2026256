package org.cinekinal.system.repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;
import org.cinekinal.system.model.Showtime;

public class ShowtimeRepository {

    public List<Showtime> getBillboard() {
        return Db.list("{call sp_obtener_cartelera()}", ShowtimeRepository::map);
    }

    public List<Showtime> getBillboardByDate(Date date) {
        try {
            return Db.list("{call sp_obtener_cartelera_por_fecha(?)}", ShowtimeRepository::map, date);
        } catch (RuntimeException e) {
            return getBillboard().stream()
                    .filter(f -> f.getDate() != null && f.getDate().toString().equals(date.toString()))
                    .toList();
        }
    }

    public List<Showtime> searchByTitle(String query) {
        if (query == null || query.isBlank()) {
            return getBillboard();
        }
        String filter = query.trim().toLowerCase();
        return getBillboard().stream()
                .filter(f -> f.getMovieTitle() != null && f.getMovieTitle().toLowerCase().contains(filter))
                .toList();
    }

    public void create(String idMovie, String idTheater, Date date, Time time, BigDecimal basePrice) {
        Db.update("{call sp_crear_funcion(?,?,?,?,?)}", idMovie, idTheater, date, time, basePrice);
    }

    public boolean updateBasePrice(String idShowtime, BigDecimal newPrice) {
        try {
            return Db.update("UPDATE Funciones SET precio_base = ? WHERE id_funcion = ?",
                    newPrice, idShowtime) > 0;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static Showtime map(ResultSet rs) throws SQLException {
        Showtime showtime = new Showtime();
        showtime.setIdShowtime(rs.getString("id_funcion"));
        showtime.setIdTheater(rs.getString("id_sala"));
        showtime.setMovieTitle(rs.getString("titulo"));
        showtime.setDurationMin(rs.getInt("duracion_min"));
        try { showtime.setGenre(rs.getString("genero")); } catch (SQLException ignored) {}
        try { showtime.setRating(rs.getString("clasificacion")); } catch (SQLException ignored) {}
        try { showtime.setSynopsis(rs.getString("sinopsis")); } catch (SQLException ignored) {}
        try { showtime.setPosterUrl(rs.getString("poster_url")); } catch (SQLException ignored) {}
        try { showtime.setTrailerUrl(rs.getString("trailer_url")); } catch (SQLException ignored) {}
        showtime.setTheaterName(rs.getString("nombre_sala"));
        showtime.setTheaterType(rs.getString("tipo_sala"));
        showtime.setDate(rs.getDate("fecha"));
        showtime.setTime(rs.getTime("hora"));
        showtime.setBasePrice(rs.getBigDecimal("precio_base"));
        return showtime;
    }

    // Compatibility aliases
    public List<Showtime> obtenerCartelera() { return getBillboard(); }
    public List<Showtime> obtenerCarteleraPorFecha(Date d) { return getBillboardByDate(d); }
    public List<Showtime> buscarPorTitulo(String q) { return searchByTitle(q); }
    public void crear(String m, String t, Date d, Time h, BigDecimal p) { create(m, t, d, h, p); }
    public boolean actualizarPrecioBase(String id, BigDecimal p) { return updateBasePrice(id, p); }
}
