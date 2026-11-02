package com.TuraTrip.backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.TuraTrip.backend.models.Seguimiento;
import com.TuraTrip.backend.models.Usuario;

public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {

    Optional<Seguimiento> findBySeguidorIdAndSeguidoId(Long seguidorId, Long seguidoId);

    boolean existsBySeguidorIdAndSeguidoId(Long seguidorId, Long seguidoId);

    /** Cuántos siguen a este usuario. */
    long countBySeguidoId(Long usuarioId);

    /** A cuántos sigue este usuario. */
    long countBySeguidorId(Long usuarioId);

    @Query("select s.seguidor from Seguimiento s where s.seguido.id = :usuarioId order by s.fechaCreacion desc")
    List<Usuario> findSeguidores(@Param("usuarioId") Long usuarioId);

    @Query("select s.seguido from Seguimiento s where s.seguidor.id = :usuarioId order by s.fechaCreacion desc")
    List<Usuario> findSeguidos(@Param("usuarioId") Long usuarioId);
}
