package org.cinekinal.system.service;

import java.sql.SQLIntegrityConstraintViolationException;
import org.cinekinal.system.model.Cliente;
import org.cinekinal.system.model.ClienteRegistroStatus;
import org.cinekinal.system.repository.ClienteRepository;

public class ClienteService {

    private final ClienteRepository clienteRepo = new ClienteRepository();

    public Cliente login(String correo, String password) {
        try {
            return clienteRepo.login(correo, password);
        } catch (Exception e) {
            return null;
        }
    }

    public ClienteRegistroStatus registrar(String nombres, String apellidos, String correo, String password) {
        try {
            Cliente cliente = new Cliente();
            cliente.setNombres(nombres);
            cliente.setApellidos(apellidos);
            cliente.setCorreo(correo);
            cliente.setPassword(password);

            clienteRepo.crear(cliente);
            return ClienteRegistroStatus.CLIENTE_CREADO;
        } catch (RuntimeException e) {
            //sp_crear_cliente choca con uq_clientes_correo si el correo ya existe
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                return ClienteRegistroStatus.CORREO_YA_REGISTRADO;
            }
            return ClienteRegistroStatus.ERROR_AL_CREAR;
        }
    }
}
