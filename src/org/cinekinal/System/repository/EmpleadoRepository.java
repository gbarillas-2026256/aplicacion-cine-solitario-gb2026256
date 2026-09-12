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

    public void editar(String idEmpleado, String nombres, String apellidos, String correo, int idPuesto) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_editar_empleado(?,?,?,?,?)}")) {
            callSP.setString(1, idEmpleado);
            callSP.setString(2, nombres);
            callSP.setString(3, apellidos);
            callSP.setString(4, correo);
            callSP.setInt(5, idPuesto);
            callSP.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Aviso: intentando fallback para editar empleado: " + e.getMessage());
            try (java.sql.PreparedStatement ps = conexionDB.getConnection().prepareStatement(
                    "UPDATE Empleados SET nombres = ?, apellidos = ?, correo = ?, id_puesto = ? WHERE id_empleado = ?")) {
                ps.setString(1, nombres);
                ps.setString(2, apellidos);
                ps.setString(3, correo);
                ps.setInt(4, idPuesto);
                ps.setString(5, idEmpleado);
                ps.executeUpdate();
            } catch (SQLException ex) {
                System.out.println("Error al editar empleado: " + ex.getMessage());
                throw new RuntimeException(ex);
            }
        }
    }

    public void reportar(String idEmpleado, String idReportador, String tipoReporte, String descripcion) {
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_reportar_empleado(?,?,?,?)}")) {
            callSP.setString(1, idEmpleado);
            callSP.setString(2, idReportador);
            callSP.setString(3, tipoReporte);
            callSP.setString(4, descripcion);
            callSP.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Aviso: intentando registrar reporte en BD directamente: " + e.getMessage());
            try {
                try (java.sql.Statement st = conexionDB.getConnection().createStatement()) {
                    st.executeUpdate("create table if not exists ReportesEmpleados ("
                            + "id_reporte varchar(36) not null primary key, "
                            + "id_empleado varchar(36) not null, "
                            + "id_reportador varchar(36) not null, "
                            + "tipo_reporte varchar(60) not null, "
                            + "descripcion varchar(500) not null, "
                            + "fecha_reporte datetime not null default current_timestamp)");
                }
                try (java.sql.PreparedStatement ps = conexionDB.getConnection().prepareStatement(
                        "INSERT INTO ReportesEmpleados(id_reporte, id_empleado, id_reportador, tipo_reporte, descripcion) VALUES(uuid(), ?, ?, ?, ?)")) {
                    ps.setString(1, idEmpleado);
                    ps.setString(2, idReportador);
                    ps.setString(3, tipoReporte);
                    ps.setString(4, descripcion);
                    ps.executeUpdate();
                }
            } catch (SQLException ex) {
                System.out.println("Error al guardar reporte de empleado: " + ex.getMessage());
                throw new RuntimeException(ex);
            }
        }
    }

    // English aliases
    public void edit(String employeeId, String firstName, String lastName, String email, int positionId) {
        editar(employeeId, firstName, lastName, email, positionId);
    }

    public void report(String employeeId, String reporterId, String reportType, String description) {
        reportar(employeeId, reporterId, reportType, description);
    }

    public void deactivate(String employeeId, String reason) {
        desactivar(employeeId, reason);
    }

    public List<Empleado> getAll() {
        return obtenerTodos();
    }
}

