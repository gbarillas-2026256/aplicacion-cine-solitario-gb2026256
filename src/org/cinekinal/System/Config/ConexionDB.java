package org.cinekinal.system.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static ConexionDB instanciaConexionDB;
    private Connection connection;

    private ConexionDB() {
        conectar();
    }
    private void conectar() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + Enviroment.LOCATION_SERVICE + "/" + Enviroment.DATA_BASE,
                    Enviroment.USER,
                    Enviroment.PASSWORD);
        } catch (ClassNotFoundException classNotFound) {
            throw new RuntimeException(
                "No se encontro el driver de MySQL (mysql-connector-j). "
                + "Revisa que el .jar este agregado a las librerias del proyecto.", classNotFound);
        } catch (SQLException sqlException) {
            throw new RuntimeException(
                "No se pudo conectar a la base de datos '" + Enviroment.DATA_BASE
                + "' en " + Enviroment.LOCATION_SERVICE + " con el usuario '" + Enviroment.USER + "'. "
                + "Verifica que MySQL este corriendo y que hayas ejecutado ddl_cine.sql completo. "
                + "Detalle: " + sqlException.getMessage(), sqlException);
        }
    }

    public static ConexionDB getInstanciaConexionDB() {
        if (instanciaConexionDB == null) {
            instanciaConexionDB = new ConexionDB();
        }
        return instanciaConexionDB;
    }


    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                conectar();
            }
        } catch (SQLException e) {
            conectar();
        }
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
