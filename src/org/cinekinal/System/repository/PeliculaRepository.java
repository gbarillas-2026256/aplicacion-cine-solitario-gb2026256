package org.cinekinal.system.repository;

import java.util.List;
import org.cinekinal.system.model.Pelicula;

public class PeliculaRepository {

    public List<Pelicula> obtenerActivas() {
        return Db.list("{call sp_obtener_peliculas()}", PeliculaRepository::mapear);
    }

    public void crear(String titulo, String genero, String clasificacion, int duracionMin,
                       String sinopsis, String posterUrl, String trailerUrl) {
        Db.update("{call sp_crear_pelicula(?,?,?,?,?,?,?)}",
                titulo, genero, clasificacion, duracionMin, sinopsis, posterUrl, trailerUrl);
    }

    public void crear(String titulo, String genero, String clasificacion, int duracionMin, String sinopsis) {
        crear(titulo, genero, clasificacion, duracionMin, sinopsis, null, null);
    }

    public void desactivar(String idPelicula) {
        try {
            Db.update("{call sp_desactivar_pelicula(?)}", idPelicula);
        } catch (RuntimeException e) {
            System.out.println("Aviso: fallback para desactivar pelicula: " + e.getMessage());
            Db.update("UPDATE Peliculas SET activa = false WHERE id_pelicula = ?", idPelicula);
        }
    }

    public void editar(String idPelicula, String titulo, String genero, String clasificacion,
                        int duracionMin, String sinopsis, String posterUrl, String trailerUrl) {
        try {
            Db.update("{call sp_editar_pelicula(?,?,?,?,?,?,?,?)}",
                    idPelicula, titulo, genero, clasificacion, duracionMin, sinopsis, posterUrl, trailerUrl);
        } catch (RuntimeException e) {
            System.out.println("Aviso: fallback para editar pelicula: " + e.getMessage());
            Db.update("UPDATE Peliculas SET titulo=?, genero=?, clasificacion=?, duracion_min=?, "
                    + "sinopsis=?, poster_url=?, trailer_url=? WHERE id_pelicula=?",
                    titulo, genero, clasificacion, duracionMin, sinopsis, posterUrl, trailerUrl, idPelicula);
        }
    }

    private static Pelicula mapear(java.sql.ResultSet rs) throws java.sql.SQLException {
        Pelicula pelicula = new Pelicula();
        pelicula.setIdPelicula(rs.getString("id_pelicula"));
        pelicula.setTitulo(rs.getString("titulo"));
        pelicula.setGenero(rs.getString("genero"));
        pelicula.setClasificacion(rs.getString("clasificacion"));
        pelicula.setDuracionMin(rs.getInt("duracion_min"));
        pelicula.setSinopsis(rs.getString("sinopsis"));
        pelicula.setPosterUrl(rs.getString("poster_url"));
        pelicula.setTrailerUrl(rs.getString("trailer_url"));
        return pelicula;
    }
}
