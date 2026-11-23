package com.TuraTrip.backend.dtos.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos de una publicación. Se usa al crearla (parte JSON del multipart) y al editarla
 * (PUT con JSON). La ubicación es opcional, pero latitud y longitud van juntas.
 */
public record PublicacionRequest(
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255, message = "El título no puede exceder 255 caracteres")
    String titulo,

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    String descripcion,

    String categoria,

    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    String direccion,

    @DecimalMin(value = "-90.0", message = "Latitud inválida")
    @DecimalMax(value = "90.0", message = "Latitud inválida")
    Double latitud,

    @DecimalMin(value = "-180.0", message = "Longitud inválida")
    @DecimalMax(value = "180.0", message = "Longitud inválida")
    Double longitud

) {}
