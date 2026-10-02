package org.cinekinal.system.model;

/**
 * Result of attempting to register a new Customer.
 */
public enum CustomerRegistrationStatus {
    CUSTOMER_CREATED,
    EMAIL_ALREADY_REGISTERED,
    USERNAME_ALREADY_REGISTERED,
    CREATION_ERROR
}
