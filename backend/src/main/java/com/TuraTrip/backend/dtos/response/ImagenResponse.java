package com.TuraTrip.backend.dtos.response;

/** Una foto de una publicación. El {@code id} sirve para conservarla al editar. */
public record ImagenResponse(
    Long id,
    String url
) {}
