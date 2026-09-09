package org.cinekinal.system.repository;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.cinekinal.system.config.ConexionDB;
import org.cinekinal.system.model.Cliente;

public class ClienteRepository {

    private final ConexionDB conexionDB = ConexionDB.getInstanciaConexionDB();

    public void crear(Cliente cliente) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_crear_cliente(?,?,?,?)}")) {
            callSP.setString(1, cliente.getNombres());
            callSP.setString(2, cliente.getApellidos());
            callSP.setString(3, cliente.getCorreo());
            callSP.setString(4, cliente.getPassword());
            callSP.execute();
        } catch (SQLException e) {
            System.out.println("Error al crear cliente: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    public Cliente login(String correo, String password) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_login_cliente(?,?)}")) {
            callSP.setString(1, correo);
            callSP.setString(2, password);

            try (ResultSet resultado = callSP.executeQuery()) {
                if (resultado.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setIdCliente(resultado.getString("id_cliente"));
                    cliente.setNombres(resultado.getString("nombres"));
                    cliente.setApellidos(resultado.getString("apellidos"));
                    cliente.setCorreo(resultado.getString("correo"));
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
}
