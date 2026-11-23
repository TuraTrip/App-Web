package com.TuraTrip.backend.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.TuraTrip.backend.models.Denuncia;

/** Motivo: SPAM, OFENSIVO, FALSO, ACOSO u OTRO. El detalle es opcional. */
public record DenunciaRequest(
    @NotNull(message = "Elige un motivo")
    Denuncia.Motivo motivo,

    @Size(max = 300, message = "El detalle no puede exceder 300 caracteres")
    String detalle
) {}
