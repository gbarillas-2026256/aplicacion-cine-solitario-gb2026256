package org.cinekinal.system.repository;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.cinekinal.system.config.ConexionDB;
import org.cinekinal.system.model.Sala;

public class SalaRepository {

    private final ConexionDB conexionDB = ConexionDB.getInstanciaConexionDB();

    public List<Sala> obtenerTodas() {
        List<Sala> salas = new ArrayList<>();
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_obtener_salas()}")) {
            try (ResultSet resultado = callSP.executeQuery()) {
                while (resultado.next()) {
                    Sala sala = new Sala();
                    sala.setIdSala(resultado.getString("id_sala"));
                    sala.setNombreSala(resultado.getString("nombre_sala"));
                    sala.setTipoSala(resultado.getString("tipo_sala"));
                    sala.setFilas(resultado.getInt("filas"));
                    sala.setColumnas(resultado.getInt("columnas"));
                    salas.add(sala);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener salas: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return salas;
    }

    /** sp_crear_sala tambien genera los asientos y devuelve el id de la sala nueva. */
    public String crear(String nombreSala, String tipoSala, int filas, int columnas) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_crear_sala(?,?,?,?)}")) {
            callSP.setString(1, nombreSala);
            callSP.setString(2, tipoSala);
            callSP.setInt(3, filas);
            callSP.setInt(4, columnas);
            try (ResultSet resultado = callSP.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getString("id_sala");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al crear sala: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }
}
