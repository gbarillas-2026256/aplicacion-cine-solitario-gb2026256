package org.cinekinal.system.service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import org.cinekinal.system.model.Empleado;
import org.cinekinal.system.model.EmpleadoRegistroStatus;
import org.cinekinal.system.repository.EmpleadoRepository;

public class EmpleadoService {

    private final EmpleadoRepository empleadoRepo = new EmpleadoRepository();

    public Empleado login(String usuario, String password) {
        try {
            return empleadoRepo.login(usuario, password);
        } catch (Exception e) {
            return null;
        }
    }

    public EmpleadoRegistroStatus registrar(String nombres, String apellidos, String correo,
                                           String usuario, String password, int idPuesto) {
        try {
            Empleado empleado = new Empleado();
            empleado.setNombres(nombres);
            empleado.setApellidos(apellidos);
            empleado.setCorreo(correo);
            empleado.setUsuario(usuario);
            empleado.setPassword(password);
            empleado.setIdPuesto(idPuesto);

            empleadoRepo.crear(empleado);
            return EmpleadoRegistroStatus.EMPLEADO_CREADO;
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                String mensaje = e.getCause().getMessage();
                if (mensaje != null && mensaje.contains("uq_empleados_usuario")) {
                    return EmpleadoRegistroStatus.USUARIO_YA_EXISTE;
                }
                return EmpleadoRegistroStatus.ERROR_AL_CREAR;
            }
            return EmpleadoRegistroStatus.ERROR_AL_CREAR;
        }
    }

    public List<Empleado> obtenerTodos() {
        try {
            return empleadoRepo.obtenerTodos();
        } catch (Exception e) {
            return List.of();
        }
    }

    public boolean desactivar(String idEmpleado, String motivoBaja) {
        try {
            empleadoRepo.desactivar(idEmpleado, motivoBaja);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean desactivar(String idEmpleado) {
        return desactivar(idEmpleado, "Baja administrativa");
    }

    public boolean editar(String idEmpleado, String nombres, String apellidos, String correo, int idPuesto) {
        try {
            empleadoRepo.editar(idEmpleado, nombres, apellidos, correo, idPuesto);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean reportar(String idEmpleado, String idReportador, String tipoReporte, String descripcion) {
        try {
            empleadoRepo.reportar(idEmpleado, idReportador, tipoReporte, descripcion);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // English aliases
    public boolean edit(String employeeId, String firstName, String lastName, String email, int positionId) {
        return editar(employeeId, firstName, lastName, email, positionId);
    }

    public boolean report(String employeeId, String reporterId, String reportType, String description) {
        return reportar(employeeId, reporterId, reportType, description);
    }

    public boolean deactivate(String employeeId, String reason) {
        return desactivar(employeeId, reason);
    }

    public List<Empleado> getAll() {
        return obtenerTodos();
    }
}

