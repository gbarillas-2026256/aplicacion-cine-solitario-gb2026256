package org.cinekinal.system.repository;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.cinekinal.system.config.ConexionDB;
import org.cinekinal.system.model.Empleado;

public class EmpleadoRepository {

    private final ConexionDB conexionDB = ConexionDB.getInstanciaConexionDB();

    public Empleado login(String usuario, String password) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_login_empleado(?,?)}")) {
            callSP.setString(1, usuario);
            callSP.setString(2, password);

            try (ResultSet resultado = callSP.executeQuery()) {
                if (resultado.next()) {
                    Empleado empleado = new Empleado();
                    empleado.setIdEmpleado(resultado.getString("id_empleado"));
                    empleado.setNombres(resultado.getString("nombres"));
                    empleado.setApellidos(resultado.getString("apellidos"));
                    empleado.setCorreo(resultado.getString("correo"));
                    empleado.setUsuario(resultado.getString("usuario"));
                    empleado.setIdPuesto(resultado.getInt("id_puesto"));
                    empleado.setNombrePuesto(resultado.getString("nombre_puesto"));
                    empleado.setNivelJerarquico(resultado.getInt("nivel_jerarquico"));
                    return empleado;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al validar login de empleado: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }
}
