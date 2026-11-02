package com.TuraTrip.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "publicaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(nullable = false, length = 500)
    private String imagen;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    /** Dirección legible, p. ej. "Playa Piangüita, Buenaventura". Opcional. */
    @Column(length = 500)
    private String direccion;

    private Double latitud;

    private Double longitud;

    /** Se calcula en la misma consulta, sin cargar todos los "me gusta". */
    @Formula("(select count(*) from me_gusta m where m.publicacion_id = id)")
    private Long totalMeGusta;

    /** Oculta por denuncias: no aparece en feed, búsqueda ni mapa hasta que un admin la revisa. */
    @Builder.Default
    @Column(nullable = false)
    private Boolean oculta = false;

    // Los comentarios se borran junto con la publicación (también hay ON DELETE CASCADE en la BD).
    @Builder.Default
    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaCreacion ASC")
    // Los comentarios ocultos por denuncias no se muestran dentro de la publicación.
    @SQLRestriction("oculto = false")
    private List<Comentario> comentarios = new ArrayList<>();

    /** Fotos en orden (máx. 10). La primera coincide con {@link #imagen}, la portada. */
    @Builder.Default
    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<PublicacionImagen> imagenes = new ArrayList<>();
}
