package org.cinekinal.system.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.cinekinal.system.model.Customer;

public class CustomerRepository {

    public void create(Customer customer) {
        Db.update("{call sp_crear_cliente(?,?,?,?,?)}",
                customer.getFirstName(), customer.getLastName(), customer.getEmail(),
                customer.getUsername(), customer.getPassword());
    }

    public Customer login(String username, String password) {
        return Db.one("{call sp_login_cliente(?,?)}", CustomerRepository::map, username, password);
    }

    public List<Customer> getAll() {
        return Db.list("SELECT id_cliente, nombres, apellidos, correo, usuario, es_vip "
                + "FROM Clientes ORDER BY nombres", CustomerRepository::map);
    }

    /** Generic customer used for walk-in box office sales; creates it if it doesn't exist yet. */
    public Customer getOrCreateGenericCustomer() {
        Customer existing = Db.one("SELECT id_cliente, nombres, apellidos, correo, usuario, es_vip "
                + "FROM Clientes WHERE usuario = 'taquilla_general'", CustomerRepository::map);
        if (existing != null) {
            return existing;
        }

        String newId = UUID.randomUUID().toString();
        Db.update("INSERT INTO Clientes(id_cliente, nombres, apellidos, correo, usuario, password, es_vip) "
                + "VALUES(?, 'Público', 'General', 'taquilla@cinekinal.org', 'taquilla_general', 'taquilla123', false)",
                newId);

        Customer c = new Customer();
        c.setIdCustomer(newId);
        c.setFirstName("Público");
        c.setLastName("General");
        c.setEmail("taquilla@cinekinal.org");
        c.setUsername("taquilla_general");
        c.setVip(false);
        return c;
    }

    public static Customer map(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setIdCustomer(rs.getString("id_cliente"));
        c.setFirstName(rs.getString("nombres"));
        c.setLastName(rs.getString("apellidos"));
        c.setEmail(rs.getString("correo"));
        c.setUsername(rs.getString("usuario"));
        c.setVip(rs.getBoolean("es_vip"));
        return c;
    }
}
