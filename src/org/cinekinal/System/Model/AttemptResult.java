package org.cinekinal.system.model;

/**
 * Result of attempting to execute an Action through RequestService / PermissionService.
 */
public enum AttemptResult {
    EXECUTED,
    REQUESTED,
    UNAUTHORIZED
}
