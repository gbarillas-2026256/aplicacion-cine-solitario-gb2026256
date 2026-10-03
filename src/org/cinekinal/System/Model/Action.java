package org.cinekinal.system.model;

/**
 * Catalog of sensitive system actions.
 * Hierarchy level: 1 = Owner, 2 = Manager, 3 = Supervisor, 4 = Employee.
 * (Lower numbers mean higher authority/privileges).
 */
public enum Action {

    VIEW_EARNINGS("Ver ganancias", 2, 2),
    VIEW_REPORTS("Ver reportes", 3, 3),
    MANAGE_SHOWTIMES_THEATERS("Administrar funciones y salas", 3, 3),
    MANAGE_MOVIES("Agregar, editar o eliminar peliculas del catalogo", 3, 1),
    CHANGE_PRICE("Cambiar el precio de boletos o funciones", 2, 1),
    DEACTIVATE_EMPLOYEE("Dar de baja a un empleado", 2, 1),
    VIEW_BILLBOARD("Ver cartelera", 4, 4),
    VERIFY_ENTRY("Verificar boleto de entrada", 4, 4),
    BOX_OFFICE_SALE("Registrar venta o entrada", 4, 4),
    CASH_CLOSING("Hacer corte de caja del dia", 4, 4);

    private final String description;
    private final int visibleLevel;
    private final int freeLevel;

    Action(String description, int visibleLevel, int freeLevel) {
        this.description = description;
        this.visibleLevel = visibleLevel;
        this.freeLevel = freeLevel;
    }

    public String getDescription() {
        return description;
    }

    public int getVisibleLevel() {
        return visibleLevel;
    }

    public int getFreeLevel() {
        return freeLevel;
    }

}
