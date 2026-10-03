package org.cinekinal.system.repository;

import java.util.List;
import org.cinekinal.system.model.Seat;

public class SeatRepository {

    public List<Seat> getByTheater(String idTheater) {
        return Db.list("{call sp_obtener_asientos_por_sala(?)}", rs -> {
            Seat seat = new Seat();
            seat.setIdSeat(rs.getString("id_asiento"));
            seat.setRow(rs.getString("fila"));
            seat.setNumber(rs.getInt("numero"));
            return seat;
        }, idTheater);
    }
}
