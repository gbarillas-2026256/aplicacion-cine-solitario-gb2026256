package org.cinekinal.system.utils;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class SceneManager {
    private static SceneManager instance;
    private Stage mainStage;

    private SceneManager() {
    }

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public static SceneManager getInstanciaSceneManager() {
        return getInstance();
    }

    public void changeRoot(Parent root, int width, int height, boolean maximize) {
        try {
            if (mainStage == null) {
                return;
            }
            if (root instanceof Region) {
                ((Region) root).setMaxWidth(Double.MAX_VALUE);
                ((Region) root).setMaxHeight(Double.MAX_VALUE);
            }
            Scene currentScene = mainStage.getScene();
            if (currentScene == null) {
                if (width > 0 && height > 0) {
                    currentScene = new Scene(root, width, height);
                } else {
                    currentScene = new Scene(root);
                }
                mainStage.setScene(currentScene);
            } else {
                currentScene.setRoot(root);
            }

            if (maximize) {
                if (!mainStage.isMaximized()) {
                    mainStage.setMaximized(true);
                }
                Platform.runLater(() -> {
                    if (!mainStage.isMaximized()) {
                        mainStage.setMaximized(true);
                    }
                });
            } else {
                mainStage.setMaximized(false);
                mainStage.sizeToScene();
                mainStage.centerOnScreen();
            }
            mainStage.show();
        } catch (Exception e) {
            System.out.println("Error al cambiar de vista: " + e.getMessage());
        }
    }

    public void changeScene(Scene scene) {
        changeScene(scene, true);
    }

    public void changeScene(Scene scene, boolean maximize) {
        if (scene != null) {
            changeRoot(scene.getRoot(), 0, 0, maximize);
        }
    }

    public Stage getMainStage() {
        return mainStage;
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    public Stage getStagePrincipal() {
        return getMainStage();
    }

    public void setStagePrincipal(Stage stagePrincipal) {
        setMainStage(stagePrincipal);
    }
}
