package com.TuraTrip.backend.dtos.request;

import jakarta.validation.constraints.Size;

/** Campos opcionales: los que lleguen en {@code null} no se modifican. */
public record EditarPerfilRequest(

    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    String nombre,

    @Size(max = 160, message = "La biografía no puede exceder 160 caracteres")
    String biografia

) {}
