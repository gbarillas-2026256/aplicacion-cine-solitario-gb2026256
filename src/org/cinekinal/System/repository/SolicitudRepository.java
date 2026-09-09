package org.cinekinal.system.repository;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.cinekinal.system.config.ConexionDB;
import org.cinekinal.system.model.Solicitud;

public class SolicitudRepository {

    private final ConexionDB conexionDB = ConexionDB.getInstanciaConexionDB();

    public void crear(String idSolicitante, String accion) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_crear_solicitud(?,?)}")) {
            callSP.setString(1, idSolicitante);
            callSP.setString(2, accion);
            callSP.execute();
        } catch (SQLException e) {
            System.out.println("Error al crear solicitud: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void responder(String idSolicitud, String idAprobador, String estado) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_responder_solicitud(?,?,?)}")) {
            callSP.setString(1, idSolicitud);
            callSP.setString(2, idAprobador);
            callSP.setString(3, estado);
            callSP.execute();
        } catch (SQLException e) {
            System.out.println("Error al responder solicitud: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Solicitud> obtenerPendientes() {
        List<Solicitud> solicitudes = new ArrayList<>();
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_obtener_solicitudes_pendientes()}")) {
            try (ResultSet resultado = callSP.executeQuery()) {
                while (resultado.next()) {
                    Solicitud solicitud = new Solicitud();
                    solicitud.setIdSolicitud(resultado.getString("id_solicitud"));
                    solicitud.setAccion(resultado.getString("accion"));
                    solicitud.setFechaSolicitud(resultado.getTimestamp("fecha_solicitud"));
                    solicitud.setSolicitanteNombres(resultado.getString("solicitante_nombres"));
                    solicitud.setSolicitanteApellidos(resultado.getString("solicitante_apellidos"));
                    solicitudes.add(solicitud);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener solicitudes pendientes: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return solicitudes;
    }
}
