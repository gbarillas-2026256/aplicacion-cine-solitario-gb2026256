package org.cinekinal.system.utils;

/**
 * Validaciones genericas de formulario, reutilizables en cualquier
 * pantalla de registro/edicion (Cliente, Empleado, etc.).
 */
public class Validations {

    public Validations() {
    }

    public Boolean validateTextEmpty(String text) {
        return text == null || text.isEmpty() || text.isBlank();
    }

    public Boolean validateTextLength(String text, int longitudMaxima) {
        return text.length() <= longitudMaxima;
    }

    public Boolean equalsText(String textoOriginal, String textoComparar) {
        return textoOriginal.equals(textoComparar);
    }

    public Boolean validateEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        //debe existir exactamente un @, y no puede estar al inicio ni al final
        int cantidadArrobas = 0;
        int indiceArroba = -1;
        for (int index = 0; index < email.length(); index++) {
            if (email.charAt(index) == '@') {
                cantidadArrobas++;
                indiceArroba = index;
            }
        }
        if (cantidadArrobas != 1) {
            return false;
        }
        if (indiceArroba == 0 || indiceArroba == email.length() - 1) {
            return false;
        }

        //el dominio (despues del @) debe tener al menos un punto, sin
        //estar pegado al @ ni al final
        String dominio = email.substring(indiceArroba + 1);
        if (dominio.startsWith(".") || dominio.endsWith(".")) {
            return false;
        }

        int cantidadPuntos = 0;
        for (int index = 0; index < dominio.length(); index++) {
            if (dominio.charAt(index) == '.') {
                cantidadPuntos++;
            }
        }
        return cantidadPuntos >= 1;
    }
}
