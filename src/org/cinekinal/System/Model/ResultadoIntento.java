package org.cinekinal.system.model;

/**
 * Resultado de intentar ejecutar una Accion a traves de SolicitudService.
 *
 * EJECUTADA    -> el empleado tenia jerarquia suficiente, la accion ya corrio.
 * SOLICITADA   -> se creo una Solicitud pendiente, la accion todavia NO corrio.
 * NO_AUTORIZADO -> el empleado ni siquiera puede ver/intentar esta accion.
 */
public enum ResultadoIntento {
    EJECUTADA,
    SOLICITADA,
    NO_AUTORIZADO
}
