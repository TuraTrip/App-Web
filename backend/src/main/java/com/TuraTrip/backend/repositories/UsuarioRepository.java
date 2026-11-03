package com.TuraTrip.backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.TuraTrip.backend.models.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByCorreo(String correo);
    Optional<Usuario> findByCorreo(String correo);

    /** Búsqueda de cuentas por nombre; solo cuentas verificadas. */
    List<Usuario> findTop20ByNombreContainingIgnoreCaseAndHabilitadoTrueOrderByNombreAsc(String nombre);
}
