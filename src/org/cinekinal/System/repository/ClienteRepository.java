package org.cinekinal.system.repository;

import java.util.List;
import org.cinekinal.system.model.Cliente;

public class ClienteRepository {

    public void crear(Cliente cliente) {
        int filas = Db.update("{call sp_crear_cliente(?,?,?,?,?)}",
                cliente.getNombres(), cliente.getApellidos(), cliente.getCorreo(),
                cliente.getUsuario(), cliente.getPassword());
        System.out.println("[DEBUG] sp_crear_cliente -> filas insertadas: " + filas);
    }

    public Cliente login(String usuario, String password) {
        return Db.one("{call sp_login_cliente(?,?)}", ClienteRepository::mapear, usuario, password);
    }

    public List<Cliente> obtenerTodos() {
        return Db.list("SELECT id_cliente, nombres, apellidos, correo, usuario, es_vip "
                + "FROM Clientes ORDER BY nombres", ClienteRepository::mapear);
    }

    /** Cliente generico usado para ventas en taquilla sin identificar al comprador; lo crea si no existe. */
    public Cliente obtenerOcrearClienteGenerico() {
        Cliente existente = Db.one("SELECT id_cliente, nombres, apellidos, correo, usuario, es_vip "
                + "FROM Clientes WHERE usuario = 'taquilla_general'", ClienteRepository::mapear);
        if (existente != null) {
            return existente;
        }

        String nuevoId = java.util.UUID.randomUUID().toString();
        Db.update("INSERT INTO Clientes(id_cliente, nombres, apellidos, correo, usuario, password, es_vip) "
                + "VALUES(?, 'Público', 'General', 'taquilla@cinekinal.org', 'taquilla_general', 'taquilla123', false)",
                nuevoId);

        Cliente c = new Cliente();
        c.setIdCliente(nuevoId);
        c.setNombres("Público");
        c.setApellidos("General");
        c.setCorreo("taquilla@cinekinal.org");
        c.setUsuario("taquilla_general");
        c.setEsVip(false);
        return c;
    }

    private static Cliente mapear(java.sql.ResultSet rs) throws java.sql.SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getString("id_cliente"));
        c.setNombres(rs.getString("nombres"));
        c.setApellidos(rs.getString("apellidos"));
        c.setCorreo(rs.getString("correo"));
        c.setUsuario(rs.getString("usuario"));
        c.setEsVip(rs.getBoolean("es_vip"));
        return c;
    }
}
