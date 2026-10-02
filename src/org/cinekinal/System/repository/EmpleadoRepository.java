package org.cinekinal.system.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.cinekinal.system.model.Empleado;

public class EmpleadoRepository {

    public Empleado login(String usuario, String password) {
        return Db.one("{call sp_login_empleado(?,?)}", EmpleadoRepository::mapear, usuario, password);
    }

    public void crear(Empleado empleado) {
        Db.update("{call sp_crear_empleado(?,?,?,?,?,?)}",
                empleado.getNombres(), empleado.getApellidos(), empleado.getCorreo(),
                empleado.getUsuario(), empleado.getPassword(), empleado.getIdPuesto());
    }

    public List<Empleado> obtenerTodos() {
        return Db.list("{call sp_obtener_empleados()}", rs -> {
            Empleado e = mapear(rs);
            e.setActivo(rs.getBoolean("activo"));
            return e;
        });
    }

    public void desactivar(String idEmpleado, String motivoBaja) {
        Db.update("{call sp_desactivar_empleado(?,?)}",
                idEmpleado, motivoBaja != null ? motivoBaja : "Baja administrativa");
    }

    public void desactivar(String idEmpleado) {
        desactivar(idEmpleado, "Baja administrativa");
    }

    public void editar(String idEmpleado, String nombres, String apellidos, String correo, int idPuesto) {
        try {
            Db.update("{call sp_editar_empleado(?,?,?,?,?)}", idEmpleado, nombres, apellidos, correo, idPuesto);
        } catch (RuntimeException e) {
            System.out.println("Aviso: fallback para editar empleado: " + e.getMessage());
            Db.update("UPDATE Empleados SET nombres = ?, apellidos = ?, correo = ?, id_puesto = ? "
                    + "WHERE id_empleado = ?", nombres, apellidos, correo, idPuesto, idEmpleado);
        }
    }

    public void reportar(String idEmpleado, String idReportador, String tipoReporte, String descripcion) {
        try {
            Db.update("{call sp_reportar_empleado(?,?,?,?)}", idEmpleado, idReportador, tipoReporte, descripcion);
        } catch (RuntimeException e) {
            System.out.println("Aviso: registrando reporte directo en BD: " + e.getMessage());
            Db.update("create table if not exists ReportesEmpleados ("
                    + "id_reporte varchar(36) not null primary key, "
                    + "id_empleado varchar(36) not null, "
                    + "id_reportador varchar(36) not null, "
                    + "tipo_reporte varchar(60) not null, "
                    + "descripcion varchar(500) not null, "
                    + "fecha_reporte datetime not null default current_timestamp)");
            Db.update("INSERT INTO ReportesEmpleados(id_reporte, id_empleado, id_reportador, tipo_reporte, "
                    + "descripcion) VALUES(uuid(), ?, ?, ?, ?)", idEmpleado, idReportador, tipoReporte, descripcion);
        }
    }

    private static Empleado mapear(ResultSet rs) throws SQLException {
        Empleado empleado = new Empleado();
        empleado.setIdEmpleado(rs.getString("id_empleado"));
        empleado.setNombres(rs.getString("nombres"));
        empleado.setApellidos(rs.getString("apellidos"));
        empleado.setCorreo(rs.getString("correo"));
        empleado.setUsuario(rs.getString("usuario"));
        empleado.setIdPuesto(rs.getInt("id_puesto"));
        empleado.setNombrePuesto(rs.getString("nombre_puesto"));
        empleado.setNivelJerarquico(rs.getInt("nivel_jerarquico"));
        return empleado;
    }
}
