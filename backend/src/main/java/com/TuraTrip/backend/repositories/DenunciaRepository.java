package com.TuraTrip.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.TuraTrip.backend.models.Denuncia;

public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {

    boolean existsByUsuarioIdAndPublicacionId(Long usuarioId, Long publicacionId);

    boolean existsByUsuarioIdAndComentarioId(Long usuarioId, Long comentarioId);

    long countByPublicacionIdAndEstado(Long publicacionId, Denuncia.Estado estado);

    long countByComentarioIdAndEstado(Long comentarioId, Denuncia.Estado estado);

    /** Denuncias por revisar, las más recientes primero. */
    List<Denuncia> findAllByEstadoOrderByFechaCreacionDesc(Denuncia.Estado estado);

    /** Marca como resueltas las denuncias pendientes de una publicación. */
    @Modifying
    @Query("update Denuncia d set d.estado = :nuevo where d.publicacion.id = :id and d.estado = :actual")
    int cambiarEstadoDePublicacion(
        @Param("id") Long publicacionId,
        @Param("actual") Denuncia.Estado actual,
        @Param("nuevo") Denuncia.Estado nuevo
    );

    @Modifying
    @Query("update Denuncia d set d.estado = :nuevo where d.comentario.id = :id and d.estado = :actual")
    int cambiarEstadoDeComentario(
        @Param("id") Long comentarioId,
        @Param("actual") Denuncia.Estado actual,
        @Param("nuevo") Denuncia.Estado nuevo
    );
}
