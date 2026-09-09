package org.cinekinal.system.controller;


import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.cinekinal.system.model.User;
import org.cinekinal.system.service.UserService;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

public class LoginController implements Initializable{

    @FXML
    private TextField txtUser;
    @FXML
    private PasswordField pwdPassword;

//    private final AlertInformation alertInfo = new AlertInformation();
    private final UserService userService = new UserService();

    //Credenciales maestras fijas en codigo, tal como pide la hoja de trabajo (ej: admin/123).
    //Ademas de estas, cualquier usuario creado en Registro tambien puede iniciar sesion
    //(se valida contra la base de datos con UserService.login).
    private static final String USUARIO_MAESTRO = "admin";
    private static final String PASSWORD_MAESTRO = "123";

    @Override
    public void initialize(URL url, ResourceBundle rb){
        
    }

    @FXML
    public void onLogin(MouseEvent event){
        String usuario = txtUser.getText().trim();
        String password = pwdPassword.getText().trim();

        if (usuario.equals(USUARIO_MAESTRO) && password.equals(PASSWORD_MAESTRO)) {
            Session.setUsuarioActual(USUARIO_MAESTRO.toUpperCase());
            ViewFactory viewFacto = new ViewFactory();
            viewFacto.viewMainMenu();
            return;
        }

        User usuarioEncontrado = userService.login(usuario, password);
        if (usuarioEncontrado != null) {
            Session.setUsuarioActual(usuarioEncontrado.getUser().toUpperCase());
            ViewFactory viewFacto = new ViewFactory();
            viewFacto.viewMainMenu();
        } else {
            System.out.println ("ERROR INICIO DE SESION");
//            alertInfo.viewAlert("WARNING", "ERROR DE ACCESO",
//                    "CREDENCIALES INCORRECTAS",
//                    "El usuario o la contraseña no son correctos");
        }
    }

    @FXML
    public void onRegister (MouseEvent event){
        ViewFactory viewFacto = new ViewFactory();
        viewFacto.viewRegister();
    }
}
