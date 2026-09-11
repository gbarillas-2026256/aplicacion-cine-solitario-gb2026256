package org.cinekinal.system.service;

import java.math.BigDecimal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import org.cinekinal.system.model.Boleto;
import org.cinekinal.system.model.BoletoCompraStatus;
import org.cinekinal.system.repository.BoletoRepository;

public class BoletoService {

    private final BoletoRepository boletoRepo = new BoletoRepository();

    public BoletoCompraStatus comprar(String idFuncion, String idCliente, String idAsiento, BigDecimal precioFinal) {
        try {
            boletoRepo.comprar(idFuncion, idCliente, idAsiento, precioFinal);
            return BoletoCompraStatus.COMPRA_EXITOSA;
        } catch (RuntimeException e) {
            e.printStackTrace();
            // uq_boletos_asiento_funcion: alguien mas compro ese asiento primero
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                return BoletoCompraStatus.ASIENTO_YA_VENDIDO;
            }
            return BoletoCompraStatus.ERROR_AL_COMPRAR;
        }
    }

    public List<Boleto> obtenerPorCliente(String idCliente) {
        return boletoRepo.obtenerPorCliente(idCliente);
    }
}
