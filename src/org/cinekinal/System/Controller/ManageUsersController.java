package org.cinekinal.system.controller;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import org.cinekinal.system.model.Empleado;
import org.cinekinal.system.model.EmpleadoRegistroStatus;
import org.cinekinal.system.service.EmpleadoService;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.Validations;
import org.cinekinal.system.utils.ViewFactory;

public class ManageUsersController implements Initializable {

    @FXML
    private TableView<Empleado> tableUsers;
    @FXML
    private TableColumn<Empleado, String> colFullName;
    @FXML
    private TableColumn<Empleado, String> colEmail;
    @FXML
    private TableColumn<Empleado, String> colPuesto;

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
    private ComboBox<String> cmbPuesto;

    private final EmpleadoService empleadoService = new EmpleadoService();
    private final Validations validate = new Validations();
    private final AlertInformation alertInfo = new AlertInformation();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Solo el Dueño (nivel 1) puede gestionar usuarios y subordinados
        if (!Session.esEmpleado() || Session.getEmpleadoActual().getNivelJerarquico() != 1) {
            alertInfo.viewAlert("ERROR", "ACCESO DENEGADO", "Solo el Dueño",
                    "Esta sección es exclusiva para el Dueño del cine.");
            new ViewFactory().viewMainMenu();
            return;
        }

        colFullName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreCompleto()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCorreo()));
        colPuesto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombrePuesto()));

        cmbPuesto.setItems(FXCollections.observableArrayList("Gerente", "Encargado", "Empleado"));
        cmbPuesto.getSelectionModel().select("Empleado");

        cargarTabla();
    }

    private void cargarTabla() {
        List<Empleado> lista = empleadoService.obtenerTodos();
        tableUsers.setItems(FXCollections.observableArrayList(lista));
    }

    @FXML
    public void onAddUser(MouseEvent event) {
        String user = txtUser.getText().trim();
        String name = txtName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = pwdPassword.getText().trim();
        String puestoSeleccionado = cmbPuesto.getValue();

        if (validate.validateTextEmpty(user) || validate.validateTextEmpty(name)
                || validate.validateTextEmpty(lastName) || validate.validateTextEmpty(email)
                || validate.validateTextEmpty(password) || puestoSeleccionado == null) {
            alertInfo.viewAlert("WARNING", "CAMPOS INCOMPLETOS",
                    "FALTAN DATOS",
                    "Llena todos los campos para dar de alta al empleado.");
            return;
        }

        if (!validate.validateEmail(email)) {
            alertInfo.viewAlert("WARNING", "CORREO INVÁLIDO",
                    "ERROR DE CORREO",
                    "Ingresa un correo electrónico con formato válido.");
            return;
        }

        if (!validate.validateTextLength(user, 30) || !validate.validateTextLength(name, 60)
                || !validate.validateTextLength(lastName, 60) || !validate.validateTextLength(email, 80)
                || !validate.validateTextLength(password, 60)) {
            alertInfo.viewAlert("WARNING", "LONGITUD EXCEDIDA",
                    "CAMPOS DEMASIADO LARGOS",
                    "Verifica que los datos no superen los límites de caracteres permitidos.");
            return;
        }

        int idPuesto = switch (puestoSeleccionado) {
            case "Gerente" -> 2;
            case "Encargado" -> 3;
            default -> 4; // Empleado
        };

        EmpleadoRegistroStatus status = empleadoService.registrar(name, lastName, email, user, password, idPuesto);

        switch (status) {
            case EMPLEADO_CREADO -> {
                limpiarFormulario();
                cargarTabla();
                alertInfo.viewAlert("INFORMATION", "EMPLEADO REGISTRADO",
                        "ALTA COMPLETADA",
                        "El empleado se registró correctamente con el puesto de " + puestoSeleccionado + ".");
            }
            case USUARIO_YA_EXISTE -> alertInfo.viewAlert("WARNING", "USUARIO EN USO",
                    "USUARIO YA EXISTE",
                    "Ese nombre de usuario ya está en uso. Elige uno diferente.");
            case CORREO_YA_EXISTE -> alertInfo.viewAlert("WARNING", "CORREO EN USO",
                    "CORREO YA REGISTRADO",
                    "Ese correo ya está registrado en el sistema.");
            case ERROR_AL_CREAR -> alertInfo.viewAlert("ERROR", "ERROR AL REGISTRAR",
                    "NO SE PUDO CREAR EL EMPLEADO",
                    "Ocurrió un error al guardar los datos. Verifica la conexión a la base de datos.");
        }
    }

    @FXML
    public void onDeleteUser(MouseEvent event) {
        Empleado seleccionado = tableUsers.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            alertInfo.viewAlert("WARNING", "SIN SELECCIÓN",
                    "NINGÚN EMPLEADO SELECCIONADO",
                    "Selecciona un empleado de la tabla para darlo de baja.");
            return;
        }

        // Evitar que el Dueño se desactive a sí mismo
        Empleado actual = Session.getEmpleadoActual();
        if (actual != null && actual.getIdEmpleado().equals(seleccionado.getIdEmpleado())) {
            alertInfo.viewAlert("WARNING", "ACCIÓN NO PERMITIDA",
                    "NO PUEDES DARTE DE BAJA A TI MISMO",
                    "La cuenta del Dueño principal no puede ser desactivada desde este módulo.");
            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("CONFIRMAR BAJA");
        confirmacion.setHeaderText("DAR DE BAJA EMPLEADO");
        confirmacion.setContentText("¿Seguro que deseas dar de baja a \"" + seleccionado.getNombres() + " "
                + seleccionado.getApellidos() + "\" (" + seleccionado.getNombrePuesto() + ")?");

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            TextInputDialog dialogMotivo = new TextInputDialog();
            dialogMotivo.setTitle("MOTIVO DE LA BAJA");
            dialogMotivo.setHeaderText("Registro de Motivo de Baja");
            dialogMotivo.setContentText("Indica la razón o motivo de la baja:");

            Optional<String> motivoOpt = dialogMotivo.showAndWait();
            if (motivoOpt.isPresent()) {
                String motivo = motivoOpt.get().trim();
                if (motivo.isEmpty()) {
                    motivo = "Baja administrativa";
                }
                boolean eliminado = empleadoService.desactivar(seleccionado.getIdEmpleado(), motivo);
                if (eliminado) {
                    cargarTabla();
                    alertInfo.viewAlert("INFORMATION", "EMPLEADO DADO DE BAJA",
                            "OPERACIÓN COMPLETADA",
                            "El empleado ha sido desactivado del sistema.\nMotivo registrado: " + motivo);
                } else {
                    alertInfo.viewAlert("ERROR", "ERROR AL DAR DE BAJA",
                            "NO SE PUDO DESACTIVAR",
                            "Ocurrió un error al procesar la baja del empleado.");
                }
            }
        }
    }

    @FXML
    public void onBack(MouseEvent event) {
        new ViewFactory().viewMainMenu();
    }

    private void limpiarFormulario() {
        txtUser.clear();
        txtName.clear();
        txtLastName.clear();
        txtEmail.clear();
        pwdPassword.clear();
        cmbPuesto.getSelectionModel().select("Empleado");
    }
}
