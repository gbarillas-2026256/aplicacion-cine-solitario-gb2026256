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
    private String motivo;
    private String estado;
    private Timestamp fechaSolicitud;
    private Timestamp fechaRespuesta;
    private String solicitanteNombres;
    private String solicitanteApellidos;
    private String aprobadorNombres;
    private String aprobadorApellidos;
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

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Timestamp getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(Timestamp fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    public String getAprobadorNombres() {
        return aprobadorNombres;
    }

    public void setAprobadorNombres(String aprobadorNombres) {
        this.aprobadorNombres = aprobadorNombres;
    }

    public String getAprobadorApellidos() {
        return aprobadorApellidos;
    }

    public void setAprobadorApellidos(String aprobadorApellidos) {
        this.aprobadorApellidos = aprobadorApellidos;
    }
    
    
}
