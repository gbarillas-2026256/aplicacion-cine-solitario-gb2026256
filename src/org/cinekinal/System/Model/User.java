/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.cinekinal.system.model;

/**
 *
 * @author STEPHRYS
 */
public class User {
    private String name;
    private String lastname;
    private String email;
    private String password;
    private String user;
    private String idUser;

    //Constructores
    public User(){
        
    }

    public User(String name, String lastname, String email, String password, String user, String idUser) {
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.user = user;
        this.idUser = idUser;
    }

    public User(String name, String lastname, String email, String user) {
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.user = user;
    }
    
    
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }
    
    
    
}
