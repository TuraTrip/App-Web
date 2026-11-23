-- 8. Biografía, "me gusta" y seguidores.
-- Las columnas de ubicación ya las crea V7; aquí solo se indexan para la búsqueda por cercanía.

ALTER TABLE publicaciones
    ADD INDEX idx_publicaciones_ubicacion (latitud, longitud);

-- Al eliminar una publicación o un usuario se eliminan también sus comentarios (V6 no lo hacía).
ALTER TABLE comentarios
    DROP FOREIGN KEY fk_comentario_usuario,
    DROP FOREIGN KEY fk_comentario_publicacion;
ALTER TABLE comentarios
    ADD CONSTRAINT fk_comentario_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_comentario_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicaciones (id) ON DELETE CASCADE,
    ADD INDEX idx_comentarios_publicacion (publicacion_id, fecha_creacion);

ALTER TABLE usuarios
    ADD COLUMN biografia VARCHAR(160) NULL;

-- Un usuario solo puede dar un "me gusta" por publicación.
CREATE TABLE me_gusta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    publicacion_id BIGINT NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    CONSTRAINT uq_me_gusta UNIQUE (usuario_id, publicacion_id),
    CONSTRAINT fk_me_gusta_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_me_gusta_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicaciones (id) ON DELETE CASCADE,
    INDEX idx_me_gusta_publicacion (publicacion_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- seguidor_id sigue a seguido_id.
CREATE TABLE seguimientos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seguidor_id BIGINT NOT NULL,
    seguido_id BIGINT NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    CONSTRAINT uq_seguimientos UNIQUE (seguidor_id, seguido_id),
    CONSTRAINT fk_seguimientos_seguidor FOREIGN KEY (seguidor_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_seguimientos_seguido FOREIGN KEY (seguido_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    INDEX idx_seguimientos_seguido (seguido_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
