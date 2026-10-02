package org.cinekinal.system.repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;
import org.cinekinal.system.model.Funcion;

public class FuncionRepository {

    /**
     * IMPORTANTE: requiere haber corrido el parche
     * 08_patch_cartelera_id_sala.sql, que agrega f.id_sala al SELECT
     * de sp_obtener_cartelera (lo necesitamos para poder pedir despues
     * los asientos de esa sala con AsientoRepository).
     */
    public List<Funcion> obtenerCartelera() {
        return Db.list("{call sp_obtener_cartelera()}", FuncionRepository::mapear);
    }

    public List<Funcion> obtenerCarteleraPorFecha(Date fecha) {
        try {
            return Db.list("{call sp_obtener_cartelera_por_fecha(?)}", FuncionRepository::mapear, fecha);
        } catch (RuntimeException e) {
            System.out.println("Aviso: No se pudo llamar sp_obtener_cartelera_por_fecha (" + e.getMessage()
                    + "). Filtrando en memoria...");
            return obtenerCartelera().stream()
                    .filter(f -> f.getFecha() != null && f.getFecha().toString().equals(fecha.toString()))
                    .toList();
        }
    }

    public List<Funcion> buscarPorTitulo(String query) {
        if (query == null || query.isBlank()) {
            return obtenerCartelera();
        }
        String filtro = query.trim().toLowerCase();
        return obtenerCartelera().stream()
                .filter(f -> f.getTituloPelicula() != null && f.getTituloPelicula().toLowerCase().contains(filtro))
                .toList();
    }

    public void crear(String idPelicula, String idSala, Date fecha, Time hora, BigDecimal precioBase) {
        Db.update("{call sp_crear_funcion(?,?,?,?,?)}", idPelicula, idSala, fecha, hora, precioBase);
    }

    public boolean actualizarPrecioBase(String idFuncion, BigDecimal nuevoPrecio) {
        try {
            return Db.update("UPDATE Funciones SET precio_base = ? WHERE id_funcion = ?",
                    nuevoPrecio, idFuncion) > 0;
        } catch (RuntimeException e) {
            System.out.println("Error al actualizar precio de funcion: " + e.getMessage());
            return false;
        }
    }

    private static Funcion mapear(java.sql.ResultSet rs) throws SQLException {
        Funcion funcion = new Funcion();
        funcion.setIdFuncion(rs.getString("id_funcion"));
        funcion.setIdSala(rs.getString("id_sala"));
        funcion.setTituloPelicula(rs.getString("titulo"));
        funcion.setDuracionMin(rs.getInt("duracion_min"));
        try { funcion.setGenero(rs.getString("genero")); } catch (SQLException ignored) {}
        try { funcion.setClasificacion(rs.getString("clasificacion")); } catch (SQLException ignored) {}
        try { funcion.setSinopsis(rs.getString("sinopsis")); } catch (SQLException ignored) {}
        try { funcion.setPosterUrl(rs.getString("poster_url")); } catch (SQLException ignored) {}
        try { funcion.setTrailerUrl(rs.getString("trailer_url")); } catch (SQLException ignored) {}
        funcion.setNombreSala(rs.getString("nombre_sala"));
        funcion.setTipoSala(rs.getString("tipo_sala"));
        funcion.setFecha(rs.getDate("fecha"));
        funcion.setHora(rs.getTime("hora"));
        funcion.setPrecioBase(rs.getBigDecimal("precio_base"));
        return funcion;
    }
}
