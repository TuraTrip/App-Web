package com.TuraTrip.backend.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TuraTrip.backend.models.Comentario;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByPublicacionIdAndOcultoFalseOrderByFechaCreacionAsc(Long publicacionId);

    Page<Comentario> findByUsuarioIdAndOcultoFalseOrderByFechaCreacionDesc(Long usuarioId, Pageable pageable);

}
