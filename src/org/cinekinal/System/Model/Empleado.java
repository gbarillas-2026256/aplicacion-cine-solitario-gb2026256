package org.cinekinal.system.model;

public class Empleado {
    private String idEmpleado;
    private String nombres;
    private String apellidos;
    private String correo;
    private String usuario;
    private String password;
    private boolean activo;
    private String motivoBaja;

    //Datos del puesto, "aplanados" aqui porque sp_login_empleado ya los
    //trae con un JOIN en la misma fila. Si mas adelante se necesita
    //administrar los puestos por separado, ahi si conviene una clase
    //Puesto aparte.
    private int idPuesto;
    private String nombrePuesto;
    private int nivelJerarquico;

    public Empleado() {
    }

    public String getNombreCompleto() {
        return (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "").trim();
    }

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public int getIdPuesto() {
        return idPuesto;
    }

    public void setIdPuesto(int idPuesto) {
        this.idPuesto = idPuesto;
    }

    public String getNombrePuesto() {
        return nombrePuesto;
    }

    public void setNombrePuesto(String nombrePuesto) {
        this.nombrePuesto = nombrePuesto;
    }

    public int getNivelJerarquico() {
        return nivelJerarquico;
    }

    public void setNivelJerarquico(int nivelJerarquico) {
        this.nivelJerarquico = nivelJerarquico;
    }

    public String getMotivoBaja() {
        return motivoBaja;
    }

    public void setMotivoBaja(String motivoBaja) {
        this.motivoBaja = motivoBaja;
    }

    // English alias methods
    public String getEmployeeId() { return idEmpleado; }
    public String getFirstName() { return nombres; }
    public String getLastName() { return apellidos; }
    public String getEmail() { return correo; }
    public String getUsername() { return usuario; }
    public int getPositionId() { return idPuesto; }
    public String getPositionName() { return nombrePuesto; }
    public int getHierarchyLevel() { return nivelJerarquico; }
    public String getDeactivationReason() { return motivoBaja; }
}

