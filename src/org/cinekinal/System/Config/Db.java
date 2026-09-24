package org.cinekinal.system.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.cinekinal.system.config.ConexionDB;

/**
 * Punto unico de acceso JDBC. Los repositorios solo dicen QUE ejecutar
 * (un "{call sp(?,?)}" o un SQL normal) y COMO convertir cada fila; el
 * try/catch, el bind de parametros y el cierre de recursos viven aqui.
 * Si el SQL falla lanza RuntimeException con la SQLException como causa
 * (ClienteService depende de eso para detectar llaves duplicadas).
 */
public final class Db {

    @FunctionalInterface
    public interface Mapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private Db() {
    }

    /** Todas las filas del resultado, convertidas con el mapper. */
    public static <T> List<T> list(String sql, Mapper<T> mapper, Object... params) {
        try (PreparedStatement ps = preparar(sql, params); ResultSet rs = ps.executeQuery()) {
            List<T> filas = new ArrayList<>();
            while (rs.next()) {
                filas.add(mapper.map(rs));
            }
            return filas;
        } catch (SQLException e) {
            throw error(sql, e);
        }
    }

    /** Solo la primera fila, o null si no hay resultados. */
    public static <T> T one(String sql, Mapper<T> mapper, Object... params) {
        try (PreparedStatement ps = preparar(sql, params); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? mapper.map(rs) : null;
        } catch (SQLException e) {
            throw error(sql, e);
        }
    }

    /** INSERT / UPDATE / DDL / SP sin resultado. Devuelve las filas afectadas. */
    public static int update(String sql, Object... params) {
        try (PreparedStatement ps = preparar(sql, params)) {
            ps.execute();
            return Math.max(ps.getUpdateCount(), 0);
        } catch (SQLException e) {
            throw error(sql, e);
        }
    }

    private static PreparedStatement preparar(String sql, Object[] params) throws SQLException {
        Connection conexion = ConexionDB.getInstanciaConexionDB().getConnection();
        PreparedStatement ps = sql.startsWith("{") ? conexion.prepareCall(sql) : conexion.prepareStatement(sql);
        try {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
        } catch (SQLException e) {
            ps.close();
            throw e;
        }
        return ps;
    }

    private static RuntimeException error(String sql, SQLException e) {
        System.out.println("Error SQL en [" + sql + "]: " + e.getMessage());
        return new RuntimeException(e);
    }
}
