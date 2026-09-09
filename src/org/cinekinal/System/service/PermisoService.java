package org.cinekinal.system.service;

import org.cinekinal.system.model.Accion;
import org.cinekinal.system.model.Empleado;

/**
 * Reglas de visibilidad y ejecucion basadas en el nivel_jerarquico del
 * Empleado (1 = Dueño ... 4 = Empleado, entre mas bajo el numero, mas
 * privilegios). No hace falta modificar esta clase para agregar
 * excepciones por Puesto especifico: todo se resuelve comparando contra
 * los umbrales definidos en cada Accion.
 *
 * Usar puedeVer(...) para decidir que botones/menus mostrar en la UI.
 * Usar SolicitudService.intentar(...) para decidir si una accion corre
 * directo o genera una Solicitud — esa es la validacion real, la de la
 * UI es solo para la experiencia de usuario.
 */
public class PermisoService {

    public boolean puedeVer(Empleado empleado, Accion accion) {
        return empleado.getNivelJerarquico() <= accion.getNivelVisible();
    }

    public boolean puedeEjecutarDirecto(Empleado empleado, Accion accion) {
        return empleado.getNivelJerarquico() <= accion.getNivelLibre();
    }

    public boolean necesitaSolicitud(Empleado empleado, Accion accion) {
        return puedeVer(empleado, accion) && !puedeEjecutarDirecto(empleado, accion);
    }
}
