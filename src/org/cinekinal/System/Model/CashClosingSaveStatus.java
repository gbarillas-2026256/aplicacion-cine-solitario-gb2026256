package org.cinekinal.system.model;

/**
 * Result of attempting to save a cash closing.
 */
public enum CashClosingSaveStatus {
    SAVED,
    ALREADY_EXISTS_TODAY,
    SAVE_ERROR
}
