
package org.cinekinal.system.service;

import java.util.Collections;
import java.util.List;
import org.cinekinal.system.repository.UserRepository;
import org.cinekinal.system.model.User;
import org.cinekinal.system.model.UserStatus;

public class UserService {
    private UserRepository userRepo = new UserRepository();

    public UserStatus createUser(String user, String name, String lastName, String email, String password){
        try {
            User newUser = new User(name, lastName, email, password, user, null);
            userRepo.create(newUser);
            return UserStatus.USER_CREATED;
        }catch (Exception e){
            return UserStatus.ERROR_USER_CREATE;
        }
    }

    //Devuelve el usuario si las credenciales existen en la base de datos, o null si no
    public User login(String user, String password){
        try {
            return userRepo.login(user, password);
        } catch (Exception e) {
            return null;
        }
    }

    //Lista de usuarios para la pantalla de Administrar Usuarios
    public List<User> getAllUsers(){
        try {
            return userRepo.getAll();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    //true si se pudo eliminar, false si ocurrio un error
    public boolean deleteUser(String idUser){
        try {
            userRepo.delete(idUser);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
