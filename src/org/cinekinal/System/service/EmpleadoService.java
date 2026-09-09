package org.cinekinal.system.service;

import org.cinekinal.system.model.Empleado;
import org.cinekinal.system.repository.EmpleadoRepository;

public class EmpleadoService {

    private final EmpleadoRepository empleadoRepo = new EmpleadoRepository();

    public Empleado login(String usuario, String password) {
        try {
            return empleadoRepo.login(usuario, password);
        } catch (Exception e) {
            return null;
        }
    }
}
