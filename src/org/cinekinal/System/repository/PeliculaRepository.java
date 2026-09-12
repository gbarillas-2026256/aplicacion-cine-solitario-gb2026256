package org.cinekinal.system.repository;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.cinekinal.system.config.ConexionDB;
import org.cinekinal.system.model.Pelicula;

public class PeliculaRepository {

    private final ConexionDB conexionDB = ConexionDB.getInstanciaConexionDB();

    public List<Pelicula> obtenerActivas() {
        List<Pelicula> peliculas = new ArrayList<>();
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_obtener_peliculas()}")) {
            try (ResultSet resultado = callSP.executeQuery()) {
                while (resultado.next()) {
                    Pelicula pelicula = new Pelicula();
                    pelicula.setIdPelicula(resultado.getString("id_pelicula"));
                    pelicula.setTitulo(resultado.getString("titulo"));
                    pelicula.setGenero(resultado.getString("genero"));
                    pelicula.setClasificacion(resultado.getString("clasificacion"));
                    pelicula.setDuracionMin(resultado.getInt("duracion_min"));
                    pelicula.setSinopsis(resultado.getString("sinopsis"));
                    pelicula.setPosterUrl(resultado.getString("poster_url"));
                    pelicula.setTrailerUrl(resultado.getString("trailer_url"));
                    peliculas.add(pelicula);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener peliculas: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return peliculas;
    }

    public void crear(String titulo, String genero, String clasificacion, int duracionMin, String sinopsis, String posterUrl, String trailerUrl) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_crear_pelicula(?,?,?,?,?,?,?)}")) {
            callSP.setString(1, titulo);
            callSP.setString(2, genero);
            callSP.setString(3, clasificacion);
            callSP.setInt(4, duracionMin);
            callSP.setString(5, sinopsis);
            callSP.setString(6, posterUrl);
            callSP.setString(7, trailerUrl);
            callSP.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al crear pelicula: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void crear(String titulo, String genero, String clasificacion, int duracionMin, String sinopsis) {
        crear(titulo, genero, clasificacion, duracionMin, sinopsis, null, null);
    }

    public void desactivar(String idPelicula) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_desactivar_pelicula(?)}")) {
            callSP.setString(1, idPelicula);
            callSP.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al desactivar pelicula: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
