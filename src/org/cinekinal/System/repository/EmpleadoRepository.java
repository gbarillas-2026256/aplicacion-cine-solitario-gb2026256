package org.cinekinal.system.repository;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

    public void crear(Empleado empleado) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_crear_empleado(?,?,?,?,?,?)}")) {
            callSP.setString(1, empleado.getNombres());
            callSP.setString(2, empleado.getApellidos());
            callSP.setString(3, empleado.getCorreo());
            callSP.setString(4, empleado.getUsuario());
            callSP.setString(5, empleado.getPassword());
            callSP.setInt(6, empleado.getIdPuesto());

            callSP.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al crear empleado: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Empleado> obtenerTodos() {
        List<Empleado> empleados = new ArrayList<>();
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_obtener_empleados()}")) {
            try (ResultSet resultado = callSP.executeQuery()) {
                while (resultado.next()) {
                    Empleado empleado = new Empleado();
                    empleado.setIdEmpleado(resultado.getString("id_empleado"));
                    empleado.setNombres(resultado.getString("nombres"));
                    empleado.setApellidos(resultado.getString("apellidos"));
                    empleado.setCorreo(resultado.getString("correo"));
                    empleado.setUsuario(resultado.getString("usuario"));
                    empleado.setIdPuesto(resultado.getInt("id_puesto"));
                    empleado.setNombrePuesto(resultado.getString("nombre_puesto"));
                    empleado.setNivelJerarquico(resultado.getInt("nivel_jerarquico"));
                    empleado.setActivo(resultado.getBoolean("activo"));
                    empleados.add(empleado);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener empleados: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return empleados;
    }

    public void desactivar(String idEmpleado, String motivoBaja) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_desactivar_empleado(?,?)}")) {
            callSP.setString(1, idEmpleado);
            callSP.setString(2, motivoBaja != null ? motivoBaja : "Baja administrativa");
            callSP.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al desactivar empleado: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void desactivar(String idEmpleado) {
        desactivar(idEmpleado, "Baja administrativa");
    }
}

