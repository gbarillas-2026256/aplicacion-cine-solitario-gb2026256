package org.cinekinal.system.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.cinekinal.system.model.Movie;

public class MovieRepository {

    public List<Movie> getActiveMovies() {
        return Db.list("{call sp_obtener_peliculas()}", MovieRepository::map);
    }

    public void create(String title, String genre, String rating, int durationMin,
                       String synopsis, String posterUrl, String trailerUrl) {
        Db.update("{call sp_crear_pelicula(?,?,?,?,?,?,?)}",
                title, genre, rating, durationMin, synopsis, posterUrl, trailerUrl);
    }

    public void create(String title, String genre, String rating, int durationMin, String synopsis) {
        create(title, genre, rating, durationMin, synopsis, null, null);
    }

    public void deactivate(String idMovie) {
        try {
            Db.update("{call sp_desactivar_pelicula(?)}", idMovie);
        } catch (RuntimeException e) {
            Db.update("UPDATE Peliculas SET activa = false WHERE id_pelicula = ?", idMovie);
        }
    }

    public void edit(String idMovie, String title, String genre, String rating,
                     int durationMin, String synopsis, String posterUrl, String trailerUrl) {
        try {
            Db.update("{call sp_editar_pelicula(?,?,?,?,?,?,?,?)}",
                    idMovie, title, genre, rating, durationMin, synopsis, posterUrl, trailerUrl);
        } catch (RuntimeException e) {
            Db.update("UPDATE Peliculas SET titulo=?, genero=?, clasificacion=?, duracion_min=?, "
                    + "sinopsis=?, poster_url=?, trailer_url=? WHERE id_pelicula=?",
                    title, genre, rating, durationMin, synopsis, posterUrl, trailerUrl, idMovie);
        }
    }

    public static Movie map(ResultSet rs) throws SQLException {
        Movie movie = new Movie();
        movie.setIdMovie(rs.getString("id_pelicula"));
        movie.setTitle(rs.getString("titulo"));
        movie.setGenre(rs.getString("genero"));
        movie.setRating(rs.getString("clasificacion"));
        movie.setDurationMin(rs.getInt("duracion_min"));
        movie.setSynopsis(rs.getString("sinopsis"));
        movie.setPosterUrl(rs.getString("poster_url"));
        movie.setTrailerUrl(rs.getString("trailer_url"));
        return movie;
    }

    // Compatibility aliases
    public List<Movie> obtenerActivas() { return getActiveMovies(); }
    public void crear(String t, String g, String c, int d, String s, String p, String tr) { create(t, g, c, d, s, p, tr); }
    public void crear(String t, String g, String c, int d, String s) { create(t, g, c, d, s); }
    public void desactivar(String id) { deactivate(id); }
    public void editar(String id, String t, String g, String c, int d, String s, String p, String tr) { edit(id, t, g, c, d, s, p, tr); }
}
