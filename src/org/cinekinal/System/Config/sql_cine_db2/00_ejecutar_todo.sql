-- ============================================================
-- 00_ejecutar_todo.sql
--
-- ATENCIÓN (¿Por qué SOURCE da error en MySQL Workbench?):
-- "SOURCE" NO es una instrucción SQL estándar, sino un comando
-- interno del cliente de consola (mysql.exe). Por eso MySQL Workbench
-- marca la palabra SOURCE con una X roja de "error de sintaxis".
--
-- CÓMO EJECUTAR EN MYSQL WORKBENCH:
--   1. En vez de este archivo, abre: "cine_db_completo.sql"
--      (contiene todo el código unificado en un solo archivo).
--   2. Presiona el botón del RAYO (Execute).
--
-- CÓMO EJECUTAR EN CONSOLA / TERMINAL:
--   mysql -u root -p < cine_db_completo.sql
--   (o si estás dentro de la consola: SOURCE 00_ejecutar_todo.sql;)
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
