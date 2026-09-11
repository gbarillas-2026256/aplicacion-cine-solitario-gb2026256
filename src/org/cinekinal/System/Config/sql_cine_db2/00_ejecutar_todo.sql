-- ============================================================
-- 00_ejecutar_todo.sql
--
-- Este es el UNICO archivo que necesitas correr manualmente.
-- Manda a llamar a todos los demas, en el orden correcto, usando
-- SOURCE. Es seguro volver a correrlo cuantas veces quieras: el
-- primer archivo borra y recrea la base de datos desde cero.
--
-- Como correrlo:
--   - MySQL Workbench: abre este archivo y dale "Execute" (rayo).
--   - Consola mysql:   mysql -u root -p < 00_ejecutar_todo.sql
--                      (o, ya dentro de la consola: SOURCE 00_ejecutar_todo.sql;)
--
-- IMPORTANTE: los 8 archivos deben estar en la misma carpeta.
-- ============================================================

SOURCE 01_tablas.sql;
SOURCE 02_datos_iniciales.sql;
SOURCE 03_procedimientos_empleados.sql;
SOURCE 04_procedimientos_clientes.sql;
SOURCE 05_procedimientos_peliculas_salas.sql;
SOURCE 06_procedimientos_funciones_boletos.sql;
SOURCE 07_procedimientos_reportes.sql;

-- Verificacion rapida: debe devolver 27 filas (los 27 procedimientos)
use cine_db_gb2026256_in4av;
select routine_name from information_schema.routines
    where routine_schema = 'cine_db_gb2026256_in4av';
