package org.cinekinal.system.model;

import java.sql.Timestamp;

/**
 * Representa una fila de la tabla Solicitudes: una accion sensible que un
 * empleado con nivel jerarquico insuficiente pidio ejecutar, y que queda
 * pendiente hasta que alguien con mas jerarquia (normalmente el Dueño) la
 * apruebe o la rechace.
 */
public class Solicitud {

    private String idSolicitud;
    private String accion;
    private Timestamp fechaSolicitud;
    private String solicitanteNombres;
    private String solicitanteApellidos;

    public Solicitud() {
    }

    public String getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(String idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public Timestamp getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(Timestamp fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getSolicitanteNombres() {
        return solicitanteNombres;
    }

    public void setSolicitanteNombres(String solicitanteNombres) {
        this.solicitanteNombres = solicitanteNombres;
    }

    public String getSolicitanteApellidos() {
        return solicitanteApellidos;
    }

    public void setSolicitanteApellidos(String solicitanteApellidos) {
        this.solicitanteApellidos = solicitanteApellidos;
    }
}
