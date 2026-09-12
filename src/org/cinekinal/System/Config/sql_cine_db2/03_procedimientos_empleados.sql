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
