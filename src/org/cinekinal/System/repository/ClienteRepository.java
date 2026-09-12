package org.cinekinal.system.repository;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.cinekinal.system.config.ConexionDB;
import org.cinekinal.system.model.Cliente;

public class ClienteRepository {

    private final ConexionDB conexionDB = ConexionDB.getInstanciaConexionDB();

    public void crear(Cliente cliente) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_crear_cliente(?,?,?,?,?)}")) {
            callSP.setString(1, cliente.getNombres());
            callSP.setString(2, cliente.getApellidos());
            callSP.setString(3, cliente.getCorreo());
            callSP.setString(4, cliente.getUsuario());
            callSP.setString(5, cliente.getPassword());

            int filasAfectadas = callSP.executeUpdate();
            System.out.println("[DEBUG] sp_crear_cliente -> filas insertadas: " + filasAfectadas);

        } catch (SQLException e) {
            System.out.println("Error al crear cliente: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Cliente login(String usuario, String password) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_login_cliente(?,?)}")) {
            callSP.setString(1, usuario);
            callSP.setString(2, password);

            try (ResultSet resultado = callSP.executeQuery()) {
                if (resultado.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setIdCliente(resultado.getString("id_cliente"));
                    cliente.setNombres(resultado.getString("nombres"));
                    cliente.setApellidos(resultado.getString("apellidos"));
                    cliente.setCorreo(resultado.getString("correo"));
                    cliente.setUsuario(resultado.getString("usuario"));
                    cliente.setEsVip(resultado.getBoolean("es_vip"));
                    return cliente;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al validar login de cliente: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<Cliente> obtenerTodos() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT id_cliente, nombres, apellidos, correo, usuario, es_vip FROM Clientes ORDER BY nombres";
        try (java.sql.PreparedStatement ps = conexionDB.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Cliente c = new Cliente();
                c.setIdCliente(rs.getString("id_cliente"));
                c.setNombres(rs.getString("nombres"));
                c.setApellidos(rs.getString("apellidos"));
                c.setCorreo(rs.getString("correo"));
                c.setUsuario(rs.getString("usuario"));
                c.setEsVip(rs.getBoolean("es_vip"));
                lista.add(c);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar clientes: " + e.getMessage());
        }
        return lista;
    }

    public Cliente obtenerOcrearClienteGenerico() {
        String sql = "SELECT id_cliente, nombres, apellidos, correo, usuario, es_vip FROM Clientes WHERE usuario = 'taquilla_general'";
        try (java.sql.PreparedStatement ps = conexionDB.getConnection().prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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
        } catch (SQLException ignored) {}

        String nuevoId = java.util.UUID.randomUUID().toString();
        String insert = "INSERT INTO Clientes(id_cliente, nombres, apellidos, correo, usuario, password, es_vip) "
                + "VALUES(?, 'Público', 'General', 'taquilla@cinekinal.org', 'taquilla_general', 'taquilla123', false)";
        try (java.sql.PreparedStatement ps = conexionDB.getConnection().prepareStatement(insert)) {
            ps.setString(1, nuevoId);
            ps.executeUpdate();
            Cliente c = new Cliente();
            c.setIdCliente(nuevoId);
            c.setNombres("Público");
            c.setApellidos("General");
            c.setCorreo("taquilla@cinekinal.org");
            c.setUsuario("taquilla_general");
            c.setEsVip(false);
            return c;
        } catch (SQLException e) {
            System.out.println("Error al asegurar cliente de taquilla: " + e.getMessage());
        }
        return null;
    }
}
