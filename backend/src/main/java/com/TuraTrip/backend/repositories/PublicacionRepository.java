package com.TuraTrip.backend.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.TuraTrip.backend.models.Publicacion;
import com.TuraTrip.backend.models.Usuario;

public interface PublicacionRepository extends JpaRepository<Publicacion, Long>, JpaSpecificationExecutor<Publicacion> {

    /** Feed: solo publicaciones visibles (no ocultas por moderación). */
    Page<Publicacion> findAllByOcultaFalse(Pageable pageable);

    Page<Publicacion> findAllByUsuarioId(Long usuarioId, Pageable pageable);

    /** Perfil de otra persona: sin las ocultas por moderación. */
    Page<Publicacion> findAllByUsuarioIdAndOcultaFalse(Long usuarioId, Pageable pageable);

    Page<Publicacion> findAllByUsuario(Usuario usuario, Pageable pageable);

    int countByCategoriaId(Long categoriaId);

    long countByUsuarioId(Long usuarioId);

    /** Publicaciones con ubicación, para el mapa. */
    List<Publicacion> findTop500ByOcultaFalseAndLatitudIsNotNullAndLongitudIsNotNullOrderByFechaCreacionDesc();

    /** Publicaciones de las personas que sigue el usuario (pestaña "Siguiendo"). */
    @Query("""
        select p from Publicacion p
        where p.oculta = false
          and p.usuario.id in (select s.seguido.id from Seguimiento s where s.seguidor.id = :usuarioId)
        """)
    Page<Publicacion> findDeSeguidos(@Param("usuarioId") Long usuarioId, Pageable pageable);

    /** Publicaciones a las que el usuario dio "me gusta", de la más reciente a la más antigua. */
    @Query(
        value = "select m.publicacion from MeGusta m where m.usuario.id = :usuarioId and m.publicacion.oculta = false order by m.fechaCreacion desc",
        countQuery = "select count(m) from MeGusta m where m.usuario.id = :usuarioId and m.publicacion.oculta = false"
    )
    Page<Publicacion> findConMeGustaDe(@Param("usuarioId") Long usuarioId, Pageable pageable);

    /** Publicaciones que guardó el usuario, de la más recién guardada a la más antigua. */
    @Query(
        value = "select g.publicacion from Guardado g where g.usuario.id = :usuarioId and g.publicacion.oculta = false order by g.fechaCreacion desc",
        countQuery = "select count(g) from Guardado g where g.usuario.id = :usuarioId and g.publicacion.oculta = false"
    )
    Page<Publicacion> findGuardadasPor(@Param("usuarioId") Long usuarioId, Pageable pageable);

    /**
     * Ids de las publicaciones a menos de {@code radioKm} kilómetros, de la más cercana a la
     * más lejana (fórmula de Haversine). {@code least(1, ...)} evita errores de redondeo en
     * {@code acos}. Devuelve ids y no entidades porque la consulta es nativa y la entidad tiene
     * un campo calculado ({@code @Formula}) que no existe como columna.
     */
    @Query(value = """
        select t.id from (
            select p.id, (6371 * acos(least(1,
                cos(radians(:lat)) * cos(radians(p.latitud)) * cos(radians(p.longitud) - radians(:lon))
                + sin(radians(:lat)) * sin(radians(p.latitud))))) as distancia_km
            from publicaciones p
            where p.latitud is not null and p.longitud is not null and p.oculta = false
        ) t
        where t.distancia_km <= :radioKm
        order by t.distancia_km
        limit :limite
        """, nativeQuery = true)
    List<Number> findIdsCercanas(
        @Param("lat") double latitud,
        @Param("lon") double longitud,
        @Param("radioKm") double radioKm,
        @Param("limite") int limite
    );
}
