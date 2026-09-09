package org.cinekinal.system.utils;

/**
 * Guarda el nombre del operador que inicio sesion, para poder
 * mostrarlo en el Dashboard sin tener que pasar el objeto User
 * de una vista a otra manualmente.
 */
public class Session {
    private static String usuarioActual = "ADMIN";

    private Session() {
    }

    public static String getUsuarioActual() {
        return usuarioActual;
    }

    public static void setUsuarioActual(String usuario) {
        usuarioActual = usuario;
    }
}
