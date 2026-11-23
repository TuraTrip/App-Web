-- 11. Publicaciones guardadas por cada usuario (privadas: solo las ve quien las guardó).
CREATE TABLE guardados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    publicacion_id BIGINT NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    CONSTRAINT uq_guardados UNIQUE (usuario_id, publicacion_id),
    CONSTRAINT fk_guardados_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_guardados_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicaciones (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
