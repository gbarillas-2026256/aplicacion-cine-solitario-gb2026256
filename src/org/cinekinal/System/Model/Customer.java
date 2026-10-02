package org.cinekinal.system.model;

/**
 * Customer model.
 */
public class Customer extends Cliente {

    public Customer() {
        super();
    }

    public Customer(String idCustomer, String firstName, String lastName, String email,
                    String username, String password, boolean isVip) {
        super(idCustomer, firstName, lastName, email, username, password, isVip);
    }

    public String getIdCustomer() {
        return getIdCliente();
    }

    public void setIdCustomer(String idCustomer) {
        setIdCliente(idCustomer);
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

    public boolean isVip() {
        return isEsVip();
    }

    public void setVip(boolean vip) {
        setEsVip(vip);
    }

    public String getFullName() {
        return getNombreCompleto();
    }
}
