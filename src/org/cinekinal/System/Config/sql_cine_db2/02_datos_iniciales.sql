-- ============================================================
-- 02_datos_iniciales.sql
-- Datos que la aplicacion necesita desde el primer arranque
-- (no son datos de prueba, son catalogo fijo del sistema).
-- Requiere haber corrido 01_tablas.sql antes.
-- ============================================================

use cine_db_gb2026256_in4av;

insert into Puestos (nombre_puesto, nivel_jerarquico) values
    ('Dueño', 1),
    ('Gerente', 2),
    ('Encargado', 3),
    ('Empleado', 4);
