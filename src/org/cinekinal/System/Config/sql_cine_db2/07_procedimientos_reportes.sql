-- ============================================================
-- 07_procedimientos_reportes.sql
-- Reportes de ingresos y ocupacion para Dueño/Gerente/Encargado.
-- Requiere haber corrido 01_tablas.sql antes.
-- ============================================================

use cine_db_gb2026256_in4av;

-- ---------- INGRESOS ----------

-- Ingresos totales agrupados por dia, en un rango de fechas.
-- Si fecha_inicio_p o fecha_fin_p vienen NULL, no filtra ese extremo
-- (ej: pasando ambos NULL te da el historico completo).
drop procedure if exists sp_reporte_ingresos_por_dia;
Delimiter $$
create procedure sp_reporte_ingresos_por_dia(in fecha_inicio_p date, in fecha_fin_p date)
begin
    select f.fecha,
           count(b.id_boleto) as boletos_vendidos,
           sum(b.precio_final) as ingresos
        from Boletos b
        inner join Funciones f on f.id_funcion = b.id_funcion
        where (fecha_inicio_p is null or f.fecha >= fecha_inicio_p)
          and (fecha_fin_p is null or f.fecha <= fecha_fin_p)
        group by f.fecha
        order by f.fecha;
end$$
Delimiter ;

-- Ingresos totales agrupados por pelicula, en un rango de fechas
-- (mismo criterio de NULL que el procedimiento anterior).
drop procedure if exists sp_reporte_ingresos_por_pelicula;
Delimiter $$
create procedure sp_reporte_ingresos_por_pelicula(in fecha_inicio_p date, in fecha_fin_p date)
begin
    select p.titulo,
           count(b.id_boleto) as boletos_vendidos,
           sum(b.precio_final) as ingresos
        from Boletos b
        inner join Funciones f on f.id_funcion = b.id_funcion
        inner join Peliculas p on p.id_pelicula = f.id_pelicula
        where (fecha_inicio_p is null or f.fecha >= fecha_inicio_p)
          and (fecha_fin_p is null or f.fecha <= fecha_fin_p)
        group by p.id_pelicula, p.titulo
        order by ingresos desc;
end$$
Delimiter ;

-- Resumen rapido del dia de hoy: para un dashboard que el Dueño o
-- Gerente ve al entrar a la app.
drop procedure if exists sp_reporte_resumen_hoy;
Delimiter $$
create procedure sp_reporte_resumen_hoy()
begin
    select count(b.id_boleto) as boletos_vendidos_hoy,
           coalesce(sum(b.precio_final), 0) as ingresos_hoy
        from Boletos b
        where date(b.fecha_compra) = curdate();
end$$
Delimiter ;

-- Top N peliculas por ingresos, en un rango de fechas.
drop procedure if exists sp_reporte_top_peliculas;
Delimiter $$
create procedure sp_reporte_top_peliculas(in fecha_inicio_p date, in fecha_fin_p date, in limite_p int)
begin
    select p.titulo,
           count(b.id_boleto) as boletos_vendidos,
           sum(b.precio_final) as ingresos
        from Boletos b
        inner join Funciones f on f.id_funcion = b.id_funcion
        inner join Peliculas p on p.id_pelicula = f.id_pelicula
        where (fecha_inicio_p is null or f.fecha >= fecha_inicio_p)
          and (fecha_fin_p is null or f.fecha <= fecha_fin_p)
        group by p.id_pelicula, p.titulo
        order by ingresos desc
        limit limite_p;
end$$
Delimiter ;

-- ---------- OCUPACION ----------

-- Cuantos asientos se vendieron vs la capacidad total de la sala,
-- para una funcion especifica (util para decidir si mover a sala mas grande/chica).
drop procedure if exists sp_reporte_ocupacion_funcion;
Delimiter $$
create procedure sp_reporte_ocupacion_funcion(in id_funcion_p varchar(36))
begin
    select p.titulo, s.nombre_sala, f.fecha, f.hora,
           (s.filas * s.columnas) as capacidad_total,
           count(b.id_boleto) as asientos_vendidos,
           round(count(b.id_boleto) / (s.filas * s.columnas) * 100, 1) as porcentaje_ocupacion
        from Funciones f
        inner join Peliculas p on p.id_pelicula = f.id_pelicula
        inner join Salas s on s.id_sala = f.id_sala
        left join Boletos b on b.id_funcion = f.id_funcion
        where f.id_funcion = id_funcion_p
        group by f.id_funcion, p.titulo, s.nombre_sala, f.fecha, f.hora, s.filas, s.columnas;
end$$
Delimiter ;

-- Ocupacion de todas las funciones futuras, para ver de un vistazo
-- cuales funciones se estan llenando y cuales no.
drop procedure if exists sp_reporte_ocupacion_cartelera;
Delimiter $$
create procedure sp_reporte_ocupacion_cartelera()
begin
    select f.id_funcion, p.titulo, s.nombre_sala, f.fecha, f.hora,
           (s.filas * s.columnas) as capacidad_total,
           count(b.id_boleto) as asientos_vendidos,
           round(count(b.id_boleto) / (s.filas * s.columnas) * 100, 1) as porcentaje_ocupacion
        from Funciones f
        inner join Peliculas p on p.id_pelicula = f.id_pelicula
        inner join Salas s on s.id_sala = f.id_sala
        left join Boletos b on b.id_funcion = f.id_funcion
        where f.fecha >= curdate()
        group by f.id_funcion, p.titulo, s.nombre_sala, f.fecha, f.hora, s.filas, s.columnas
        order by f.fecha, f.hora;
end$$
Delimiter ;

-- ============================================================
-- PROCEDIMIENTOS DE CORTE DE CAJA Y DULCERÍA
-- ============================================================

drop procedure if exists sp_corte_entradas_del_dia;
Delimiter $$
create procedure sp_corte_entradas_del_dia(in fecha_p date)
begin
    select count(*) as boletos_vendidos,
           coalesce(sum(b.precio_final), 0) as total_entradas
        from Boletos b
        where date(b.fecha_compra) = fecha_p;
end$$
Delimiter ;

drop procedure if exists sp_corte_entradas_por_pelicula;
Delimiter $$
create procedure sp_corte_entradas_por_pelicula(in fecha_p date)
begin
    select p.titulo,
           count(*) as boletos_vendidos,
           coalesce(sum(b.precio_final), 0) as total
        from Boletos b
        inner join Funciones f on f.id_funcion = b.id_funcion
        inner join Peliculas p on p.id_pelicula = f.id_pelicula
        where date(b.fecha_compra) = fecha_p
        group by p.id_pelicula, p.titulo
        order by total desc;
end$$
Delimiter ;

drop procedure if exists sp_corte_crear;
Delimiter $$
create procedure sp_corte_crear(in id_empleado_p varchar(36), in fecha_corte_p date,
                                 in total_entradas_p decimal(10,2), in boletos_vendidos_p int,
                                 in observaciones_p varchar(300))
begin
    declare nuevo_id varchar(36);
    set nuevo_id = uuid();

    insert into CortesCaja(id_corte, id_empleado, fecha_corte, total_entradas,
                           boletos_vendidos, total_dulceria, total_general, observaciones)
        values(nuevo_id, id_empleado_p, fecha_corte_p, total_entradas_p,
               boletos_vendidos_p, 0, total_entradas_p, observaciones_p);

    select nuevo_id as id_corte;
end$$
Delimiter ;

drop procedure if exists sp_corte_agregar_detalle;
Delimiter $$
create procedure sp_corte_agregar_detalle(in id_corte_p varchar(36), in categoria_p varchar(30),
                                           in descripcion_p varchar(120), in cantidad_p int,
                                           in precio_unitario_p decimal(8,2))
begin
    insert into CorteDetalles(id_detalle, id_corte, categoria, descripcion,
                              cantidad, precio_unitario, subtotal)
        values(uuid(), id_corte_p, categoria_p, descripcion_p, cantidad_p,
               precio_unitario_p, cantidad_p * precio_unitario_p);

    update CortesCaja c
        set c.total_dulceria = (select coalesce(sum(d.subtotal), 0)
                                    from CorteDetalles d where d.id_corte = id_corte_p),
            c.total_general = c.total_entradas + (select coalesce(sum(d.subtotal), 0)
                                    from CorteDetalles d where d.id_corte = id_corte_p)
        where c.id_corte = id_corte_p;
end$$
Delimiter ;

drop procedure if exists sp_corte_obtener_por_fecha;
Delimiter $$
create procedure sp_corte_obtener_por_fecha(in fecha_inicio_p date, in fecha_fin_p date)
begin
    select c.id_corte, c.fecha_corte, c.total_entradas, c.boletos_vendidos,
           c.total_dulceria, c.total_general, c.observaciones, c.fecha_registro,
           e.nombres as empleado_nombres, e.apellidos as empleado_apellidos,
           p.nombre_puesto as empleado_puesto
        from CortesCaja c
        inner join Empleados e on e.id_empleado = c.id_empleado
        inner join Puestos p on p.id_puesto = e.id_puesto
        where c.fecha_corte between fecha_inicio_p and fecha_fin_p
        order by c.fecha_corte desc, c.fecha_registro desc;
end$$
Delimiter ;

drop procedure if exists sp_corte_obtener_detalles;
Delimiter $$
create procedure sp_corte_obtener_detalles(in id_corte_p varchar(36))
begin
    select categoria, descripcion, cantidad, precio_unitario, subtotal
        from CorteDetalles
        where id_corte = id_corte_p
        order by categoria, descripcion;
end$$
Delimiter ;

drop procedure if exists sp_corte_existe;
Delimiter $$
create procedure sp_corte_existe(in id_empleado_p varchar(36), in fecha_corte_p date)
begin
    select count(*) as ya_existe
        from CortesCaja
        where id_empleado = id_empleado_p and fecha_corte = fecha_corte_p;
end$$
Delimiter ;

