package org.cinekinal.system.utils;

import java.net.URL;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;

public class AlertInformation {

    private static final String ALERT_CSS = "/org/cinekinal/system/styles/AlertStyles.css";

    public void viewAlert(String tipoAlerta, String titulo, String cabecera, String mensaje) {
        AlertType tipo = switch (tipoAlerta.toUpperCase()) {
            case "ERROR" -> AlertType.ERROR;
            case "WARNING" -> AlertType.WARNING;
            default -> AlertType.INFORMATION;
        };

        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecera);
        alert.setContentText(mensaje);

        DialogPane dialogPane = alert.getDialogPane();
        URL css = getClass().getResource(ALERT_CSS);
        if (css != null) {
            dialogPane.getStylesheets().add(css.toExternalForm());
        }
        dialogPane.getStyleClass().add(switch (tipoAlerta.toUpperCase()) {
            case "ERROR" -> "kinal-alert-error";
            case "WARNING" -> "kinal-alert-warning";
            default -> "kinal-alert-info";
        });

        alert.showAndWait();
    }
}
