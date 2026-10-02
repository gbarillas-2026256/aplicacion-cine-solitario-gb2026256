package org.cinekinal.system.service;

import org.cinekinal.system.model.Action;
import org.cinekinal.system.model.Employee;

/**
 * Visibility and execution rules based on Employee hierarchy level
 * (1 = Owner ... 4 = Employee).
 */
public class PermissionService {

    public boolean canView(Employee employee, Action action) {
        if (employee == null || action == null) return false;
        return employee.getHierarchyLevel() <= action.getVisibleLevel();
    }

    public boolean canExecuteDirectly(Employee employee, Action action) {
        if (employee == null || action == null) return false;
        return employee.getHierarchyLevel() <= action.getFreeLevel();
    }

    public boolean needsRequest(Employee employee, Action action) {
        return canView(employee, action) && !canExecuteDirectly(employee, action);
    }

    // Compatibility aliases
    public boolean puedeVer(Employee e, Action a) { return canView(e, a); }
    public boolean puedeEjecutarDirecto(Employee e, Action a) { return canExecuteDirectly(e, a); }
    public boolean necesitaSolicitud(Employee e, Action a) { return needsRequest(e, a); }
}
