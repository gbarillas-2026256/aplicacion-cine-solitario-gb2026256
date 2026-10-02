package org.cinekinal.system.repository;

import org.cinekinal.system.model.Sala;
import java.util.List;

public class SalaRepository {

    public List<Sala> obtenerTodas() {
        return Db.list("{call sp_obtener_salas()}", rs -> {
            Sala sala = new Sala();
            sala.setIdSala(rs.getString("id_sala"));
            sala.setNombreSala(rs.getString("nombre_sala"));
            sala.setTipoSala(rs.getString("tipo_sala"));
            sala.setFilas(rs.getInt("filas"));
            sala.setColumnas(rs.getInt("columnas"));
            return sala;
        });
    }

    /** sp_crear_sala tambien genera los asientos y devuelve el id de la sala nueva. */
    public String crear(String nombreSala, String tipoSala, int filas, int columnas) {
        Sala creada = Db.one("{call sp_crear_sala(?,?,?,?)}",
                rs -> { Sala s = new Sala(); s.setIdSala(rs.getString("id_sala")); return s; },
                nombreSala, tipoSala, filas, columnas);
        return creada != null ? creada.getIdSala() : null;
    }
}
