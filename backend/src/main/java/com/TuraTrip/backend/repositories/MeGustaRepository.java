package com.TuraTrip.backend.repositories;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.TuraTrip.backend.models.MeGusta;

public interface MeGustaRepository extends JpaRepository<MeGusta, Long> {

    Optional<MeGusta> findByUsuarioIdAndPublicacionId(Long usuarioId, Long publicacionId);

    long countByPublicacionId(Long publicacionId);

    /** De estas publicaciones, cuáles le gustan al usuario: una sola consulta por página. */
    @Query("select m.publicacion.id from MeGusta m where m.usuario.id = :usuarioId and m.publicacion.id in :ids")
    Set<Long> findIdsConMeGusta(@Param("usuarioId") Long usuarioId, @Param("ids") Collection<Long> ids);
}
