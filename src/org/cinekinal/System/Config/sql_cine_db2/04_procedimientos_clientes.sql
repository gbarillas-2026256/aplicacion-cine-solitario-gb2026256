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
