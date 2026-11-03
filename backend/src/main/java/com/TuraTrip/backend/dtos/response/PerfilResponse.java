package com.TuraTrip.backend.dtos.response;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Perfil de un usuario con sus estadísticas.
 *
 * {@code correo} y {@code premiumHasta} solo se envían en el perfil propio; en el de otros
 * llegan en {@code null}. {@code verificado} sí es público: es el check que ven todos.
 * {@code loSigo} indica si quien hace la petición sigue a este usuario.
 */
public record PerfilResponse(
    Long id,
    String nombre,
    String correo,
    String fotoPerfil,
    Set<String> roles,
    String biografia,
    boolean verificado,
    LocalDateTime premiumHasta,
    long totalPublicaciones,
    long seguidores,
    long siguiendo,
    boolean loSigo
) {

    public PerfilResponse conEstadisticas(
            boolean esPropio, long totalPublicaciones, long seguidores, long siguiendo, boolean loSigo) {
        return new PerfilResponse(
            id, nombre, esPropio ? correo : null, fotoPerfil, roles, biografia,
            verificado, esPropio ? premiumHasta : null,
            totalPublicaciones, seguidores, siguiendo, loSigo
        );
    }
}
