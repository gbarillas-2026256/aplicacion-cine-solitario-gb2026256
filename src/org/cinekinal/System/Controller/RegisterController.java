package org.cinekinal.system.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.cinekinal.system.model.CustomerRegistrationStatus;
import org.cinekinal.system.service.CustomerService;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Validations;
import org.cinekinal.system.utils.ViewFactory;

public class RegisterController implements Initializable {

    @FXML
    private TextField txtUser;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtLastName;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField pwdPassword;
    @FXML
    private PasswordField pwdConfirmedPassword;

    private final Validations validate = new Validations();
    private final AlertInformation alertInfo = new AlertInformation();
    private final CustomerService customerService = new CustomerService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    public void onCancelarRegistro(MouseEvent event) {
        new ViewFactory().viewLogin();
    }

    @FXML
    public void onRegistrarCliente(MouseEvent event) {
        String username = txtUser.getText().trim();
        String firstName = txtName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = pwdPassword.getText().trim();
        String confirmedPassword = pwdConfirmedPassword.getText().trim();

        if (validate.validateTextEmpty(username) || validate.validateTextEmpty(firstName)
                || validate.validateTextEmpty(lastName) || validate.validateTextEmpty(email)
                || validate.validateTextEmpty(password) || validate.validateTextEmpty(confirmedPassword)) {
            alertInfo.viewAlert("WARNING", "CAMPOS INCOMPLETOS",
                    "FALTAN DATOS",
                    "Llena todos los campos para continuar.");
            return;
        }

        if (!validate.validateEmail(email)) {
            alertInfo.viewAlert("WARNING", "CORREO INVÁLIDO",
                    "ERROR EN EL CAMPO CORREO",
                    "Ingresa un correo con un formato válido.");
            return;
        }

        String fieldExceededLength = "";
        if (!validate.validateTextLength(username, 30)) {
            fieldExceededLength = "El campo USUARIO supera los 30 caracteres.";
        } else if (!validate.validateTextLength(firstName, 60)) {
            fieldExceededLength = "El campo NOMBRES supera los 60 caracteres.";
        } else if (!validate.validateTextLength(lastName, 60)) {
            fieldExceededLength = "El campo APELLIDOS supera los 60 caracteres.";
        } else if (!validate.validateTextLength(email, 80)) {
            fieldExceededLength = "El campo CORREO supera los 80 caracteres.";
        } else if (!validate.validateTextLength(password, 60)) {
            fieldExceededLength = "La CONTRASEÑA supera los 60 caracteres.";
        }

        if (!fieldExceededLength.isEmpty()) {
            alertInfo.viewAlert("WARNING", "CAMPO DEMASIADO LARGO",
                    "ERROR DE LONGITUD",
                    fieldExceededLength);
            return;
        }

        if (!validate.equalsText(password, confirmedPassword)) {
            alertInfo.viewAlert("WARNING", "LAS CONTRASEÑAS NO COINCIDEN",
                    "ERROR DE CONTRASEÑA",
                    "Verifica que ambas contraseñas sean iguales.");
            return;
        }

        CustomerRegistrationStatus result = customerService.register(firstName, lastName, email, username, password);

        switch (result) {
            case CUSTOMER_CREATED -> {
                alertInfo.viewAlert("INFORMATION", "REGISTRO EXITOSO",
                        "CUENTA CREADA",
                        "Tu cuenta se creó correctamente. Ya puedes iniciar sesión.");
                new ViewFactory().viewLogin();
            }
            case EMAIL_ALREADY_REGISTERED -> alertInfo.viewAlert("WARNING", "CORREO EN USO",
                    "ESTE CORREO YA TIENE CUENTA",
                    "Ya existe una cuenta registrada con ese correo. Intenta iniciar sesión.");
            case USERNAME_ALREADY_REGISTERED -> alertInfo.viewAlert("WARNING", "USUARIO EN USO",
                    "ESTE USUARIO YA EXISTE",
                    "Ese nombre de usuario ya está en uso. Elige otro.");
            case CREATION_ERROR -> alertInfo.viewAlert("ERROR", "ERROR AL REGISTRAR",
                    "NO SE PUDO CREAR LA CUENTA",
                    "Ocurrió un error al guardar tus datos. Verifica la conexión a la base de datos.");
        }
    }
}
