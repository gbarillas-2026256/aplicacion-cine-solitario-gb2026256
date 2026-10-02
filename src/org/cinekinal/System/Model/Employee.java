package org.cinekinal.system.model;

/**
 * Employee entity representing staff members and theater administrators.
 */
public class Employee {
    private String idEmployee;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
    private boolean active;
    private String inactiveReason;
    private int positionId;
    private String positionName;
    private int hierarchyLevel;

    public Employee() {
    }

    public Employee(String idEmployee, String firstName, String lastName, String email,
                    String username, String password, boolean active, String inactiveReason,
                    int positionId, String positionName, int hierarchyLevel) {
        this.idEmployee = idEmployee;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.active = active;
        this.inactiveReason = inactiveReason;
        this.positionId = positionId;
        this.positionName = positionName;
        this.hierarchyLevel = hierarchyLevel;
    }

    public String getIdEmployee() {
        return idEmployee;
    }

    public void setIdEmployee(String idEmployee) {
        this.idEmployee = idEmployee;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getInactiveReason() {
        return inactiveReason;
    }

    public void setInactiveReason(String inactiveReason) {
        this.inactiveReason = inactiveReason;
    }

    public int getPositionId() {
        return positionId;
    }

    public void setPositionId(int positionId) {
        this.positionId = positionId;
    }

    public int getIdPosition() { return positionId; }
    public void setIdPosition(int id) { this.positionId = id; }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public int getHierarchyLevel() {
        return hierarchyLevel;
    }

    public void setHierarchyLevel(int hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "").trim();
    }

    // Compatibility getters
    public String getIdEmpleado() { return idEmployee; }
    public String getNombres() { return firstName; }
    public String getApellidos() { return lastName; }
    public String getCorreo() { return email; }
    public String getUsuario() { return username; }
    public boolean isActivo() { return active; }
    public String getMotivoBaja() { return inactiveReason; }
    public int getIdPuesto() { return positionId; }
    public String getNombrePuesto() { return positionName; }
    public int getNivelJerarquico() { return hierarchyLevel; }
    public String getNombreCompleto() { return getFullName(); }
}
