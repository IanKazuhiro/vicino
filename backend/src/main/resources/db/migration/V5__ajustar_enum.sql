ALTER TABLE usuario DROP CONSTRAINT ck_usuario_perfil;

UPDATE usuario SET perfil = 'PORTARIA' WHERE perfil = 'PORTEIRO';

ALTER TABLE usuario
    ADD CONSTRAINT ck_usuario_perfil
    CHECK (perfil IN ('MORADOR', 'PORTARIA', 'SINDICO', 'ADMINISTRADOR'));