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


