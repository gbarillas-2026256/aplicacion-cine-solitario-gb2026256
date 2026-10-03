package org.cinekinal.system.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.cinekinal.system.model.Employee;

public class EmployeeRepository {

    public Employee login(String username, String password) {
        return Db.one("{call sp_login_empleado(?,?)}", EmployeeRepository::map, username, password);
    }

    public void create(Employee employee) {
        Db.update("{call sp_crear_empleado(?,?,?,?,?,?)}",
                employee.getFirstName(), employee.getLastName(), employee.getEmail(),
                employee.getUsername(), employee.getPassword(), employee.getIdPosition());
    }

    public List<Employee> getAll() {
        return Db.list("{call sp_obtener_empleados()}", rs -> {
            Employee e = map(rs);
            e.setActive(rs.getBoolean("activo"));
            return e;
        });
    }

    public void deactivate(String idEmployee, String reason) {
        Db.update("{call sp_desactivar_empleado(?,?)}",
                idEmployee, reason != null ? reason : "Baja administrativa");
    }

    public void deactivate(String idEmployee) {
        deactivate(idEmployee, "Baja administrativa");
    }

    public void edit(String idEmployee, String firstName, String lastName, String email, int idPosition) {
        try {
            Db.update("{call sp_editar_empleado(?,?,?,?,?)}", idEmployee, firstName, lastName, email, idPosition);
        } catch (RuntimeException e) {
            Db.update("UPDATE Empleados SET nombres = ?, apellidos = ?, correo = ?, id_puesto = ? "
                    + "WHERE id_empleado = ?", firstName, lastName, email, idPosition, idEmployee);
        }
    }

    public void report(String idEmployee, String idReporter, String reportType, String description) {
        try {
            Db.update("{call sp_reportar_empleado(?,?,?,?)}", idEmployee, idReporter, reportType, description);
        } catch (RuntimeException e) {
            Db.update("create table if not exists ReportesEmpleados ("
                    + "id_reporte varchar(36) not null primary key, "
                    + "id_empleado varchar(36) not null, "
                    + "id_reportador varchar(36) not null, "
                    + "tipo_reporte varchar(60) not null, "
                    + "descripcion varchar(500) not null, "
                    + "fecha_reporte datetime not null default current_timestamp)");
            Db.update("INSERT INTO ReportesEmpleados(id_reporte, id_empleado, id_reportador, tipo_reporte, "
                    + "descripcion) VALUES(uuid(), ?, ?, ?, ?)", idEmployee, idReporter, reportType, description);
        }
    }

    public static Employee map(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setIdEmployee(rs.getString("id_empleado"));
        employee.setFirstName(rs.getString("nombres"));
        employee.setLastName(rs.getString("apellidos"));
        employee.setEmail(rs.getString("correo"));
        employee.setUsername(rs.getString("usuario"));
        employee.setIdPosition(rs.getInt("id_puesto"));
        employee.setPositionName(rs.getString("nombre_puesto"));
        employee.setHierarchyLevel(rs.getInt("nivel_jerarquico"));
        return employee;
    }
}
