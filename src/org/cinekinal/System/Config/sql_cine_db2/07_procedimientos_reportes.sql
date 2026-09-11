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
