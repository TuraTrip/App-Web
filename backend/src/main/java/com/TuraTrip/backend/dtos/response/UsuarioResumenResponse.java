package com.TuraTrip.backend.dtos.response;

/** Datos públicos mínimos de una cuenta: búsqueda y listas de seguidores. Sin correo. */
public record UsuarioResumenResponse(
    Long id,
    String nombre,
    String fotoPerfil,
    String biografia,
    boolean verificado
) {}
