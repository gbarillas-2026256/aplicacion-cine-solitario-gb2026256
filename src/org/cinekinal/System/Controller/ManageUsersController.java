package org.cinekinal.system.controller;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.cinekinal.system.model.Empleado;
import org.cinekinal.system.model.EmpleadoRegistroStatus;
import org.cinekinal.system.service.EmpleadoService;
import org.cinekinal.system.utils.AlertInformation;
import org.cinekinal.system.utils.Session;
import org.cinekinal.system.utils.Validations;
import org.cinekinal.system.utils.ViewFactory;

/**
 * Controlador para la Gestión Unificada de Empleados y Subordinados (Opción 2: Diálogos Modales).
 * Ofrece una vista limpia centrada en el listado de personal activo con opciones para:
 *  - Agregar empleado (modal de alta)
 *  - Gestionar empleado seleccionado (modal con opciones de Editar, Dar de baja o Reportar).
 */
public class ManageUsersController implements Initializable {

    @FXML
    private TableView<Empleado> tableUsers;
    @FXML
    private TableColumn<Empleado, String> colUser;
    @FXML
    private TableColumn<Empleado, String> colFullName;
    @FXML
    private TableColumn<Empleado, String> colEmail;
    @FXML
    private TableColumn<Empleado, String> colPuesto;

    @FXML
    private Label lblStatus;
    @FXML
    private Button btnGestionarEmpleado;

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

        colUser.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsuario()));
        colFullName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreCompleto()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCorreo()));
        colPuesto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombrePuesto()));

        tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                btnGestionarEmpleado.setDisable(true);
                lblStatus.setText("Ningún empleado seleccionado. Haz clic en una fila para seleccionarlo.");
            } else {
                btnGestionarEmpleado.setDisable(false);
                lblStatus.setText("Seleccionado: " + newVal.getNombreCompleto()
                        + " (" + newVal.getNombrePuesto() + ") · Haz clic en GESTIONAR para ver acciones.");
            }
        });

        cargarTabla();
    }

    private void cargarTabla() {
        List<Empleado> lista = empleadoService.obtenerTodos();
        tableUsers.setItems(FXCollections.observableArrayList(lista));
        btnGestionarEmpleado.setDisable(true);
        lblStatus.setText("Total de empleados activos: " + lista.size());
    }

    @FXML
    public void onTableClicked(MouseEvent event) {
        if (event.getClickCount() == 2 && tableUsers.getSelectionModel().getSelectedItem() != null) {
            onGestionarEmpleado(null);
        }
    }

    // =========================================================================
    // MODAL 1: AGREGAR EMPLEADO
    // =========================================================================
    @FXML
    public void onAgregarEmpleado(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("ALTA DE EMPLEADO");
        dialog.setHeaderText("Registro de Nuevo Colaborador / Subordinado");
        aplicarEstiloDialogo(dialog);

        ButtonType btnGuardarType = new ButtonType("REGISTRAR EMPLEADO", ButtonData.OK_DONE);
        ButtonType btnCancelarType = new ButtonType("CANCELAR", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardarType, btnCancelarType);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 20, 16, 20));

        TextField txtUser = new TextField();
        txtUser.setPromptText("Nombre de usuario para login");
        txtUser.getStyleClass().add("eva-field");

        TextField txtName = new TextField();
        txtName.setPromptText("Nombres del colaborador");
        txtName.getStyleClass().add("eva-field");

        TextField txtLastName = new TextField();
        txtLastName.setPromptText("Apellidos del colaborador");
        txtLastName.getStyleClass().add("eva-field");

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("correo@ejemplo.com");
        txtEmail.getStyleClass().add("eva-field");

        PasswordField pwdPassword = new PasswordField();
        pwdPassword.setPromptText("Contraseña de acceso");
        pwdPassword.getStyleClass().add("eva-field");

        ComboBox<String> cmbPuesto = new ComboBox<>(FXCollections.observableArrayList("Gerente", "Encargado", "Empleado"));
        cmbPuesto.getSelectionModel().select("Empleado");
        cmbPuesto.setMaxWidth(Double.MAX_VALUE);
        cmbPuesto.getStyleClass().add("eva-field");

        grid.add(crearLabel("USUARIO:"), 0, 0);
        grid.add(txtUser, 1, 0);

        grid.add(crearLabel("NOMBRES:"), 0, 1);
        grid.add(txtName, 1, 1);

        grid.add(crearLabel("APELLIDOS:"), 0, 2);
        grid.add(txtLastName, 1, 2);

        grid.add(crearLabel("CORREO:"), 0, 3);
        grid.add(txtEmail, 1, 3);

        grid.add(crearLabel("CONTRASEÑA:"), 0, 4);
        grid.add(pwdPassword, 1, 4);

        grid.add(crearLabel("PUESTO / ROL:"), 0, 5);
        grid.add(cmbPuesto, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == btnGuardarType) {
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
                        "FALTAN DATOS", "Llena todos los campos para dar de alta al empleado.");
                return;
            }

            if (!validate.validateEmail(email)) {
                alertInfo.viewAlert("WARNING", "CORREO INVÁLIDO",
                        "ERROR DE FORMATO", "Ingresa un correo electrónico con formato válido.");
                return;
            }

            if (!validate.validateTextLength(user, 30) || !validate.validateTextLength(name, 60)
                    || !validate.validateTextLength(lastName, 60) || !validate.validateTextLength(email, 80)
                    || !validate.validateTextLength(password, 60)) {
                alertInfo.viewAlert("WARNING", "LONGITUD EXCEDIDA",
                        "CAMPOS DEMASIADO LARGOS", "Verifica que los datos no superen los límites de caracteres permitidos.");
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
                    cargarTabla();
                    alertInfo.viewAlert("INFORMATION", "EMPLEADO REGISTRADO",
                            "ALTA COMPLETADA",
                            "El colaborador \"" + name + " " + lastName + "\" se registró correctamente con el rol de " + puestoSeleccionado + ".");
                }
                case USUARIO_YA_EXISTE -> alertInfo.viewAlert("WARNING", "USUARIO EN USO",
                        "USUARIO YA EXISTE", "Ese nombre de usuario ya está en uso. Elige uno diferente.");
                case CORREO_YA_EXISTE -> alertInfo.viewAlert("WARNING", "CORREO EN USO",
                        "CORREO YA REGISTRADO", "Ese correo ya está registrado en el sistema.");
                case ERROR_AL_CREAR -> alertInfo.viewAlert("ERROR", "ERROR AL REGISTRAR",
                        "NO SE PUDO CREAR EL EMPLEADO", "Ocurrió un error al guardar los datos en la base de datos.");
            }
        }
    }

    // =========================================================================
    // MODAL 2: GESTIONAR EMPLEADO SELECCIONADO (Acciones: Editar / Reportar / Dar de baja)
    // =========================================================================
    @FXML
    public void onGestionarEmpleado(ActionEvent event) {
        Empleado seleccionado = tableUsers.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alertInfo.viewAlert("WARNING", "SIN SELECCIÓN",
                    "NINGÚN EMPLEADO SELECCIONADO", "Selecciona un empleado de la tabla para gestionarlo.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("GESTIÓN DE EMPLEADO");
        dialog.setHeaderText("Gestión de Colaborador: " + seleccionado.getNombreCompleto());
        aplicarEstiloDialogo(dialog);

        VBox content = new VBox(14);
        content.setPadding(new Insets(16, 20, 16, 20));

        // Tarjeta resumen del empleado
        VBox cardInfo = new VBox(5);
        cardInfo.setStyle("-fx-background-color: #202226; -fx-padding: 12; -fx-border-color: #33352E; -fx-border-width: 1; -fx-border-radius: 2;");
        Label lblInfo1 = new Label("👤 NOMBRE: " + seleccionado.getNombreCompleto());
        lblInfo1.setStyle("-fx-text-fill: #FF6A13; -fx-font-weight: bold; -fx-font-size: 13px;");
        Label lblInfo2 = new Label("USUARIO: " + seleccionado.getUsuario() + "   ·   ROL / PUESTO: " + seleccionado.getNombrePuesto());
        lblInfo2.setStyle("-fx-text-fill: #ECEDE9; -fx-font-size: 12px;");
        Label lblInfo3 = new Label("CORREO: " + seleccionado.getCorreo());
        lblInfo3.setStyle("-fx-text-fill: #9AA095; -fx-font-size: 11.5px;");
        cardInfo.getChildren().addAll(lblInfo1, lblInfo2, lblInfo3);

        Label lblAccionTitle = new Label("SELECCIONA LA ACCIÓN A REALIZAR:");
        lblAccionTitle.getStyleClass().add("eva-label");

        // Botones de acción principales
        Button btnEditar = new Button("✏️  EDITAR INFORMACIÓN PERSONAL / PUESTO");
        btnEditar.setMaxWidth(Double.MAX_VALUE);
        btnEditar.setStyle("-fx-background-color: #202226; -fx-text-fill: #39FF6A; -fx-border-color: #1F9C3D; -fx-border-width: 1.2; -fx-padding: 10 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;");
        btnEditar.setOnAction(e -> {
            dialog.setResult("EDITAR");
            dialog.close();
        });

        Button btnReportar = new Button("⚠️  REGISTRAR REPORTE O INCIDENCIA DISCIPLINARIA");
        btnReportar.setMaxWidth(Double.MAX_VALUE);
        btnReportar.setStyle("-fx-background-color: #202226; -fx-text-fill: #FFD500; -fx-border-color: #FFD500; -fx-border-width: 1.2; -fx-padding: 10 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;");
        btnReportar.setOnAction(e -> {
            dialog.setResult("REPORTAR");
            dialog.close();
        });

        Button btnBaja = new Button("❌  DAR DE BAJA AL EMPLEADO");
        btnBaja.setMaxWidth(Double.MAX_VALUE);
        btnBaja.setStyle("-fx-background-color: #202226; -fx-text-fill: #C1121F; -fx-border-color: #C1121F; -fx-border-width: 1.2; -fx-padding: 10 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;");
        btnBaja.setOnAction(e -> {
            dialog.setResult("BAJA");
            dialog.close();
        });

        content.getChildren().addAll(cardInfo, lblAccionTitle, btnEditar, btnReportar, btnBaja);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(new ButtonType("CANCELAR", ButtonData.CANCEL_CLOSE));

        Optional<String> opcion = dialog.showAndWait();
        if (opcion.isPresent()) {
            switch (opcion.get()) {
                case "EDITAR" -> abrirDialogoEditar(seleccionado);
                case "REPORTAR" -> abrirDialogoReportar(seleccionado);
                case "BAJA" -> procederDarDeBaja(seleccionado);
            }
        }
    }

    // =========================================================================
    // SUB-MODAL 2.1: EDITAR EMPLEADO
    // =========================================================================
    private void abrirDialogoEditar(Empleado emp) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("EDITAR EMPLEADO");
        dialog.setHeaderText("Modificar Datos: " + emp.getNombreCompleto());
        aplicarEstiloDialogo(dialog);

        ButtonType btnGuardar = new ButtonType("GUARDAR CAMBIOS", ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("CANCELAR", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, btnCancelar);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 20, 16, 20));

        TextField txtName = new TextField(emp.getNombres());
        txtName.getStyleClass().add("eva-field");

        TextField txtLastName = new TextField(emp.getApellidos());
        txtLastName.getStyleClass().add("eva-field");

        TextField txtEmail = new TextField(emp.getCorreo());
        txtEmail.getStyleClass().add("eva-field");

        ComboBox<String> cmbPuesto = new ComboBox<>(FXCollections.observableArrayList("Dueño", "Gerente", "Encargado", "Empleado"));
        cmbPuesto.setValue(emp.getNombrePuesto());
        cmbPuesto.setMaxWidth(Double.MAX_VALUE);
        cmbPuesto.getStyleClass().add("eva-field");

        // Si es el Dueño principal, protegemos el cambio de rol a menor rango accidental
        if (emp.getNivelJerarquico() == 1) {
            cmbPuesto.setDisable(true);
        }

        grid.add(crearLabel("NOMBRES:"), 0, 0);
        grid.add(txtName, 1, 0);

        grid.add(crearLabel("APELLIDOS:"), 0, 1);
        grid.add(txtLastName, 1, 1);

        grid.add(crearLabel("CORREO:"), 0, 2);
        grid.add(txtEmail, 1, 2);

        grid.add(crearLabel("PUESTO / ROL:"), 0, 3);
        grid.add(cmbPuesto, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == btnGuardar) {
            String newName = txtName.getText().trim();
            String newLastName = txtLastName.getText().trim();
            String newEmail = txtEmail.getText().trim();
            String nuevoPuesto = cmbPuesto.getValue();

            if (validate.validateTextEmpty(newName) || validate.validateTextEmpty(newLastName)
                    || validate.validateTextEmpty(newEmail)) {
                alertInfo.viewAlert("WARNING", "DATOS VACÍOS", "CAMPOS INCOMPLETOS", "Ningún campo puede quedar vacío.");
                return;
            }

            if (!validate.validateEmail(newEmail)) {
                alertInfo.viewAlert("WARNING", "CORREO INVÁLIDO", "ERROR DE CORREO", "Ingresa un correo electrónico válido.");
                return;
            }

            int idPuesto = switch (nuevoPuesto) {
                case "Dueño" -> 1;
                case "Gerente" -> 2;
                case "Encargado" -> 3;
                default -> 4;
            };

            boolean actualizado = empleadoService.editar(emp.getIdEmpleado(), newName, newLastName, newEmail, idPuesto);
            if (actualizado) {
                // Si el Dueño se editó a sí mismo, actualizar la sesión actual
                Empleado actual = Session.getEmpleadoActual();
                if (actual != null && actual.getIdEmpleado().equals(emp.getIdEmpleado())) {
                    actual.setNombres(newName);
                    actual.setApellidos(newLastName);
                    actual.setCorreo(newEmail);
                }

                cargarTabla();
                alertInfo.viewAlert("INFORMATION", "CAMBIOS GUARDADOS",
                        "EMPLEADO ACTUALIZADO",
                        "Los datos de \"" + newName + " " + newLastName + "\" han sido actualizados exitosamente.");
            } else {
                alertInfo.viewAlert("ERROR", "ERROR AL ACTUALIZAR",
                        "NO SE PUDIERON GUARDAR LOS CAMBIOS",
                        "Ocurrió un error al actualizar los datos en la base de datos.");
            }
        }
    }

    // =========================================================================
    // SUB-MODAL 2.2: REPORTAR INCIDENCIA DE EMPLEADO
    // =========================================================================
    private void abrirDialogoReportar(Empleado emp) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("REPORTAR EMPLEADO");
        dialog.setHeaderText("Registro de Incidencia / Falta Disciplinaria");
        aplicarEstiloDialogo(dialog);

        ButtonType btnReportar = new ButtonType("REGISTRAR REPORTE", ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("CANCELAR", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnReportar, btnCancelar);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(16, 20, 16, 20));

        Label lblSub = new Label("Colaborador: " + emp.getNombreCompleto() + " (" + emp.getNombrePuesto() + ")");
        lblSub.setStyle("-fx-text-fill: #FF6A13; -fx-font-weight: bold;");

        Label lblTipo = crearLabel("TIPO DE REPORTE / FALTA:");
        ComboBox<String> cmbTipo = new ComboBox<>(FXCollections.observableArrayList(
                "Llegada tardía / Inasistencia injustificada",
                "Incumplimiento de funciones o asignaciones",
                "Queja de cliente o mala atención en taquilla",
                "Falta disciplinaria / Conducta inapropiada",
                "Descuadre o inconsistencia en ventas",
                "Otro motivo administrativo"
        ));
        cmbTipo.getSelectionModel().selectFirst();
        cmbTipo.setMaxWidth(Double.MAX_VALUE);
        cmbTipo.getStyleClass().add("eva-field");

        Label lblDetalle = crearLabel("DESCRIPCIÓN DETALLADA DE LA INCIDENCIA:");
        TextArea txtDetalle = new TextArea();
        txtDetalle.setPromptText("Indica detalles de los hechos, fecha, hora u observaciones relevantes...");
        txtDetalle.setPrefRowCount(4);
        txtDetalle.setWrapText(true);
        txtDetalle.getStyleClass().add("eva-field");

        vbox.getChildren().addAll(lblSub, lblTipo, cmbTipo, lblDetalle, txtDetalle);
        dialog.getDialogPane().setContent(vbox);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == btnReportar) {
            String detalle = txtDetalle.getText().trim();
            String tipo = cmbTipo.getValue();

            if (detalle.isEmpty()) {
                alertInfo.viewAlert("WARNING", "DETALLE REQUERIDO",
                        "CAMPO VACÍO", "Debes ingresar una descripción detallada de la incidencia.");
                return;
            }

            Empleado reportador = Session.getEmpleadoActual();
            String idReportador = reportador != null ? reportador.getIdEmpleado() : emp.getIdEmpleado();

            boolean registrado = empleadoService.reportar(emp.getIdEmpleado(), idReportador, tipo, detalle);
            if (registrado) {
                alertInfo.viewAlert("INFORMATION", "REPORTE REGISTRADO",
                        "INCIDENCIA DOCUMENTADA",
                        "El reporte para \"" + emp.getNombreCompleto() + "\" ha sido guardado exitosamente en el sistema.");
            } else {
                alertInfo.viewAlert("ERROR", "ERROR AL REPORTAR",
                        "FALLO DE REGISTRO", "Ocurrió un error al guardar el reporte.");
            }
        }
    }

    // =========================================================================
    // SUB-MODAL 2.3: DAR DE BAJA
    // =========================================================================
    private void procederDarDeBaja(Empleado seleccionado) {
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
        confirmacion.setContentText("¿Seguro que deseas dar de baja a \"" + seleccionado.getNombreCompleto()
                + "\" (" + seleccionado.getNombrePuesto() + ")?");
        aplicarEstiloDialogo(confirmacion);

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            TextInputDialog dialogMotivo = new TextInputDialog();
            dialogMotivo.setTitle("MOTIVO DE LA BAJA");
            dialogMotivo.setHeaderText("Registro de Motivo de Baja");
            dialogMotivo.setContentText("Indica la razón o motivo de la baja:");
            aplicarEstiloDialogo(dialogMotivo);

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
    public void onBack(ActionEvent event) {
        new ViewFactory().viewMainMenu();
    }

    private Label crearLabel(String texto) {
        Label lbl = new Label(texto);
        lbl.getStyleClass().add("eva-label");
        return lbl;
    }

    private void aplicarEstiloDialogo(Dialog<?> dialog) {
        try {
            dialog.getDialogPane().getStylesheets().add(
                    getClass().getResource("/org/cinekinal/system/styles/ManageUsersStyles.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("dialog-eva");
        } catch (Exception ignored) {
        }
    }
}
