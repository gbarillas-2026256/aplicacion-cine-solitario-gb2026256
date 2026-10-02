package org.cinekinal.system.repository;

import java.util.List;
import org.cinekinal.system.model.Asiento;

public class AsientoRepository {

    public List<Asiento> obtenerPorSala(String idSala) {
        return Db.list("{call sp_obtener_asientos_por_sala(?)}", rs -> {
            Asiento asiento = new Asiento();
            asiento.setIdAsiento(rs.getString("id_asiento"));
            asiento.setFila(rs.getString("fila"));
            asiento.setNumero(rs.getInt("numero"));
            return asiento;
        }, idSala);
    }
}
