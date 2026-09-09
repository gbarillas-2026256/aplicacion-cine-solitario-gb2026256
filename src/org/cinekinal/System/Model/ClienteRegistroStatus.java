package org.cinekinal.system.model;

/**
 * Resultado de intentar registrar un Cliente nuevo (usado por
 * RegisterController). El correo tiene una restriccion "unique" en la
 * base de datos (uq_clientes_correo), asi que CORREO_YA_REGISTRADO se
 * detecta capturando esa violacion en ClienteService.
 */
public enum ClienteRegistroStatus {
    CLIENTE_CREADO,
    CORREO_YA_REGISTRADO,
    ERROR_AL_CREAR
}
