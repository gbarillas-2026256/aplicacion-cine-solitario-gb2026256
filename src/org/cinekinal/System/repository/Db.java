package org.cinekinal.system.repository;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.cinekinal.system.config.ConexionDB;

/**
 * Helper centralizado para simplificar el acceso a datos JDBC en repositorios.
 */
public final class Db {

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private Db() {}

    private static Connection getConnection() {
        return ConexionDB.getInstanciaConexionDB().getConnection();
    }

    private static PreparedStatement prepare(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement ps = sql.trim().startsWith("{")
                ? conn.prepareCall(sql)
                : conn.prepareStatement(sql);
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
        }
        return ps;
    }

    public static <T> List<T> list(String sql, RowMapper<T> mapper, Object... params) {
        List<T> result = new ArrayList<>();
        try (PreparedStatement ps = prepare(getConnection(), sql, params);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapper.map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static <T> T one(String sql, RowMapper<T> mapper, Object... params) {
        try (PreparedStatement ps = prepare(getConnection(), sql, params);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapper.map(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int update(String sql, Object... params) {
        try (PreparedStatement ps = prepare(getConnection(), sql, params)) {
            boolean hasRs = ps.execute();
            int count = ps.getUpdateCount();
            return count >= 0 ? count : (hasRs ? 1 : 0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
