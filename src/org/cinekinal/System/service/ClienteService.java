package org.cinekinal.system.service;

import java.sql.SQLIntegrityConstraintViolationException;
import org.cinekinal.system.model.Cliente;
import org.cinekinal.system.model.ClienteRegistroStatus;
import org.cinekinal.system.repository.ClienteRepository;

public class ClienteService {

    private final ClienteRepository clienteRepo = new ClienteRepository();

    public Cliente login(String usuario, String password) {
        try {
            return clienteRepo.login(usuario, password);
        } catch (Exception e) {
            return null;
        }
    }

    public ClienteRegistroStatus registrar(String nombres, String apellidos, String correo,
                                            String usuario, String password) {
        try {
            Cliente cliente = new Cliente();
            cliente.setNombres(nombres);
            cliente.setApellidos(apellidos);
            cliente.setCorreo(correo);
            cliente.setUsuario(usuario);
            cliente.setPassword(password);

            clienteRepo.crear(cliente);
            return ClienteRegistroStatus.CLIENTE_CREADO;
        } catch (RuntimeException e) {
            //sp_crear_cliente choca con uq_clientes_correo o uq_clientes_usuario
            //si el correo/usuario ya existe. El mensaje de MySQL trae el nombre
            //del indice que choco, asi que revisamos cual fue.
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                String mensaje = e.getCause().getMessage();
                if (mensaje != null && mensaje.contains("uq_clientes_usuario")) {
                    return ClienteRegistroStatus.USUARIO_YA_REGISTRADO;
                }
                return ClienteRegistroStatus.CORREO_YA_REGISTRADO;
            }
            return ClienteRegistroStatus.ERROR_AL_CREAR;
        }
    }
}
