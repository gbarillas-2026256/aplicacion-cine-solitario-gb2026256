package org.cinekinal.system.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.cinekinal.system.model.Customer;
import org.cinekinal.system.model.Employee;
import org.cinekinal.system.service.CustomerService;
import org.cinekinal.system.service.EmployeeService;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.ViewFactory;

public class LoginController implements Initializable {

    @FXML
    private TextField txtUser;
    @FXML
    private PasswordField pwdPassword;

    private final AlertInformation alertInfo = new AlertInformation();
    private final EmployeeService employeeService = new EmployeeService();
    private final CustomerService customerService = new CustomerService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    public void onLogin(MouseEvent event) {
        String username = txtUser.getText().trim();
        String password = pwdPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            alertInfo.viewAlert("WARNING", "CAMPOS INCOMPLETOS",
                    "FALTAN DATOS",
                    "Ingresa tu usuario y tu contraseña.");
            return;
        }

        // 1. Try to authenticate as Employee first
        Employee employee = employeeService.login(username, password);
        if (employee != null) {
            Session.loginAsEmployee(employee);
            new ViewFactory().viewMainMenu();
            return;
        }

        // 2. Try to authenticate as Customer
        Customer customer = customerService.login(username, password);
        if (customer != null) {
            Session.loginAsCustomer(customer);
            new ViewFactory().viewMainMenu();
            return;
        }

        // 3. Invalid credentials
        alertInfo.viewAlert("WARNING", "ERROR DE ACCESO",
                "CREDENCIALES INCORRECTAS",
                "El usuario o la contraseña no son correctos.");
    }

    @FXML
    public void onRegister(MouseEvent event) {
        new ViewFactory().viewRegister();
    }
}
