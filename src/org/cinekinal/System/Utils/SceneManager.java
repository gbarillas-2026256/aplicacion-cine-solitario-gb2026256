package org.cinekinal.system.utils;

import javafx.stage.Stage;
import javafx.scene.Scene;
public class SceneManager {
    private static SceneManager instanciaSceneManager;
    private Stage stagePrincipal;
            
    private SceneManager(){ }
    
    public static SceneManager getInstanciaSceneManager(){
        if( instanciaSceneManager == null  )
            instanciaSceneManager = new SceneManager();
        return instanciaSceneManager;
    }

    public void changeScene(Scene scene){
        try {
            stagePrincipal.setScene(scene);
            stagePrincipal.sizeToScene();
            stagePrincipal.show();
        } catch (NullPointerException objetoNulo) {
            //Alert
        }
    }
    
    public Stage getStagePrincipal() {
        return stagePrincipal;
    }

    public void setStagePrincipal(Stage stagePrincipal) {
        this.stagePrincipal = stagePrincipal;
    }
}
