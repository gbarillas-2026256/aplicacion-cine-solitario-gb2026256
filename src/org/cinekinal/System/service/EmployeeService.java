package org.cinekinal.system.service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import org.cinekinal.system.model.Employee;
import org.cinekinal.system.model.EmployeeRegistrationStatus;
import org.cinekinal.system.repository.EmployeeRepository;

public class EmployeeService {

    private final EmployeeRepository employeeRepo = new EmployeeRepository();

    public Employee login(String username, String password) {
        try {
            return employeeRepo.login(username, password);
        } catch (Exception e) {
            return null;
        }
    }

    public EmployeeRegistrationStatus register(String firstName, String lastName, String email,
                                             String username, String password, int idPosition) {
        try {
            Employee employee = new Employee();
            employee.setFirstName(firstName);
            employee.setLastName(lastName);
            employee.setEmail(email);
            employee.setUsername(username);
            employee.setPassword(password);
            employee.setIdPosition(idPosition);

            employeeRepo.create(employee);
            return EmployeeRegistrationStatus.EMPLOYEE_CREATED;
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                String message = e.getCause().getMessage();
                if (message != null && message.contains("uq_empleados_usuario")) {
                    return EmployeeRegistrationStatus.USERNAME_ALREADY_EXISTS;
                }
                if (message != null && message.contains("uq_empleados_correo")) {
                    return EmployeeRegistrationStatus.EMAIL_ALREADY_EXISTS;
                }
                return EmployeeRegistrationStatus.CREATION_ERROR;
            }
            return EmployeeRegistrationStatus.CREATION_ERROR;
        }
    }

    public List<Employee> getAll() {
        try {
            return employeeRepo.getAll();
        } catch (Exception e) {
            return List.of();
        }
    }

    public boolean deactivate(String idEmployee, String reason) {
        try {
            employeeRepo.deactivate(idEmployee, reason);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deactivate(String idEmployee) {
        return deactivate(idEmployee, "Baja administrativa");
    }

    public boolean edit(String idEmployee, String firstName, String lastName, String email, int idPosition) {
        try {
            employeeRepo.edit(idEmployee, firstName, lastName, email, idPosition);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean report(String idEmployee, String idReporter, String reportType, String description) {
        try {
            employeeRepo.report(idEmployee, idReporter, reportType, description);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Compatibility aliases
    public EmployeeRegistrationStatus registrar(String n, String a, String c, String u, String p, int pos) {
        return register(n, a, c, u, p, pos);
    }
    public List<Employee> obtenerTodos() { return getAll(); }
    public boolean editar(String id, String n, String a, String c, int p) { return edit(id, n, a, c, p); }
    public boolean reportar(String id, String rep, String t, String d) { return report(id, rep, t, d); }
}
