package com.TuraTrip.backend.repositories;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.TuraTrip.backend.models.Guardado;

public interface GuardadoRepository extends JpaRepository<Guardado, Long> {

    Optional<Guardado> findByUsuarioIdAndPublicacionId(Long usuarioId, Long publicacionId);

    /** De estas publicaciones, cuáles guardó el usuario: una sola consulta por página. */
    @Query("select g.publicacion.id from Guardado g where g.usuario.id = :usuarioId and g.publicacion.id in :ids")
    Set<Long> findIdsGuardados(@Param("usuarioId") Long usuarioId, @Param("ids") Collection<Long> ids);
}
