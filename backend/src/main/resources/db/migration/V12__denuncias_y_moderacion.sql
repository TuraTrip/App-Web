-- 12. Denuncias de publicaciones y comentarios, y ocultación por moderación.

-- Una publicación o comentario oculto no aparece en el feed, la búsqueda ni el mapa hasta
-- que un administrador lo restaura (o lo elimina).
ALTER TABLE publicaciones ADD COLUMN oculta BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE comentarios ADD COLUMN oculto BOOLEAN NOT NULL DEFAULT FALSE;

-- Cada denuncia apunta a una publicación o a un comentario (uno de los dos).
-- Un usuario solo puede denunciar una vez el mismo contenido.
CREATE TABLE denuncias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    publicacion_id BIGINT NULL,
    comentario_id BIGINT NULL,
    motivo VARCHAR(30) NOT NULL,
    detalle VARCHAR(300) NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion DATETIME(6) NOT NULL,
    CONSTRAINT uq_denuncias_publicacion UNIQUE (usuario_id, publicacion_id),
    CONSTRAINT uq_denuncias_comentario UNIQUE (usuario_id, comentario_id),
    CONSTRAINT fk_denuncias_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_denuncias_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicaciones (id) ON DELETE CASCADE,
    CONSTRAINT fk_denuncias_comentario FOREIGN KEY (comentario_id) REFERENCES comentarios (id) ON DELETE CASCADE,
    INDEX idx_denuncias_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
