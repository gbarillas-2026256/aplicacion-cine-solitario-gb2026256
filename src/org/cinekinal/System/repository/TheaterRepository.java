package org.cinekinal.system.repository;

import java.util.List;
import org.cinekinal.system.model.Theater;

public class TheaterRepository {

    public List<Theater> getAll() {
        return Db.list("{call sp_obtener_salas()}", rs -> {
            Theater theater = new Theater();
            theater.setIdTheater(rs.getString("id_sala"));
            theater.setName(rs.getString("nombre_sala"));
            theater.setType(rs.getString("tipo_sala"));
            theater.setRows(rs.getInt("filas"));
            theater.setColumns(rs.getInt("columnas"));
            return theater;
        });
    }

    /** sp_crear_sala also generates seats and returns the new theater id. */
    public String create(String name, String type, int rows, int columns) {
        Theater created = Db.one("{call sp_crear_sala(?,?,?,?)}",
                rs -> {
                    Theater t = new Theater();
                    t.setIdTheater(rs.getString("id_sala"));
                    return t;
                },
                name, type, rows, columns);
        return created != null ? created.getIdTheater() : null;
    }
}
