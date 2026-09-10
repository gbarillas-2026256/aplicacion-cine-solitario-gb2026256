package org.cinekinal.system.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.cinekinal.system.model.Cliente;
import org.cinekinal.system.model.Empleado;
import org.cinekinal.system.service.ClienteService;
import org.cinekinal.system.service.EmpleadoService;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

public class LoginController implements Initializable {

    @FXML
    private TextField txtUser;
    @FXML
    private PasswordField pwdPassword;

    private final AlertInformation alertInfo = new AlertInformation();
    private final EmpleadoService employeeService = new EmpleadoService();
    private final ClienteService customereService = new ClienteService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    public void onLogin(MouseEvent event) {
        //Empleados y Clientes ahora inician sesion con "usuario" los dos —
        //el formulario es el mismo, y decidimos a cual tabla pertenece
        //DESPUES de intentar contra la primera.
        String usuario = txtUser.getText().trim();
        String password = pwdPassword.getText().trim();

        if (usuario.isEmpty() || password.isEmpty()) {
            alertInfo.viewAlert("WARNING", "CAMPOS INCOMPLETOS",
                    "FALTAN DATOS",
                    "Ingresa tu usuario y tu contraseña.");
            return;
        }

        //1. Primero intentamos como Empleado
        Empleado empleado = employeeService.login(usuario, password);
        if (empleado != null) {
            Session.iniciarSesionComoEmpleado(empleado);
            ViewFactory viewFactory = new ViewFactory();
            viewFactory.viewMainMenu();
            return;
        }

        //2. Si no coincidio con ningun empleado, intentamos como Cliente
        Cliente cliente = customereService.login(usuario, password);
        if (cliente != null) {
            Session.iniciarSesionComoCliente(cliente);
            ViewFactory viewFactory = new ViewFactory();
            viewFactory.viewMainMenu();
            return;
        }

        //3. No coincidio con ninguno de los dos
        alertInfo.viewAlert("WARNING", "ERROR DE ACCESO",
                "CREDENCIALES INCORRECTAS",
                "El usuario o la contraseña no son correctos.");
    }

    @FXML
    public void onRegister(MouseEvent event) {
        ViewFactory viewFactory = new ViewFactory();
        viewFactory.viewRegister();
    }
}
