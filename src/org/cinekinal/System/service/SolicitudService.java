package org.cinekinal.system.service;

import java.util.List;
import org.cinekinal.system.model.Accion;
import org.cinekinal.system.model.Empleado;
import org.cinekinal.system.model.ResultadoIntento;
import org.cinekinal.system.model.Solicitud;
import org.cinekinal.system.repository.SolicitudRepository;

/**
 * Punto unico por donde debe pasar cualquier accion sensible del sistema.
 * Asi el permiso se valida siempre en la capa de Service (no solo
 * ocultando botones en la UI), sin importar desde que Controller se
 * dispare la accion.
 *
 * IMPORTANTE: cuando el Dueño APRUEBA una Solicitud, esta clase no
 * ejecuta la accion original automaticamente (el Runnable de aquel
 * intento ya no existe para entonces). El flujo pensado es: el Dueño ve
 * la solicitud aprobada y el mismo realiza la accion desde su sesion
 * (el llama a intentar(...) y, como es nivel 1, se ejecuta directo). Si
 * mas adelante quieres que se ejecute sola al aprobar, se puede guardar
 * el contexto de la accion en la propia tabla Solicitudes.
 */
public class SolicitudService {

    private final PermisoService permisoService = new PermisoService();
    private final SolicitudRepository solicitudRepo = new SolicitudRepository();

    /**
     * Intenta ejecutar una accion sensible para un empleado.
     *
     * - Si el empleado no puede ni ver la accion -> NO_AUTORIZADO, y
     *   accionDirecta NUNCA se ejecuta.
     * - Si el empleado tiene jerarquia suficiente -> corre accionDirecta
     *   de inmediato y devuelve EJECUTADA.
     * - Si el empleado la ve pero necesita permiso -> crea una Solicitud
     *   pendiente (sin correr accionDirecta) y devuelve SOLICITADA.
     */
    public ResultadoIntento intentar(Empleado empleado, Accion accion, Runnable accionDirecta) {
        if (!permisoService.puedeVer(empleado, accion)) {
            return ResultadoIntento.NO_AUTORIZADO;
        }

        if (permisoService.puedeEjecutarDirecto(empleado, accion)) {
            accionDirecta.run();
            return ResultadoIntento.EJECUTADA;
        }

        solicitudRepo.crear(empleado.getIdEmpleado(), accion.getDescripcion());
        return ResultadoIntento.SOLICITADA;
    }

    public void aprobar(Solicitud solicitud, Empleado aprobador) {
        solicitudRepo.responder(solicitud.getIdSolicitud(), aprobador.getIdEmpleado(), "APROBADA");
    }

    public void rechazar(Solicitud solicitud, Empleado aprobador) {
        solicitudRepo.responder(solicitud.getIdSolicitud(), aprobador.getIdEmpleado(), "RECHAZADA");
    }

    public List<Solicitud> obtenerPendientes() {
        return solicitudRepo.obtenerPendientes();
    }
}
