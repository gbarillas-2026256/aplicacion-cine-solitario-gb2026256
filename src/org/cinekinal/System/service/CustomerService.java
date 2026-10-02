package org.cinekinal.system.service;

import java.sql.SQLIntegrityConstraintViolationException;
import org.cinekinal.system.model.Customer;
import org.cinekinal.system.model.CustomerRegistrationStatus;
import org.cinekinal.system.repository.CustomerRepository;

public class CustomerService {

    private final CustomerRepository customerRepo = new CustomerRepository();

    public Customer login(String username, String password) {
        try {
            return customerRepo.login(username, password);
        } catch (Exception e) {
            return null;
        }
    }

    public CustomerRegistrationStatus register(String firstName, String lastName, String email,
                                                String username, String password) {
        try {
            Customer customer = new Customer();
            customer.setFirstName(firstName);
            customer.setLastName(lastName);
            customer.setEmail(email);
            customer.setUsername(username);
            customer.setPassword(password);

            customerRepo.create(customer);
            return CustomerRegistrationStatus.CUSTOMER_CREATED;
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                String message = e.getCause().getMessage();
                if (message != null && message.contains("uq_clientes_usuario")) {
                    return CustomerRegistrationStatus.USERNAME_ALREADY_REGISTERED;
                }
                return CustomerRegistrationStatus.EMAIL_ALREADY_REGISTERED;
            }
            return CustomerRegistrationStatus.CREATION_ERROR;
        }
    }

    // Compatibility aliases
    public CustomerRegistrationStatus registrar(String n, String a, String c, String u, String p) {
        return register(n, a, c, u, p);
    }
}
