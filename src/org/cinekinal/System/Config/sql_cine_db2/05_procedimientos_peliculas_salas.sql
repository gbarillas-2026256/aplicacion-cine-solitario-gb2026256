-- ============================================================
-- 05_procedimientos_peliculas_salas.sql
-- Catalogo de peliculas, salas y sus asientos.
-- Requiere haber corrido 01_tablas.sql antes.
-- ============================================================

use cine_db_gb2026256_in4av;

-- ---------- PELICULAS ----------

drop procedure if exists sp_crear_pelicula;
Delimiter $$
create procedure sp_crear_pelicula(in titulo_p varchar(120), in genero_p varchar(60),
                                    in clasificacion_p varchar(10), in duracion_min_p int,
                                    in sinopsis_p varchar(500), in poster_url_p varchar(300),
                                    in trailer_url_p varchar(300))
begin
    insert into Peliculas(id_pelicula, titulo, genero, clasificacion, duracion_min, sinopsis, poster_url, trailer_url)
        values(uuid(), titulo_p, genero_p, clasificacion_p, duracion_min_p, sinopsis_p, poster_url_p, trailer_url_p);
end$$
Delimiter ;

drop procedure if exists sp_obtener_peliculas;
Delimiter $$
create procedure sp_obtener_peliculas()
begin
    select id_pelicula, titulo, genero, clasificacion, duracion_min, sinopsis, poster_url, trailer_url
        from Peliculas
        where activa = true
        order by titulo;
end$$
Delimiter ;

drop procedure if exists sp_desactivar_pelicula;
Delimiter $$
create procedure sp_desactivar_pelicula(in id_pelicula_p varchar(36))
begin
    update Peliculas set activa = false where id_pelicula = id_pelicula_p;
end$$
Delimiter ;

-- ---------- SALAS Y ASIENTOS ----------

-- Crea la sala Y genera automaticamente todos sus asientos
-- (fila A,B,C... x columna 1,2,3...), para no tener que insertarlos
-- uno por uno desde Java. Devuelve el id de la sala recien creada.
drop procedure if exists sp_crear_sala;
Delimiter $$
create procedure sp_crear_sala(in nombre_sala_p varchar(40), in tipo_sala_p varchar(20),
                                in filas_p int, in columnas_p int)
begin
    declare nueva_sala_id varchar(36);
    declare i int default 1;
    declare j int default 1;

    set nueva_sala_id = uuid();

    insert into Salas(id_sala, nombre_sala, tipo_sala, filas, columnas)
        values(nueva_sala_id, nombre_sala_p, tipo_sala_p, filas_p, columnas_p);

    set i = 1;
    while i <= filas_p do
        set j = 1;
        while j <= columnas_p do
            insert into Asientos(id_asiento, id_sala, fila, numero)
                values(uuid(), nueva_sala_id, char(64 + i), j);
            set j = j + 1;
        end while;
        set i = i + 1;
    end while;

    select nueva_sala_id as id_sala;
end$$
Delimiter ;

drop procedure if exists sp_obtener_salas;
Delimiter $$
create procedure sp_obtener_salas()
begin
    select id_sala, nombre_sala, tipo_sala, filas, columnas from Salas order by nombre_sala;
end$$
Delimiter ;

drop procedure if exists sp_obtener_asientos_por_sala;
Delimiter $$
create procedure sp_obtener_asientos_por_sala(in id_sala_p varchar(36))
begin
    select id_asiento, fila, numero
        from Asientos
        where id_sala = id_sala_p
        order by fila, numero;
end$$
Delimiter ;
