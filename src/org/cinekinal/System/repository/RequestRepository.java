package org.cinekinal.system.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.cinekinal.system.model.Request;

public class RequestRepository {

    public void create(String idRequester, String action, String reason) {
        Db.update("{call sp_crear_solicitud(?,?,?)}", idRequester, action, reason);
    }

    public void respond(String idRequest, String idApprover, String status, String responseReason) {
        Db.update("{call sp_responder_solicitud(?,?,?,?)}", idRequest, idApprover, status, responseReason);
    }

    public List<Request> getPending() {
        return Db.list("{call sp_obtener_solicitudes_pendientes()}", rs -> {
            Request s = new Request();
            s.setIdRequest(rs.getString("id_solicitud"));
            s.setAction(rs.getString("accion"));
            s.setReason(rs.getString("motivo"));
            s.setStatus(rs.getString("estado"));
            s.setRequestDate(rs.getTimestamp("fecha_solicitud"));
            s.setRequesterFirstName(rs.getString("solicitante_nombres"));
            s.setRequesterLastName(rs.getString("solicitante_apellidos"));
            return s;
        });
    }

    public List<Request> getByEmployee(String idEmployee) {
        return Db.list("{call sp_obtener_solicitudes_por_empleado(?)}", RequestRepository::mapWithResponse, idEmployee);
    }

    public static Request mapWithResponse(ResultSet rs) throws SQLException {
        Request s = new Request();
        s.setIdRequest(rs.getString("id_solicitud"));
        s.setAction(rs.getString("accion"));
        s.setReason(rs.getString("motivo"));
        s.setStatus(rs.getString("estado"));
        s.setRequestDate(rs.getTimestamp("fecha_solicitud"));
        s.setResponseDate(rs.getTimestamp("fecha_respuesta"));
        s.setResponseReason(rs.getString("motivo_respuesta"));
        s.setApproverFirstName(rs.getString("aprobador_nombres"));
        s.setApproverLastName(rs.getString("aprobador_apellidos"));
        return s;
    }

    // Compatibility aliases
    public void crear(String r, String a, String m) { create(r, a, m); }
    public void responder(String req, String app, String st, String res) { respond(req, app, st, res); }
    public List<Request> obtenerPendientes() { return getPending(); }
    public List<Request> obtenerPorEmpleado(String id) { return getByEmployee(id); }
}
