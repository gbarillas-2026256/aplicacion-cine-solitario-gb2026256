package org.cinekinal.system.repository;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import org.cinekinal.system.config.ConexionDB;
import org.cinekinal.system.model.Funcion;

public class FuncionRepository {

    private final ConexionDB conexionDB = ConexionDB.getInstanciaConexionDB();

    /**
     * IMPORTANTE: requiere haber corrido el parche
     * 08_patch_cartelera_id_sala.sql, que agrega f.id_sala al SELECT
     * de sp_obtener_cartelera (lo necesitamos para poder pedir despues
     * los asientos de esa sala con AsientoRepository).
     */
    public List<Funcion> obtenerCartelera() {
        List<Funcion> cartelera = new ArrayList<>();
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_obtener_cartelera()}")) {
            try (ResultSet resultado = callSP.executeQuery()) {
                while (resultado.next()) {
                    Funcion funcion = mapearFuncion(resultado);
                    cartelera.add(funcion);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener la cartelera: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return cartelera;
    }

    public List<Funcion> obtenerCarteleraPorFecha(Date fecha) {
        List<Funcion> cartelera = new ArrayList<>();
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_obtener_cartelera_por_fecha(?)}")) {
            callSP.setDate(1, fecha);
            try (ResultSet resultado = callSP.executeQuery()) {
                while (resultado.next()) {
                    Funcion funcion = mapearFuncion(resultado);
                    cartelera.add(funcion);
                }
            }
            return cartelera;
        } catch (SQLException e) {
            System.out.println("Aviso: No se pudo llamar sp_obtener_cartelera_por_fecha (" + e.getMessage() + "). Filtrando en memoria...");
            List<Funcion> todas = obtenerCartelera();
            for (Funcion f : todas) {
                if (f.getFecha() != null && f.getFecha().toString().equals(fecha.toString())) {
                    cartelera.add(f);
                }
            }
            return cartelera;
        }
    }

    private Funcion mapearFuncion(ResultSet resultado) throws SQLException {
        Funcion funcion = new Funcion();
        funcion.setIdFuncion(resultado.getString("id_funcion"));
        funcion.setIdSala(resultado.getString("id_sala"));
        funcion.setTituloPelicula(resultado.getString("titulo"));
        funcion.setDuracionMin(resultado.getInt("duracion_min"));
        try { funcion.setGenero(resultado.getString("genero")); } catch (SQLException ignored) {}
        try { funcion.setClasificacion(resultado.getString("clasificacion")); } catch (SQLException ignored) {}
        try { funcion.setSinopsis(resultado.getString("sinopsis")); } catch (SQLException ignored) {}
        try { funcion.setPosterUrl(resultado.getString("poster_url")); } catch (SQLException ignored) {}
        try { funcion.setTrailerUrl(resultado.getString("trailer_url")); } catch (SQLException ignored) {}
        funcion.setNombreSala(resultado.getString("nombre_sala"));
        funcion.setTipoSala(resultado.getString("tipo_sala"));
        funcion.setFecha(resultado.getDate("fecha"));
        funcion.setHora(resultado.getTime("hora"));
        funcion.setPrecioBase(resultado.getBigDecimal("precio_base"));
        return funcion;
    }

    public void crear(String idPelicula, String idSala, Date fecha, Time hora, BigDecimal precioBase) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_crear_funcion(?,?,?,?,?)}")) {
            callSP.setString(1, idPelicula);
            callSP.setString(2, idSala);
            callSP.setDate(3, fecha);
            callSP.setTime(4, hora);
            callSP.setBigDecimal(5, precioBase);
            callSP.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al crear funcion: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
