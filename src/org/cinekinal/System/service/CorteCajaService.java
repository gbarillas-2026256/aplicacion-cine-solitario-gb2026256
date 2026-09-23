package org.cinekinal.system.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Collections;
import java.util.List;
import org.cinekinal.system.model.CorteCaja;
import org.cinekinal.system.model.CorteDetalle;
import org.cinekinal.system.model.CorteGuardadoStatus;
import org.cinekinal.system.repository.CorteCajaRepository;

public class CorteCajaService {

    private final CorteCajaRepository corteRepo = new CorteCajaRepository();

    public CorteCajaRepository.EntradasDelDia obtenerEntradasDelDia(Date fecha) {
        try {
            return corteRepo.obtenerEntradasDelDia(fecha);
        } catch (Exception e) {
            return new CorteCajaRepository.EntradasDelDia(0, BigDecimal.ZERO);
        }
    }

    public List<CorteCajaRepository.EntradasPorPelicula> obtenerEntradasPorPelicula(Date fecha) {
        try {
            return corteRepo.obtenerEntradasPorPelicula(fecha);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Guarda el corte completo: primero la cabecera (con las entradas ya
     * calculadas) y luego cada linea de dulceria. Los totales los
     * recalcula el propio procedimiento en la base de datos conforme se
     * agregan los detalles.
     */
    public CorteGuardadoStatus guardarCorte(String idEmpleado, Date fechaCorte,
                                             BigDecimal totalEntradas, int boletosVendidos,
                                             String observaciones, List<CorteDetalle> detalles) {
        try {
            if (corteRepo.yaExisteCorte(idEmpleado, fechaCorte)) {
                return CorteGuardadoStatus.YA_EXISTE_CORTE_HOY;
            }

            String idCorte = corteRepo.crear(idEmpleado, fechaCorte, totalEntradas,
                    boletosVendidos, observaciones);
            if (idCorte == null) {
                return CorteGuardadoStatus.ERROR_AL_GUARDAR;
            }

            for (CorteDetalle detalle : detalles) {
                corteRepo.agregarDetalle(idCorte, detalle);
            }
            return CorteGuardadoStatus.CORTE_GUARDADO;

        } catch (Exception e) {
            return CorteGuardadoStatus.ERROR_AL_GUARDAR;
        }
    }

    public List<CorteCaja> obtenerPorFecha(Date fechaInicio, Date fechaFin) {
        try {
            return corteRepo.obtenerPorFecha(fechaInicio, fechaFin);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<CorteDetalle> obtenerDetalles(String idCorte) {
        try {
            return corteRepo.obtenerDetalles(idCorte);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
