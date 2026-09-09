package org.cinekinal.system.utils;

import org.cinekinal.system.model.Cliente;
import org.cinekinal.system.model.Empleado;

/**
 * Guarda quien inicio sesion durante la ejecucion actual. Como el Login
 * es unico para empleados y clientes, solo UNO de los dos campos va a
 * tener valor a la vez — el otro se queda en null.
 */
public class Session {

    private static Empleado empleadoActual;
    private static Cliente clienteActual;

    private Session() {
    }

    public static void iniciarSesionComoEmpleado(Empleado empleado) {
        empleadoActual = empleado;
        clienteActual = null;
    }

    public static void iniciarSesionComoCliente(Cliente cliente) {
        clienteActual = cliente;
        empleadoActual = null;
    }

    public static boolean esEmpleado() {
        return empleadoActual != null;
    }

    public static boolean esCliente() {
        return clienteActual != null;
    }

    public static Empleado getEmpleadoActual() {
        return empleadoActual;
    }

    public static Cliente getClienteActual() {
        return clienteActual;
    }

    public static void cerrarSesion() {
        empleadoActual = null;
        clienteActual = null;
    }
}
