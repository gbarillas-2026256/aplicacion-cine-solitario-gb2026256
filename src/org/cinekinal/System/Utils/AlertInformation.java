package org.cinekinal.system.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AlertInformation {

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
        alert.showAndWait();
    }
}
