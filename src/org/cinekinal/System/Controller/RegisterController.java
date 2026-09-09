package org.cinekinal.system.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.cinekinal.system.model.ClienteRegistroStatus;
import org.cinekinal.system.service.ClienteService;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Validations;
import org.cinekinal.system.utils.ViewFactory;

public class RegisterController implements Initializable {

    @FXML
    private TextField txtNombres;
    @FXML
    private TextField txtApellidos;
    @FXML
    private TextField txtCorreo;
    @FXML
    private PasswordField pwdPassword;
    @FXML
    private PasswordField pwdConfirmarPassword;

    private final Validations validate = new Validations();
    private final AlertInformation alertInfo = new AlertInformation();
    private final ClienteService clienteService = new ClienteService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    public void onCancelarRegistro(MouseEvent event) {
        ViewFactory viewFactory = new ViewFactory();
        viewFactory.viewLogin();
    }

    @FXML
    public void onRegistrarCliente(MouseEvent event) {
        String nombres = txtNombres.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String correo = txtCorreo.getText().trim();
        String password = pwdPassword.getText().trim();
        String confirmarPassword = pwdConfirmarPassword.getText().trim();

        if (validate.validateTextEmpty(nombres) || validate.validateTextEmpty(apellidos)
                || validate.validateTextEmpty(correo) || validate.validateTextEmpty(password)
                || validate.validateTextEmpty(confirmarPassword)) {
            alertInfo.viewAlert("WARNING", "CAMPOS INCOMPLETOS",
                    "FALTAN DATOS",
                    "Llena todos los campos para continuar.");
            return;
        }

        if (!validate.validateEmail(correo)) {
            alertInfo.viewAlert("WARNING", "CORREO INVÁLIDO",
                    "ERROR EN EL CAMPO CORREO",
                    "Ingresa un correo con un formato válido.");
            return;
        }

        String campoFueraDeLongitud = "";
        if (!validate.validateTextLength(nombres, 60)) {
            campoFueraDeLongitud = "El campo NOMBRES supera los 60 caracteres.";
        } else if (!validate.validateTextLength(apellidos, 60)) {
            campoFueraDeLongitud = "El campo APELLIDOS supera los 60 caracteres.";
        } else if (!validate.validateTextLength(correo, 80)) {
            campoFueraDeLongitud = "El campo CORREO supera los 80 caracteres.";
        } else if (!validate.validateTextLength(password, 60)) {
            campoFueraDeLongitud = "La CONTRASEÑA supera los 60 caracteres.";
        }

        if (!campoFueraDeLongitud.isEmpty()) {
            alertInfo.viewAlert("WARNING", "CAMPO DEMASIADO LARGO",
                    "ERROR DE LONGITUD",
                    campoFueraDeLongitud);
            return;
        }

        if (!validate.equalsText(password, confirmarPassword)) {
            alertInfo.viewAlert("WARNING", "LAS CONTRASEÑAS NO COINCIDEN",
                    "ERROR DE CONTRASEÑA",
                    "Verifica que ambas contraseñas sean iguales.");
            return;
        }

        ClienteRegistroStatus resultado = clienteService.registrar(nombres, apellidos, correo, password);

        switch (resultado) {
            case CLIENTE_CREADO -> {
                alertInfo.viewAlert("INFORMATION", "REGISTRO EXITOSO",
                        "CUENTA CREADA",
                        "Tu cuenta se creó correctamente. Ya puedes iniciar sesión.");
                ViewFactory viewFactory = new ViewFactory();
                viewFactory.viewLogin();
            }
            case CORREO_YA_REGISTRADO -> alertInfo.viewAlert("WARNING", "CORREO EN USO",
                    "ESTE CORREO YA TIENE CUENTA",
                    "Ya existe una cuenta registrada con ese correo. Intenta iniciar sesión.");
            case ERROR_AL_CREAR -> alertInfo.viewAlert("ERROR", "ERROR AL REGISTRAR",
                    "NO SE PUDO CREAR LA CUENTA",
                    "Ocurrió un error al guardar tus datos. Verifica la conexión a la base de datos.");
        }
    }
}
