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
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.UncheckedIOException;
public class ViewFactory {
    private final String PATH_VIEWS="/org/cinekinal/system/view/";
    
    public Parent loadRootFXML(String nameFile){
        String pathOfFile = PATH_VIEWS + nameFile;
        try {
            FXMLLoader loadFXML = new FXMLLoader();
            URL urlFile = ClasePrincipal.class.getResource(pathOfFile);
            if (urlFile == null) {
                throw new IllegalStateException(
                    "No se encontro la vista '" + pathOfFile + "'. "
                    + "Revisa que el archivo .fxml exista en esa ruta exacta dentro de src.");
            }
            loadFXML.setBuilderFactory(new JavaFXBuilderFactory() );
            loadFXML.setLocation(urlFile);
            return loadFXML.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Scene loadFileFXML(String nameFile, int width, int height){
        Parent root = loadRootFXML(nameFile);
        if (width > 0 && height > 0) {
            return new Scene(root, width, height);
        }
        return new Scene(root);
    }
            
    public void loadScene(String nameFile){
        try {
            String fxmlFile;
            int width = 0;
            int height = 0;
            boolean esDialogo = "login".equals(nameFile) || "register".equals(nameFile);

            switch (nameFile) {
                case "login" -> { fxmlFile = "LoginView.fxml"; width = 820; height = 500; }
                case "register" -> { fxmlFile = "RegisterView.fxml"; width = 900; height = 560; }
                case "mainmenu" -> fxmlFile = "MainMenuView.fxml";
                case "users" -> fxmlFile = "ManageUsersView.fxml";
                case "solicitudes" -> fxmlFile = "SolicitudesView.fxml";
                case "comprarboletos" -> fxmlFile = "CompraBoletoView.fxml";
                case "verificarentrada" -> fxmlFile = "VerificarEntradaView.fxml";
                case "ventataquilla" -> fxmlFile = "VentaTaquillaView.fxml";
                case "administrarpeliculas" -> fxmlFile = "AdministrarPeliculasView.fxml";
                case "administrarfuncionessalas" -> fxmlFile = "AdministrarFuncionesSalasView.fxml";
                case "cambiarprecio" -> fxmlFile = "CambiarPrecioView.fxml";
                case "reportes" -> fxmlFile = "ReportesView.fxml";
                case "ganancias" -> fxmlFile = "GananciasView.fxml";
                case "reportesganancias" -> fxmlFile = "ReportesGananciasView.fxml";
                default -> { fxmlFile = "LoginView.fxml"; width = 820; height = 500; }
            }

            Parent root = loadRootFXML(fxmlFile);
            SceneManager.getInstanciaSceneManager().changeRoot(root, width, height, !esDialogo);
        } catch (RuntimeException e) {
            System.out.println("Error al cargar la vista '" + nameFile + "': " + e.getMessage());
        }
    }
    
    
    public void viewLogin(){
        if (SceneManager.getInstanciaSceneManager().getStagePrincipal() != null) {
            SceneManager.getInstanciaSceneManager().getStagePrincipal().setMaximized(false);
            SceneManager.getInstanciaSceneManager().getStagePrincipal().setFullScreen(false);
        }
        loadScene("login");
    }
    
    public void viewRegister(){
        if (SceneManager.getInstanciaSceneManager().getStagePrincipal() != null) {
            SceneManager.getInstanciaSceneManager().getStagePrincipal().setMaximized(false);
            SceneManager.getInstanciaSceneManager().getStagePrincipal().setFullScreen(false);
        }
        loadScene("register");
    }
    
    public void viewMainMenu(){
        loadScene("mainmenu");
        if (SceneManager.getInstanciaSceneManager().getStagePrincipal() != null) {
            SceneManager.getInstanciaSceneManager().getStagePrincipal().setMaximized(true);
        }
    }
    
    public void viewManageUsers(){
        loadScene("users");
    }

    public void viewSolicitudes(){
        loadScene("solicitudes");
    }

    public void viewComprarBoletos(){
        loadScene("comprarboletos");
    }

    public void viewVerificarEntrada(){
        loadScene("verificarentrada");
    }

    public void viewRegistrarVenta(){
        loadScene("ventataquilla");
    }

    public void viewAdministrarPeliculas(){
        loadScene("administrarpeliculas");
    }

    public void viewAdministrarFuncionesSalas(){
        loadScene("administrarfuncionessalas");
    }

    public void viewCambiarPrecio(){
        loadScene("cambiarprecio");
    }

    public void viewReportes(){
        loadScene("reportes");
    }

    public void viewGanancias(){
        loadScene("ganancias");
    }

    public void viewReportesGanancias(){
        loadScene("reportesganancias");
    }
}
