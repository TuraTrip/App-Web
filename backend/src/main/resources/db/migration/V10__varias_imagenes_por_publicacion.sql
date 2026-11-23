-- 10. Varias fotos por publicación (máximo 10, lo controla el backend).
-- publicaciones.imagen se conserva como portada (la primera foto) para no romper a los
-- clientes que solo leen ese campo.
CREATE TABLE publicacion_imagenes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    publicacion_id BIGINT NOT NULL,
    ruta VARCHAR(500) NOT NULL,
    orden INT NOT NULL,
    CONSTRAINT fk_publicacion_imagenes_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicaciones (id) ON DELETE CASCADE,
    INDEX idx_publicacion_imagenes_orden (publicacion_id, orden)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Las publicaciones existentes pasan a tener su única foto como primera de la lista.
INSERT INTO publicacion_imagenes (publicacion_id, ruta, orden)
SELECT id, imagen, 0 FROM publicaciones;
