/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package org.cinekinal.system.repository;

import java.util.List;
import org.cinekinal.system.model.User;

public interface UserInterface {
    void create(User user);
    User login(String user, String password);
    List<User> getAll();
    void delete(String idUser);
}
