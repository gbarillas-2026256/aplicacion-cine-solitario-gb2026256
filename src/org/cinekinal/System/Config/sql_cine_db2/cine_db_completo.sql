-- ============================================================
-- 01_tablas.sql
-- Crea la base de datos y todas las tablas con sus llaves e indices.
-- Debe ejecutarse SIEMPRE antes que cualquiera de los otros archivos.
-- ============================================================

drop database if exists cine_db_gb2026256_in4av;
create database cine_db_gb2026256_in4av;
use cine_db_gb2026256_in4av;

-- Crea el usuario de MySQL que usa la app (Enviroment.java) y le da
-- permisos sobre ESTA base de datos en especifico. Sin esto, aunque
-- 'IN4AV' ya exista de otro proyecto, MySQL rechaza la conexion con
-- "Access denied for user 'IN4AV'@'localhost'" porque nunca se le dio
-- acceso a cine_db_gb2026256_in4av.
create user if not exists 'IN4AV'@'localhost' identified by '&mnid4AV';
grant all privileges on cine_db_gb2026256_in4av.* to 'IN4AV'@'localhost';
flush privileges;

-- Catalogo de puestos/roles. nivel_jerarquico mas bajo = mas privilegios
-- (1 = Dueño). Esto permite comparar por numero en vez de por nombre.
create table if not exists Puestos (
    id_puesto int not null auto_increment,
    nombre_puesto varchar(40) not null,
    nivel_jerarquico int not null,
    constraint pk_puestos primary key (id_puesto)
);

create table if not exists Empleados (
    id_empleado varchar(36) not null,
    nombres varchar(60) not null,
    apellidos varchar(60) not null,
    correo varchar(80) not null,
    usuario varchar(30) not null,
    password varchar(60) not null,
    id_puesto int not null,
    activo boolean not null default true,
    motivo_baja varchar(255) null,
    constraint pk_empleados primary key (id_empleado),
    constraint uq_empleados_usuario unique (usuario),
    constraint fk_empleados_puesto foreign key (id_puesto) references Puestos(id_puesto)
);

-- Flujo de aprobacion: un empleado de menor jerarquia pide permiso a otro.
-- id_aprobador queda NULL mientras la solicitud sigue pendiente.
create table if not exists Solicitudes (
    id_solicitud varchar(36) not null,
    id_solicitante varchar(36) not null,
    id_aprobador varchar(36) null,
    accion varchar(120) not null,
    estado varchar(15) not null default 'PENDIENTE',
    fecha_solicitud datetime not null default current_timestamp,
    fecha_respuesta datetime null,
    constraint pk_solicitudes primary key (id_solicitud),
    constraint fk_solicitudes_solicitante foreign key (id_solicitante) references Empleados(id_empleado),
    constraint fk_solicitudes_aprobador foreign key (id_aprobador) references Empleados(id_empleado)
);

create table if not exists ReportesEmpleados (
    id_reporte varchar(36) not null,
    id_empleado varchar(36) not null,
    id_reportador varchar(36) not null,
    tipo_reporte varchar(60) not null,
    descripcion varchar(500) not null,
    fecha_reporte datetime not null default current_timestamp,
    constraint pk_reportes primary key (id_reporte),
    constraint fk_reportes_empleado foreign key (id_empleado) references Empleados(id_empleado),
    constraint fk_reportes_reportador foreign key (id_reportador) references Empleados(id_empleado)
);

create table if not exists Clientes (
    id_cliente varchar(36) not null,
    nombres varchar(60) not null,
    apellidos varchar(60) not null,
    correo varchar(80) not null,
    usuario varchar(30) not null,
    password varchar(60) not null,
    es_vip boolean not null default false,
    fecha_registro datetime not null default current_timestamp,
    constraint pk_clientes primary key (id_cliente),
    constraint uq_clientes_correo unique (correo),
    constraint uq_clientes_usuario unique (usuario)
);

create table if not exists Peliculas (
    id_pelicula varchar(36) not null,
    titulo varchar(120) not null,
    genero varchar(60) null,
    clasificacion varchar(10) null,
    duracion_min int not null,
    sinopsis varchar(500) null,
    poster_url varchar(300) null,
    trailer_url varchar(300) null,
    activa boolean not null default true,
    constraint pk_peliculas primary key (id_pelicula)
);

create table if not exists Salas (
    id_sala varchar(36) not null,
    nombre_sala varchar(40) not null,
    tipo_sala varchar(20) not null default 'Normal',
    filas int not null,
    columnas int not null,
    constraint pk_salas primary key (id_sala)
);

-- Los asientos pertenecen a la SALA, no a la funcion: existen sin
-- importar que pelicula se este proyectando.
create table if not exists Asientos (
    id_asiento varchar(36) not null,
    id_sala varchar(36) not null,
    fila varchar(2) not null,
    numero int not null,
    constraint pk_asientos primary key (id_asiento),
    constraint fk_asientos_sala foreign key (id_sala) references Salas(id_sala),
    constraint uq_asientos_posicion unique (id_sala, fila, numero)
);

create table if not exists Funciones (
    id_funcion varchar(36) not null,
    id_pelicula varchar(36) not null,
    id_sala varchar(36) not null,
    fecha date not null,
    hora time not null,
    precio_base decimal(6,2) not null,
    constraint pk_funciones primary key (id_funcion),
    constraint fk_funciones_pelicula foreign key (id_pelicula) references Peliculas(id_pelicula),
    constraint fk_funciones_sala foreign key (id_sala) references Salas(id_sala)
);

-- La tabla que amarra cliente + funcion + asiento. El UNIQUE de abajo
-- es la pieza mas importante de todo el esquema: le impide a la base
-- de datos vender el mismo asiento dos veces para la misma funcion.
create table if not exists Boletos (
    id_boleto varchar(36) not null,
    id_funcion varchar(36) not null,
    id_cliente varchar(36) not null,
    id_asiento varchar(36) not null,
    precio_final decimal(6,2) not null,
    fecha_compra datetime not null default current_timestamp,
    constraint pk_boletos primary key (id_boleto),
    constraint fk_boletos_funcion foreign key (id_funcion) references Funciones(id_funcion),
    constraint fk_boletos_cliente foreign key (id_cliente) references Clientes(id_cliente),
    constraint fk_boletos_asiento foreign key (id_asiento) references Asientos(id_asiento),
    constraint uq_boletos_asiento_funcion unique (id_funcion, id_asiento)
);

-- Indices extra para las consultas mas frecuentes de la app
create index idx_boletos_cliente on Boletos(id_cliente);
create index idx_funciones_fecha on Funciones(fecha);
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

-- Usuario Dueño por defecto (id_puesto 1 = Dueño)
insert into Empleados (id_empleado, nombres, apellidos, correo, usuario, password, id_puesto, activo) values
    (uuid(), 'Dueño', 'Principal', 'duenio@cinekinal.org', 'duenio', 'admin123', 1, true);

-- Salas de prueba con butacas
set @id_sala1 = uuid();
insert into Salas(id_sala, nombre_sala, tipo_sala, filas, columnas) values
    (@id_sala1, 'Sala 1 - IMAX Laser', 'IMAX', 4, 8);

insert into Asientos (id_asiento, id_sala, fila, numero) values
    (uuid(), @id_sala1, 'A', 1), (uuid(), @id_sala1, 'A', 2), (uuid(), @id_sala1, 'A', 3), (uuid(), @id_sala1, 'A', 4),
    (uuid(), @id_sala1, 'A', 5), (uuid(), @id_sala1, 'A', 6), (uuid(), @id_sala1, 'A', 7), (uuid(), @id_sala1, 'A', 8),
    (uuid(), @id_sala1, 'B', 1), (uuid(), @id_sala1, 'B', 2), (uuid(), @id_sala1, 'B', 3), (uuid(), @id_sala1, 'B', 4),
    (uuid(), @id_sala1, 'B', 5), (uuid(), @id_sala1, 'B', 6), (uuid(), @id_sala1, 'B', 7), (uuid(), @id_sala1, 'B', 8),
    (uuid(), @id_sala1, 'C', 1), (uuid(), @id_sala1, 'C', 2), (uuid(), @id_sala1, 'C', 3), (uuid(), @id_sala1, 'C', 4),
    (uuid(), @id_sala1, 'C', 5), (uuid(), @id_sala1, 'C', 6), (uuid(), @id_sala1, 'C', 7), (uuid(), @id_sala1, 'C', 8),
    (uuid(), @id_sala1, 'D', 1), (uuid(), @id_sala1, 'D', 2), (uuid(), @id_sala1, 'D', 3), (uuid(), @id_sala1, 'D', 4),
    (uuid(), @id_sala1, 'D', 5), (uuid(), @id_sala1, 'D', 6), (uuid(), @id_sala1, 'D', 7), (uuid(), @id_sala1, 'D', 8);

-- Películas de catálogo con póster y tráiler oficial de YouTube
set @id_peli1 = uuid();
set @id_peli2 = uuid();
set @id_peli3 = uuid();

insert into Peliculas(id_pelicula, titulo, genero, clasificacion, duracion_min, sinopsis, poster_url, trailer_url, activa) values
    (@id_peli1, 'Evangelion: 3.0+1.0 Thrice Upon a Time', 'Ciencia Ficción / Anime', 'PG-13', 155, 
     'Shinji Ikari se encuentra a la deriva después de perder la voluntad de vivir tras el Casi Tercer Impacto. Los supervivientes luchan en la última resistencia para salvar al mundo del Proyecto de Instrumentalización Humana.',
     'https://m.media-amazon.com/images/M/MV5BMjA5OTc3NjYtOWY2MC00MmI1LWExZGItMDYwNmNjMTljOTNhXkEyXkFqcGc@._V1_.jpg',
     'https://www.youtube.com/watch?v=10ict3GCxGY', true),
    (@id_peli2, 'Interstellar', 'Ciencia Ficción / Aventura', 'PG-13', 169,
     'Un grupo de exploradores espaciales viaja a través de un agujero de gusano cerca de Saturno en un intento desesperado por encontrar un nuevo hogar habitable para la humanidad.',
     'https://m.media-amazon.com/images/M/MV5BYzdjMDAxZGItMjI2My00ODA1LTlkNzItOWFjMDU5ZDJlYWY3XkEyXkFqcGc@._V1_.jpg',
     'https://www.youtube.com/watch?v=zSWdZVtXT7E', true),
    (@id_peli3, 'Spider-Man: Across the Spider-Verse', 'Animación / Acción', 'PG', 140,
     'Miles Morales es catapultado a través del Multiverso, donde se encuentra con una sociedad de Spider-People encargada de proteger su propia existencia.',
     'https://m.media-amazon.com/images/M/MV5BNThiZjA3MjItZGY5Ni00ZmJhLWEwN2EtOTBlYTA3CGExOTU2XkEyXkFqcGc@._V1_.jpg',
     'https://www.youtube.com/watch?v=cqGjhVJWtEg', true);

-- Funciones programadas para HOY
insert into Funciones(id_funcion, id_pelicula, id_sala, fecha, hora, precio_base) values
    (uuid(), @id_peli1, @id_sala1, curdate(), '15:30:00', 45.00),
    (uuid(), @id_peli2, @id_sala1, curdate(), '19:00:00', 50.00);

-- Funciones programadas para MAÑANA
insert into Funciones(id_funcion, id_pelicula, id_sala, fecha, hora, precio_base) values
    (uuid(), @id_peli3, @id_sala1, date_add(curdate(), interval 1 day), '14:00:00', 45.00),
    (uuid(), @id_peli1, @id_sala1, date_add(curdate(), interval 1 day), '18:30:00', 55.00);


-- ============================================================
-- 03_procedimientos_empleados.sql
-- Empleados, login de empleados, y el flujo de Solicitudes de permiso.
-- Requiere haber corrido 01_tablas.sql antes.
-- ============================================================

use cine_db_gb2026256_in4av;

drop procedure if exists sp_crear_empleado;
Delimiter $$
create procedure sp_crear_empleado(in nombres_p varchar(60), in apellidos_p varchar(60),
                                    in correo_p varchar(80), in usuario_p varchar(30),
                                    in password_p varchar(60), in id_puesto_p int)
begin
    insert into Empleados(id_empleado, nombres, apellidos, correo, usuario, password, id_puesto)
        values(uuid(), nombres_p, apellidos_p, correo_p, usuario_p, password_p, id_puesto_p);
end$$
Delimiter ;

-- Login de empleado: ya devuelve el nombre del puesto y su nivel
-- jerarquico en la misma consulta, para que Java sepa de una vez con
-- que permisos abrir la aplicacion.
drop procedure if exists sp_login_empleado;
Delimiter $$
create procedure sp_login_empleado(in usuario_p varchar(30), in password_p varchar(60))
begin
    select e.id_empleado, e.nombres, e.apellidos, e.correo, e.usuario,
           p.id_puesto, p.nombre_puesto, p.nivel_jerarquico
        from Empleados e
        inner join Puestos p on p.id_puesto = e.id_puesto
        where e.usuario = usuario_p and e.password = password_p and e.activo = true;
end$$
Delimiter ;

drop procedure if exists sp_obtener_empleados;
Delimiter $$
create procedure sp_obtener_empleados()
begin
    select e.id_empleado, e.nombres, e.apellidos, e.correo, e.usuario,
           p.id_puesto, p.nombre_puesto, p.nivel_jerarquico, e.activo
        from Empleados e
        inner join Puestos p on p.id_puesto = e.id_puesto
        where e.activo = true
        order by p.nivel_jerarquico, e.nombres;
end$$
Delimiter ;

-- No se borran empleados (romperia Solicitudes/Boletos historicos);
-- se desactivan.
drop procedure if exists sp_desactivar_empleado;
Delimiter $$
create procedure sp_desactivar_empleado(in id_empleado_p varchar(36), in motivo_baja_p varchar(255))
begin
    update Empleados
        set activo = false, motivo_baja = motivo_baja_p
        where id_empleado = id_empleado_p;
end$$
Delimiter ;

drop procedure if exists sp_editar_empleado;
Delimiter $$
create procedure sp_editar_empleado(in id_empleado_p varchar(36),
                                    in nombres_p varchar(60),
                                    in apellidos_p varchar(60),
                                    in correo_p varchar(80),
                                    in id_puesto_p int)
begin
    update Empleados
        set nombres = nombres_p,
            apellidos = apellidos_p,
            correo = correo_p,
            id_puesto = id_puesto_p
        where id_empleado = id_empleado_p;
end$$
Delimiter ;

drop procedure if exists sp_reportar_empleado;
Delimiter $$
create procedure sp_reportar_empleado(in id_empleado_p varchar(36),
                                      in id_reportador_p varchar(36),
                                      in tipo_reporte_p varchar(60),
                                      in descripcion_p varchar(500))
begin
    insert into ReportesEmpleados(id_reporte, id_empleado, id_reportador, tipo_reporte, descripcion)
        values(uuid(), id_empleado_p, id_reportador_p, tipo_reporte_p, descripcion_p);
end$$
Delimiter ;

-- ---------- SOLICITUDES (flujo de aprobacion) ----------

drop procedure if exists sp_crear_solicitud;
Delimiter $$
create procedure sp_crear_solicitud(in id_solicitante_p varchar(36), in accion_p varchar(120))
begin
    insert into Solicitudes(id_solicitud, id_solicitante, accion)
        values(uuid(), id_solicitante_p, accion_p);
end$$
Delimiter ;

drop procedure if exists sp_responder_solicitud;
Delimiter $$
create procedure sp_responder_solicitud(in id_solicitud_p varchar(36), in id_aprobador_p varchar(36),
                                         in estado_p varchar(15))
begin
    update Solicitudes
        set id_aprobador = id_aprobador_p, estado = estado_p, fecha_respuesta = current_timestamp
        where id_solicitud = id_solicitud_p;
end$$
Delimiter ;

drop procedure if exists sp_obtener_solicitudes_pendientes;
Delimiter $$
create procedure sp_obtener_solicitudes_pendientes()
begin
    select s.id_solicitud, s.accion, s.fecha_solicitud,
           e.nombres as solicitante_nombres, e.apellidos as solicitante_apellidos
        from Solicitudes s
        inner join Empleados e on e.id_empleado = s.id_solicitante
        where s.estado = 'PENDIENTE'
        order by s.fecha_solicitud;
end$$
Delimiter ;
-- ============================================================
-- 04_procedimientos_clientes.sql
-- Registro, login y VIP de clientes.
-- Requiere haber corrido 01_tablas.sql antes.
-- ============================================================

use cine_db_gb2026256_in4av;

drop procedure if exists sp_crear_cliente;
Delimiter $$
create procedure sp_crear_cliente(in nombres_p varchar(60), in apellidos_p varchar(60),
                                   in correo_p varchar(80), in usuario_p varchar(30),
                                   in password_p varchar(60))
begin
    insert into Clientes(id_cliente, nombres, apellidos, correo, usuario, password)
        values(uuid(), nombres_p, apellidos_p, correo_p, usuario_p, password_p);
end$$
Delimiter ;

drop procedure if exists sp_login_cliente;
Delimiter $$
create procedure sp_login_cliente(in usuario_p varchar(30), in password_p varchar(60))
begin
    select id_cliente, nombres, apellidos, correo, usuario, es_vip
        from Clientes
        where usuario = usuario_p and password = password_p;
end$$
Delimiter ;

drop procedure if exists sp_actualizar_vip;
Delimiter $$
create procedure sp_actualizar_vip(in id_cliente_p varchar(36), in es_vip_p boolean)
begin
    update Clientes set es_vip = es_vip_p where id_cliente = id_cliente_p;
end$$
Delimiter ;
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

drop procedure if exists sp_editar_pelicula;
Delimiter $$
create procedure sp_editar_pelicula(in id_pelicula_p varchar(36), in titulo_p varchar(120),
                                    in genero_p varchar(60), in clasificacion_p varchar(10),
                                    in duracion_min_p int, in sinopsis_p varchar(500),
                                    in poster_url_p varchar(300), in trailer_url_p varchar(300))
begin
    update Peliculas
        set titulo = titulo_p, genero = genero_p, clasificacion = clasificacion_p,
            duracion_min = duracion_min_p, sinopsis = sinopsis_p,
            poster_url = poster_url_p, trailer_url = trailer_url_p
        where id_pelicula = id_pelicula_p;
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
