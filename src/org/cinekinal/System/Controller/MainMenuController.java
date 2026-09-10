package org.cinekinal.system.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import org.cinekinal.system.model.Cliente;
import org.cinekinal.system.model.Empleado;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

public class MainMenuController implements Initializable {

    @FXML
    private Label lblWelcome;
    @FXML
    private Label lblAccountType;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (Session.esEmpleado()) {
            Empleado empleado = Session.getEmpleadoActual();
            lblWelcome.setText("Bienvenido, " + empleado.getNombres());
            lblAccountType.setText("Empleado · " + empleado.getNombrePuesto());
        } else if (Session.esCliente()) {
            Cliente cliente = Session.getClienteActual();
            lblWelcome.setText("Bienvenido, " + cliente.getNombres());
            lblAccountType.setText(cliente.isEsVip() ? "Cliente VIP" : "Cliente");
        }
    }

    @FXML
    public void onCerrarSesion(MouseEvent event) {
        Session.cerrarSesion();
        ViewFactory viewFactory = new ViewFactory();
        viewFactory.viewLogin();
    }
}
