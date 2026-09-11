package org.cinekinal.system.model;

/**
 * Resultado de intentar comprar un boleto (BoletoService.comprar).
 * Mismo patron que ClienteRegistroStatus: el Controller decide que
 * alerta mostrar segun este resultado, sin tener que conocer
 * excepciones de SQL.
 */
public enum BoletoCompraStatus {
    COMPRA_EXITOSA,
    ASIENTO_YA_VENDIDO,
    ERROR_AL_COMPRAR
}
