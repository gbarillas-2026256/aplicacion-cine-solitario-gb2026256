package org.cinekinal.system.model;

/**
 * Result of attempting to register a new Employee.
 */
public enum EmployeeRegistrationStatus {
    EMPLOYEE_CREATED,
    USERNAME_ALREADY_EXISTS,
    EMAIL_ALREADY_EXISTS,
    CREATION_ERROR
}
