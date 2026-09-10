/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.cinekinal.system.utils;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import java.net.URL;
import org.cinekinal.system.ClasePrincipal;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import java.io.UncheckedIOException;
public class ViewFactory {
    private final String PATH_VIEWS="/org/cinekinal/system/view/";
    
    public Scene loadFileFXML(String nameFile, int width, int height){
        String pathOfFile = PATH_VIEWS + nameFile;
        try {
            //Llamar al FXMLLoader
            FXMLLoader loadFXML = new FXMLLoader();
            //Obtener la URL del archivo, viene de la clase main
            URL urlFile = ClasePrincipal.class.getResource(pathOfFile);
            if (urlFile == null) {
                throw new IllegalStateException(
                    "No se encontro la vista '" + pathOfFile + "'. "
                    + "Revisa que el archivo .fxml exista en esa ruta exacta dentro de src.");
            }
            loadFXML.setBuilderFactory(new JavaFXBuilderFactory() );
            loadFXML.setLocation(urlFile);
            
            return new Scene( loadFXML.load(), width, height  );
            
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
            
    public void loadScene(String nameFile){
        Scene scene = null;
        try {
            switch (nameFile) {
                case "login" -> scene = loadFileFXML("LoginView.fxml",820,500);
                case "register" -> scene = loadFileFXML("RegisterView.fxml", 900, 560);
                case "mainmenu" -> scene = loadFileFXML("MainMenuView.fxml", 860, 560);
                case "users" -> scene = loadFileFXML("ManageUsersView.fxml", 900, 600);
                default      -> scene = loadFileFXML("LoginView.fxml",0,0); 
            }
            SceneManager.getInstanciaSceneManager().changeScene(scene);
        } catch (RuntimeException e) {
            System.out.println("Error al cargar la vista '" + nameFile + "': " + e.getMessage());
        }
    }
    
    
    public void viewLogin(){
        loadScene("login");
    }
    
    public void viewRegister(){
        loadScene("register");
    }
    
    public void viewMainMenu(){
        loadScene("mainmenu");
    }
    
    public void viewManageUsers(){
        loadScene("users");
    }
    
    
    
    
}
















