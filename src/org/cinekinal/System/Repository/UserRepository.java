/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.cinekinal.system.repository;

import org.cinekinal.system.model.User;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.cinekinal.system.config.ConexionDB;
import java.sql.SQLException;

public class UserRepository implements UserInterface{
    private ConexionDB conexionDB = ConexionDB.getInstanciaConexionDB();

    @Override
    public void create(User user){
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_create_users(?,?,?,?,?)}")) {
            callSP.setString(1, user.getName());
            callSP.setString(2, user.getLastname());
            callSP.setString(3, user.getEmail());
            callSP.setString(4, user.getUser());
            callSP.setString(5, user.getPassword());

            callSP.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al crear usuario en la base de datos: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public User login(String user, String password){
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_login(?,?)}")) {
            callSP.setString(1, user);
            callSP.setString(2, password);

            try (ResultSet resultado = callSP.executeQuery()) {
                if (resultado.next()) {
                    return new User(
                            resultado.getString("name"),
                            resultado.getString("lastname"),
                            resultado.getString("email"),
                            resultado.getString("password"),
                            resultado.getString("user"),
                            resultado.getString("id_user")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al validar el login en la base de datos: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<User> getAll(){
        List<User> usuarios = new ArrayList<>();
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_get_users()}");
             ResultSet resultado = callSP.executeQuery()) {

            while (resultado.next()) {
                User user = new User();
                user.setIdUser(resultado.getString("id_user"));
                user.setName(resultado.getString("name"));
                user.setLastname(resultado.getString("lastname"));
                user.setEmail(resultado.getString("email"));
                user.setUser(resultado.getString("user"));
                usuarios.add(user);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar usuarios: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return usuarios;
    }

    @Override
    public void delete(String idUser){
        try (CallableStatement callSP = conexionDB.getConnection()
                     .prepareCall("{call sp_delete_user(?)}")) {
            callSP.setString(1, idUser);
            callSP.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al eliminar usuario: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
