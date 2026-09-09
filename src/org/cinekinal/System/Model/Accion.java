package org.cinekinal.system.model;

/**
 * Catalogo de acciones sensibles del sistema. Cada accion define, en base
 * al nivel_jerarquico del Puesto (1 = Dueño, 2 = Gerente, 3 = Encargado,
 * 4 = Empleado; entre mas bajo el numero, mas privilegios):
 *
 *  - nivelVisible: el nivel jerarquico MAS ALTO (numero mas grande) que
 *    todavia puede ver/intentar esta accion. Cualquiera con un numero
 *    mayor a este ni siquiera deberia ver la opcion en el menu.
 *
 *  - nivelLibre: el nivel jerarquico MAS ALTO que puede ejecutarla
 *    DIRECTAMENTE, sin pedir permiso. Cualquiera entre (nivelLibre,
 *    nivelVisible] puede verla e intentarla, pero le genera una
 *    Solicitud pendiente de aprobacion.
 *
 * Ejemplo: CAMBIAR_PRECIO(2, 1) -> el Gerente (nivel 2) la ve y puede
 * intentarla, pero como 2 > nivelLibre(1), le genera una Solicitud.
 * El Dueño (nivel 1) la ejecuta directo porque 1 <= nivelLibre(1).
 * El Encargado (nivel 3) y el Empleado (nivel 4) ni siquiera la ven,
 * porque 3 y 4 son mayores que nivelVisible(2).
 *
 * Para agregar una accion nueva no hay que tocar nada mas: solo se
 * agrega una linea aqui con sus dos umbrales.
 */
public enum Accion {

    VER_GANANCIAS("Ver ganancias", 2, 2),
    VER_REPORTES("Ver reportes", 3, 3),
    ADMINISTRAR_FUNCIONES_SALAS("Administrar funciones y salas", 3, 3),
    ADMINISTRAR_PELICULAS("Agregar, editar o eliminar peliculas del catalogo", 3, 1),
    CAMBIAR_PRECIO("Cambiar el precio de boletos o funciones", 2, 1),
    DAR_BAJA_EMPLEADO("Dar de baja a un empleado", 2, 1),
    VER_CARTELERA("Ver cartelera", 4, 4),
    VERIFICAR_ENTRADA("Verificar boleto de entrada", 4, 4),
    REGISTRAR_VENTA("Registrar venta o entrada", 4, 4);

    private final String descripcion;
    private final int nivelVisible;
    private final int nivelLibre;

    Accion(String descripcion, int nivelVisible, int nivelLibre) {
        this.descripcion = descripcion;
        this.nivelVisible = nivelVisible;
        this.nivelLibre = nivelLibre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getNivelVisible() {
        return nivelVisible;
    }

    public int getNivelLibre() {
        return nivelLibre;
    }
}
