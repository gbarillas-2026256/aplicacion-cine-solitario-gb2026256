package org.cinekinal.system.model;

/**
 * Employee model.
 */
public class Employee extends Empleado {

    public Employee() {
        super();
    }

    public String getIdEmployee() {
        return getIdEmpleado();
    }

    public void setIdEmployee(String idEmployee) {
        setIdEmpleado(idEmployee);
    }

    public String getFirstName() {
        return getNombres();
    }

    public void setFirstName(String firstName) {
        setNombres(firstName);
    }

    public String getLastName() {
        return getApellidos();
    }

    public void setLastName(String lastName) {
        setApellidos(lastName);
    }

    public String getEmail() {
        return getCorreo();
    }

    public void setEmail(String email) {
        setCorreo(email);
    }

    public String getUsername() {
        return getUsuario();
    }

    public void setUsername(String username) {
        setUsuario(username);
    }

    public String getPassword() {
        return super.getPassword();
    }

    public void setPassword(String password) {
        super.setPassword(password);
    }

    public boolean isActive() {
        return isActivo();
    }

    public void setActive(boolean active) {
        setActivo(active);
    }

    public String getDeactivationReason() {
        return getMotivoBaja();
    }

    public void setDeactivationReason(String deactivationReason) {
        setMotivoBaja(deactivationReason);
    }

    public int getIdPosition() {
        return getIdPuesto();
    }

    public void setIdPosition(int idPosition) {
        setIdPuesto(idPosition);
    }

    public String getPositionName() {
        return getNombrePuesto();
    }

    public void setPositionName(String positionName) {
        setNombrePuesto(positionName);
    }

    public int getHierarchyLevel() {
        return getNivelJerarquico();
    }

    public void setHierarchyLevel(int hierarchyLevel) {
        setNivelJerarquico(hierarchyLevel);
    }

    public String getFullName() {
        return getNombreCompleto();
    }
}
