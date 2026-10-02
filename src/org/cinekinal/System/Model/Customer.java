package org.cinekinal.system.model;

/**
 * Customer entity representing a registered theater client.
 */
public class Customer {
    private String idCustomer;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
    private boolean vip;

    public Customer() {
    }

    public Customer(String idCustomer, String firstName, String lastName, String email,
                    String username, String password, boolean vip) {
        this.idCustomer = idCustomer;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.vip = vip;
    }

    public String getIdCustomer() {
        return idCustomer;
    }

    public void setIdCustomer(String idCustomer) {
        this.idCustomer = idCustomer;
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

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean vip) {
        this.vip = vip;
    }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "").trim();
    }

    // Compatibility getters
    public String getIdCliente() { return idCustomer; }
    public String getNombres() { return firstName; }
    public String getApellidos() { return lastName; }
    public String getCorreo() { return email; }
    public String getUsuario() { return username; }
    public boolean isEsVip() { return vip; }
    public String getNombreCompleto() { return getFullName(); }
}
