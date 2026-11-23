package com.TuraTrip.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Una foto de una publicación. {@code orden} 0 es la portada. */
@Entity
@Table(name = "publicacion_imagenes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicacionImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publicacion_id", nullable = false)
    private Publicacion publicacion;

    /** Ruta relativa en el almacenamiento, p. ej. "publicaciones/uuid.jpg". */
    @Column(nullable = false, length = 500)
    private String ruta;

    @Column(nullable = false)
    private Integer orden;
}
