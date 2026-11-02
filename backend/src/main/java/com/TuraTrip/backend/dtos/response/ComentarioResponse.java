package com.TuraTrip.backend.dtos.response;

import java.time.LocalDateTime;

public record ComentarioResponse(

    Long id,
    String contenido,
    LocalDateTime fechaCreacion,
    Long autorId,
    String autorNombre,
    String autorFotoPerfil,
    Long publicacionId,
    String publicacionTitulo,
    boolean autorVerificado

) {}
