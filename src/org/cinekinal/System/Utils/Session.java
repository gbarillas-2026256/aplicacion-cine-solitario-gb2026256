package org.cinekinal.system.utils;

import org.cinekinal.system.model.Cliente;
import org.cinekinal.system.model.Customer;
import org.cinekinal.system.model.Empleado;
import org.cinekinal.system.model.Employee;

/**
 * Stores active session state for the logged-in user (Employee or Customer).
 */
public class Session {

    private static Employee currentEmployee;
    private static Customer currentCustomer;

    private Session() {
    }

    public static void loginAsEmployee(Employee employee) {
        currentEmployee = employee;
        currentCustomer = null;
    }

    public static void loginAsCustomer(Customer customer) {
        currentCustomer = customer;
        currentEmployee = null;
    }

    public static boolean isEmployee() {
        return currentEmployee != null;
    }

    public static boolean isCustomer() {
        return currentCustomer != null;
    }

    public static Employee getCurrentEmployee() {
        return currentEmployee;
    }

    public static Customer getCurrentCustomer() {
        return currentCustomer;
    }

    public static void logout() {
        currentEmployee = null;
        currentCustomer = null;
    }

    // Spanish compatibility methods
    public static void iniciarSesionComoEmpleado(Empleado empleado) {
        if (empleado instanceof Employee emp) {
            loginAsEmployee(emp);
        } else if (empleado != null) {
            Employee emp = new Employee();
            emp.setIdEmpleado(empleado.getIdEmpleado());
            emp.setNombres(empleado.getNombres());
            emp.setApellidos(empleado.getApellidos());
            emp.setCorreo(empleado.getCorreo());
            emp.setUsuario(empleado.getUsuario());
            emp.setPassword(empleado.getPassword());
            emp.setActivo(empleado.isActivo());
            emp.setIdPuesto(empleado.getIdPuesto());
            emp.setNombrePuesto(empleado.getNombrePuesto());
            emp.setNivelJerarquico(empleado.getNivelJerarquico());
            emp.setMotivoBaja(empleado.getMotivoBaja());
            loginAsEmployee(emp);
        } else {
            loginAsEmployee(null);
        }
    }

    public static void iniciarSesionComoCliente(Cliente cliente) {
        if (cliente instanceof Customer c) {
            loginAsCustomer(c);
        } else if (cliente != null) {
            Customer c = new Customer(cliente.getIdCliente(), cliente.getNombres(), cliente.getApellidos(),
                    cliente.getCorreo(), cliente.getUsuario(), cliente.getPassword(), cliente.isEsVip());
            loginAsCustomer(c);
        } else {
            loginAsCustomer(null);
        }
    }

    public static boolean esEmpleado() {
        return isEmployee();
    }

    public static boolean esCliente() {
        return isCustomer();
    }

    public static Empleado getEmpleadoActual() {
        return currentEmployee;
    }

    public static Cliente getClienteActual() {
        return currentCustomer;
    }

    public static void cerrarSesion() {
        logout();
    }
}
