package org.cinekinal.system.service;

import java.util.List;
import org.cinekinal.system.model.Action;
import org.cinekinal.system.model.AttemptResult;
import org.cinekinal.system.model.Employee;
import org.cinekinal.system.model.Request;
import org.cinekinal.system.repository.RequestRepository;

public class RequestService {

    private final PermissionService permissionService = new PermissionService();
    private final RequestRepository requestRepo = new RequestRepository();

    public AttemptResult attempt(Employee employee, Action action, String reason, Runnable directAction) {
        if (!permissionService.canView(employee, action)) {
            return AttemptResult.UNAUTHORIZED;
        }

        if (permissionService.canExecuteDirectly(employee, action)) {
            directAction.run();
            return AttemptResult.EXECUTED;
        }

        requestRepo.create(employee.getIdEmployee(), action.getDescription(), reason);
        return AttemptResult.REQUESTED;
    }

    public void approve(Request request, Employee approver) {
        requestRepo.respond(request.getIdRequest(), approver.getIdEmployee(), "APROBADA", null);
    }

    public void reject(Request request, Employee approver, String rejectionReason) {
        requestRepo.respond(request.getIdRequest(), approver.getIdEmployee(), "RECHAZADA", rejectionReason);
    }

    public List<Request> getPending() {
        return requestRepo.getPending();
    }

    public List<Request> getMyRequests(Employee employee) {
        return requestRepo.getByEmployee(employee.getIdEmployee());
    }

    // Compatibility aliases
    public AttemptResult intentar(Employee e, Action a, String m, Runnable d) { return attempt(e, a, m, d); }
    public void aprobar(Request r, Employee a) { approve(r, a); }
    public void rechazar(Request r, Employee a, String m) { reject(r, a, m); }
    public List<Request> obtenerPendientes() { return getPending(); }
    public List<Request> obtenerMisSolicitudes(Employee e) { return getMyRequests(e); }
}
