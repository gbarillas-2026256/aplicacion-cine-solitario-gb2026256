package org.cinekinal.system;

import javafx.application.Application;
import javafx.stage.Stage;
import org.cinekinal.system.utils.SceneManager;
import org.cinekinal.system.utils.ViewFactory;

public class ClasePrincipal extends Application {

    @Override
    public void start(Stage stagePrincipal) {
        stagePrincipal.setTitle("Sistema de Administración Cinematográfica");
        SceneManager.getInstanciaSceneManager().setStagePrincipal(stagePrincipal);

        ViewFactory viewFactory = new ViewFactory();
        viewFactory.viewLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
