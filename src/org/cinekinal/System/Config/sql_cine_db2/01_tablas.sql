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
