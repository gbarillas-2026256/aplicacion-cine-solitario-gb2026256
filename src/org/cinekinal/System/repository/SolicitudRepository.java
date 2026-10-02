package org.cinekinal.system.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.cinekinal.system.model.Solicitud;

public class SolicitudRepository {

    public void crear(String idSolicitante, String accion, String motivo) {
        Db.update("{call sp_crear_solicitud(?,?,?)}", idSolicitante, accion, motivo);
    }

    public void responder(String idSolicitud, String idAprobador, String estado, String motivoRespuesta) {
        Db.update("{call sp_responder_solicitud(?,?,?,?)}", idSolicitud, idAprobador, estado, motivoRespuesta);
    }

    public List<Solicitud> obtenerPendientes() {
        return Db.list("{call sp_obtener_solicitudes_pendientes()}", rs -> {
            Solicitud s = new Solicitud();
            s.setIdSolicitud(rs.getString("id_solicitud"));
            s.setAccion(rs.getString("accion"));
            s.setMotivo(rs.getString("motivo"));
            s.setEstado(rs.getString("estado"));
            s.setFechaSolicitud(rs.getTimestamp("fecha_solicitud"));
            s.setSolicitanteNombres(rs.getString("solicitante_nombres"));
            s.setSolicitanteApellidos(rs.getString("solicitante_apellidos"));
            return s;
        });
    }

    /** Historial completo (cualquier estado) de las solicitudes de UN empleado, para "Mis solicitudes". */
    public List<Solicitud> obtenerPorEmpleado(String idEmpleado) {
        return Db.list("{call sp_obtener_solicitudes_por_empleado(?)}", SolicitudRepository::mapearConRespuesta, idEmpleado);
    }

    private static Solicitud mapearConRespuesta(ResultSet rs) throws SQLException {
        Solicitud s = new Solicitud();
        s.setIdSolicitud(rs.getString("id_solicitud"));
        s.setAccion(rs.getString("accion"));
        s.setMotivo(rs.getString("motivo"));
        s.setEstado(rs.getString("estado"));
        s.setFechaSolicitud(rs.getTimestamp("fecha_solicitud"));
        s.setFechaRespuesta(rs.getTimestamp("fecha_respuesta"));
        s.setMotivoRespuesta(rs.getString("motivo_respuesta"));
        s.setAprobadorNombres(rs.getString("aprobador_nombres"));
        s.setAprobadorApellidos(rs.getString("aprobador_apellidos"));
        return s;
    }
}
