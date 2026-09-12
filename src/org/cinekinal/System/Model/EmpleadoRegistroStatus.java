package org.cinekinal.system.model;

/**
 * Resultado de intentar registrar un Empleado nuevo (usado por
 * ManageUsersController). Captura violaciones de restricciones únicas
 * como uq_empleados_usuario.
 */
public enum EmpleadoRegistroStatus {
    EMPLEADO_CREADO,
    USUARIO_YA_EXISTE,
    CORREO_YA_EXISTE,
    ERROR_AL_CREAR
}
