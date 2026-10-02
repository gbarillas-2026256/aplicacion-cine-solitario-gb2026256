package org.cinekinal.system.utils;

import org.cinekinal.system.model.Customer;
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

    public static boolean esEmpleado() {
        return isEmployee();
    }

    public static boolean esCliente() {
        return isCustomer();
    }
}
