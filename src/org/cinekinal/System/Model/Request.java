package org.cinekinal.system.model;

import java.sql.Timestamp;

/**
 * Request entity representing an action approval request.
 */
public class Request {
    private String idRequest;
    private String action;
    private String reason;
    private String responseReason;
    private String status;
    private Timestamp requestDate;
    private Timestamp responseDate;
    private String requesterFirstName;
    private String requesterLastName;
    private String approverFirstName;
    private String approverLastName;

    public Request() {
    }

    public String getIdRequest() {
        return idRequest;
    }

    public void setIdRequest(String idRequest) {
        this.idRequest = idRequest;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getResponseReason() {
        return responseReason;
    }

    public void setResponseReason(String responseReason) {
        this.responseReason = responseReason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Timestamp requestDate) {
        this.requestDate = requestDate;
    }

    public Timestamp getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(Timestamp responseDate) {
        this.responseDate = responseDate;
    }

    public String getRequesterFirstName() {
        return requesterFirstName;
    }

    public void setRequesterFirstName(String requesterFirstName) {
        this.requesterFirstName = requesterFirstName;
    }

    public String getRequesterLastName() {
        return requesterLastName;
    }

    public void setRequesterLastName(String requesterLastName) {
        this.requesterLastName = requesterLastName;
    }

    public String getApproverFirstName() {
        return approverFirstName;
    }

    public void setApproverFirstName(String approverFirstName) {
        this.approverFirstName = approverFirstName;
    }

    public String getApproverLastName() {
        return approverLastName;
    }

    public void setApproverLastName(String approverLastName) {
        this.approverLastName = approverLastName;
    }

    public String getRequesterFullName() {
        return (requesterFirstName != null ? requesterFirstName : "") + " "
                + (requesterLastName != null ? requesterLastName : "").trim();
    }

    public String getApproverFullName() {
        return (approverFirstName != null ? approverFirstName : "") + " "
                + (approverLastName != null ? approverLastName : "").trim();
    }

    // Compatibility
    public String getIdSolicitud() { return idRequest; }
    public void setIdSolicitud(String id) { this.idRequest = id; }
    public String getAccion() { return action; }
    public void setAccion(String a) { this.action = a; }
    public String getMotivo() { return reason; }
    public void setMotivo(String m) { this.reason = m; }
    public String getMotivoRespuesta() { return responseReason; }
    public void setMotivoRespuesta(String m) { this.responseReason = m; }
    public String getEstado() { return status; }
    public void setEstado(String e) { this.status = e; }
    public Timestamp getFechaSolicitud() { return requestDate; }
    public void setFechaSolicitud(Timestamp f) { this.requestDate = f; }
    public Timestamp getFechaRespuesta() { return responseDate; }
    public void setFechaRespuesta(Timestamp f) { this.responseDate = f; }
    public String getSolicitanteNombres() { return requesterFirstName; }
    public void setSolicitanteNombres(String s) { this.requesterFirstName = s; }
    public String getSolicitanteApellidos() { return requesterLastName; }
    public void setSolicitanteApellidos(String s) { this.requesterLastName = s; }
    public String getAprobadorNombres() { return approverFirstName; }
    public void setAprobadorNombres(String a) { this.approverFirstName = a; }
    public String getAprobadorApellidos() { return approverLastName; }
    public void setAprobadorApellidos(String a) { this.approverLastName = a; }
}
