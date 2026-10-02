package org.cinekinal.system.model;

import java.sql.Timestamp;

/**
 * Action approval request model.
 */
public class Request extends Solicitud {

    public Request() {
        super();
    }

    public String getIdRequest() {
        return getIdSolicitud();
    }

    public void setIdRequest(String idRequest) {
        setIdSolicitud(idRequest);
    }

    public String getAction() {
        return getAccion();
    }

    public void setAction(String action) {
        setAccion(action);
    }

    public String getReason() {
        return getMotivo();
    }

    public void setReason(String reason) {
        setMotivo(reason);
    }

    public String getResponseReason() {
        return getMotivoRespuesta();
    }

    public void setResponseReason(String responseReason) {
        setMotivoRespuesta(responseReason);
    }

    public String getStatus() {
        return getEstado();
    }

    public void setStatus(String status) {
        setEstado(status);
    }

    public Timestamp getRequestDate() {
        return getFechaSolicitud();
    }

    public void setRequestDate(Timestamp requestDate) {
        setFechaSolicitud(requestDate);
    }

    public Timestamp getResponseDate() {
        return getFechaRespuesta();
    }

    public void setResponseDate(Timestamp responseDate) {
        setFechaRespuesta(responseDate);
    }

    public String getRequesterFirstName() {
        return getSolicitanteNombres();
    }

    public void setRequesterFirstName(String requesterFirstName) {
        setSolicitanteNombres(requesterFirstName);
    }

    public String getRequesterLastName() {
        return getSolicitanteApellidos();
    }

    public void setRequesterLastName(String requesterLastName) {
        setSolicitanteApellidos(requesterLastName);
    }

    public String getApproverFirstName() {
        return getAprobadorNombres();
    }

    public void setApproverFirstName(String approverFirstName) {
        setAprobadorNombres(approverFirstName);
    }

    public String getApproverLastName() {
        return getAprobadorApellidos();
    }

    public void setApproverLastName(String approverLastName) {
        setAprobadorApellidos(approverLastName);
    }

    public String getRequesterFullName() {
        return (getSolicitanteNombres() != null ? getSolicitanteNombres() : "") + " "
                + (getSolicitanteApellidos() != null ? getSolicitanteApellidos() : "").trim();
    }

    public String getApproverFullName() {
        return (getAprobadorNombres() != null ? getAprobadorNombres() : "") + " "
                + (getAprobadorApellidos() != null ? getAprobadorApellidos() : "").trim();
    }
}
