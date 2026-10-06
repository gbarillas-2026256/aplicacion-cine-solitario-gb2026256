-- ============================================================
-- 06_procedimientos_funciones_boletos.sql
-- Funciones (horarios de proyeccion) y Boletos (compra de entradas).
-- Requiere haber corrido 01_tablas.sql antes.
-- ============================================================

use cine_db_gb2026256_in4av;

-- ---------- FUNCIONES ----------

drop procedure if exists sp_crear_funcion;
Delimiter $$
create procedure sp_crear_funcion(in id_pelicula_p varchar(36), in id_sala_p varchar(36),
                                   in fecha_p date, in hora_p time, in precio_base_p decimal(6,2))
begin
    insert into Funciones(id_funcion, id_pelicula, id_sala, fecha, hora, precio_base)
        values(uuid(), id_pelicula_p, id_sala_p, fecha_p, hora_p, precio_base_p);
end$$
Delimiter ;

-- La cartelera: funciones futuras con el titulo, la sala y ficha tecnica resueltos
drop procedure if exists sp_obtener_cartelera;
Delimiter $$
create procedure sp_obtener_cartelera()
begin
    select f.id_funcion, f.id_sala, p.titulo, p.duracion_min, p.genero, p.clasificacion,
           p.sinopsis, p.poster_url, p.trailer_url, s.nombre_sala, s.tipo_sala,
           f.fecha, f.hora, f.precio_base
        from Funciones f
        inner join Peliculas p on p.id_pelicula = f.id_pelicula
        inner join Salas s on s.id_sala = f.id_sala
        where f.fecha >= curdate()
        order by f.fecha, f.hora;
end$$
Delimiter ;

-- Cartelera filtrada por una fecha especifica (hoy, mañana, etc.)
drop procedure if exists sp_obtener_cartelera_por_fecha;
Delimiter $$
create procedure sp_obtener_cartelera_por_fecha(in fecha_p date)
begin
    select f.id_funcion, f.id_sala, p.titulo, p.duracion_min, p.genero, p.clasificacion,
           p.sinopsis, p.poster_url, p.trailer_url, s.nombre_sala, s.tipo_sala,
           f.fecha, f.hora, f.precio_base
        from Funciones f
        inner join Peliculas p on p.id_pelicula = f.id_pelicula
        inner join Salas s on s.id_sala = f.id_sala
        where f.fecha = fecha_p
        order by f.hora;
end$$
Delimiter ;

-- ---------- BOLETOS ----------

-- Que asientos de una funcion ya estan vendidos, para pintarlos como
-- ocupados en el mapa de butacas antes de que el cliente elija.
drop procedure if exists sp_obtener_asientos_ocupados;
Delimiter $$
create procedure sp_obtener_asientos_ocupados(in id_funcion_p varchar(36))
begin
    select id_asiento from Boletos where id_funcion = id_funcion_p;
end$$
Delimiter ;

-- Si el asiento ya esta vendido para esa funcion, el UNIQUE de la
-- tabla Boletos rechaza el insert con un error de SQL — Java debe
-- atrapar esa excepcion y avisarle al cliente que elija otro asiento.
drop procedure if exists sp_comprar_boleto;
Delimiter $$
create procedure sp_comprar_boleto(in id_funcion_p varchar(36), in id_cliente_p varchar(36),
                                    in id_asiento_p varchar(36), in precio_final_p decimal(6,2))
begin
    insert into Boletos(id_boleto, id_funcion, id_cliente, id_asiento, precio_final)
        values(uuid(), id_funcion_p, id_cliente_p, id_asiento_p, precio_final_p);
end$$
Delimiter ;

drop procedure if exists sp_obtener_boletos_por_cliente;
Delimiter $$
create procedure sp_obtener_boletos_por_cliente(in id_cliente_p varchar(36))
begin
    select b.id_boleto, p.titulo, s.nombre_sala, f.fecha, f.hora,
           a.fila, a.numero, b.precio_final, b.fecha_compra
        from Boletos b
        inner join Funciones f on f.id_funcion = b.id_funcion
        inner join Peliculas p on p.id_pelicula = f.id_pelicula
        inner join Salas s on s.id_sala = f.id_sala
        inner join Asientos a on a.id_asiento = b.id_asiento
        where b.id_cliente = id_cliente_p
        order by f.fecha desc, f.hora desc;
end$$
Delimiter ;
