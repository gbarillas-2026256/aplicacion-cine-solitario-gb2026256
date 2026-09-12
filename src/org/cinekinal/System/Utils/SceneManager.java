package org.cinekinal.system.utils;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class SceneManager {
    private static SceneManager instanciaSceneManager;
    private Stage stagePrincipal;
            
    private SceneManager(){ }
    
    public static SceneManager getInstanciaSceneManager(){
        if( instanciaSceneManager == null  )
            instanciaSceneManager = new SceneManager();
        return instanciaSceneManager;
    }

    public void changeRoot(Parent root, int width, int height, boolean maximizar) {
        try {
            if (stagePrincipal == null) {
                return;
            }
            if (root instanceof Region) {
                ((Region) root).setMaxWidth(Double.MAX_VALUE);
                ((Region) root).setMaxHeight(Double.MAX_VALUE);
            }
            Scene currentScene = stagePrincipal.getScene();
            if (currentScene == null) {
                if (width > 0 && height > 0) {
                    currentScene = new Scene(root, width, height);
                } else {
                    currentScene = new Scene(root);
                }
                stagePrincipal.setScene(currentScene);
            } else {
                currentScene.setRoot(root);
            }

            if (maximizar) {
                if (!stagePrincipal.isMaximized()) {
                    stagePrincipal.setMaximized(true);
                }
                Platform.runLater(() -> {
                    if (!stagePrincipal.isMaximized()) {
                        stagePrincipal.setMaximized(true);
                    }
                });
            } else {
                stagePrincipal.setMaximized(false);
                stagePrincipal.sizeToScene();
                stagePrincipal.centerOnScreen();
            }
            stagePrincipal.show();
        } catch (Exception e) {
            System.out.println("Error al cambiar de vista: " + e.getMessage());
        }
    }

    public void changeScene(Scene scene){
        changeScene(scene, true);
    }

    public void changeScene(Scene scene, boolean maximizar){
        if (scene != null) {
            changeRoot(scene.getRoot(), 0, 0, maximizar);
        }
    }
    
    public Stage getStagePrincipal() {
        return stagePrincipal;
    }

    public void setStagePrincipal(Stage stagePrincipal) {
        this.stagePrincipal = stagePrincipal;
    }
}
