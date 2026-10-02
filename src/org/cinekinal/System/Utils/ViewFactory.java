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
                case "solicitudes", "requests" -> fxmlFile = "RequestsView.fxml";
                case "missolicitudes", "myrequests" -> fxmlFile = "MyRequestsView.fxml";
                case "mensajes", "messages" -> fxmlFile = "MessagesView.fxml";
                case "comprarboletos", "buytickets" -> fxmlFile = "BuyTicketView.fxml";
                case "verificarentrada", "verifyentry" -> fxmlFile = "VerifyEntryView.fxml";
                case "ventataquilla", "boxofficesale" -> fxmlFile = "BoxOfficeSaleView.fxml";
                case "administrarpeliculas", "managemovies" -> fxmlFile = "ManageMoviesView.fxml";
                case "administrarfuncionessalas", "manageshowtimestheaters" -> fxmlFile = "ManageShowtimesTheatersView.fxml";
                case "cambiarprecio", "changeprice" -> fxmlFile = "ChangePriceView.fxml";
                case "reportes", "reports" -> fxmlFile = "ReportsView.fxml";
                case "ganancias", "earnings" -> fxmlFile = "EarningsView.fxml";
                case "cortecaja", "cashclosing" -> fxmlFile = "CashClosingView.fxml";
                case "misboletos", "mytickets" -> fxmlFile = "MyTicketsView.fxml";
                default -> { fxmlFile = "LoginView.fxml"; width = 820; height = 500; }
            }

            Parent root = loadRootFXML(fxmlFile);
            SceneManager.getInstanciaSceneManager().changeRoot(root, width, height, !esDialogo);
        } catch (RuntimeException e) {
            System.out.println("Error al cargar la vista '" + nameFile + "': " + e.getMessage());
            e.printStackTrace();
            new AlertInformation().viewAlert("ERROR", "NO SE PUDO ABRIR LA VISTA",
                    "Error al cargar \"" + nameFile + "\"",
                    String.valueOf(e.getCause() != null ? e.getCause() : e));
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

    public void viewRequests(){
        loadScene("requests");
    }

    public void viewMyRequests(){
        loadScene("myrequests");
    }

    public void viewMessages(){
        loadScene("messages");
    }

    public void viewBuyTickets(){
        loadScene("buytickets");
    }

    public void viewVerifyEntry(){
        loadScene("verifyentry");
    }

    public void viewBoxOfficeSale(){
        loadScene("boxofficesale");
    }

    public void viewManageMovies(){
        loadScene("managemovies");
    }

    public void viewManageShowtimesTheaters(){
        loadScene("manageshowtimestheaters");
    }

    public void viewChangePrice(){
        loadScene("changeprice");
    }

    public void viewReports(){
        loadScene("reports");
    }

    public void viewEarnings(){
        loadScene("earnings");
    }

    public void viewCashClosing(){
        loadScene("cashclosing");
    }

    public void viewMyTickets(){
        loadScene("mytickets");
    }

    // Spanish compatibility aliases
    public void viewSolicitudes() { viewRequests(); }
    public void viewMisSolicitudes() { viewMyRequests(); }
    public void viewMensajes() { viewMessages(); }
    public void viewComprarBoletos() { viewBuyTickets(); }
    public void viewVerificarEntrada() { viewVerifyEntry(); }
    public void viewRegistrarVenta() { viewBoxOfficeSale(); }
    public void viewAdministrarPeliculas() { viewManageMovies(); }
    public void viewAdministrarFuncionesSalas() { viewManageShowtimesTheaters(); }
    public void viewCambiarPrecio() { viewChangePrice(); }
    public void viewReportes() { viewReports(); }
    public void viewGanancias() { viewEarnings(); }
    public void viewCorteCaja() { viewCashClosing(); }
    public void viewMisBoletos() { viewMyTickets(); }
}
