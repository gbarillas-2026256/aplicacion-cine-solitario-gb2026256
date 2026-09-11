package org.cinekinal.system.repository;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.cinekinal.system.config.ConexionDB;
import org.cinekinal.system.model.Asiento;

public class AsientoRepository {

    private final ConexionDB conexionDB = ConexionDB.getInstanciaConexionDB();

    public List<Asiento> obtenerPorSala(String idSala) {
        List<Asiento> asientos = new ArrayList<>();
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_obtener_asientos_por_sala(?)}")) {
            callSP.setString(1, idSala);
            try (ResultSet resultado = callSP.executeQuery()) {
                while (resultado.next()) {
                    Asiento asiento = new Asiento();
                    asiento.setIdAsiento(resultado.getString("id_asiento"));
                    asiento.setFila(resultado.getString("fila"));
                    asiento.setNumero(resultado.getInt("numero"));
                    asientos.add(asiento);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener asientos: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return asientos;
    }
}
