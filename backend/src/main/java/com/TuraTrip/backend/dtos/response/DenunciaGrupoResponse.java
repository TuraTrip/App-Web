package com.TuraTrip.backend.dtos.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Un contenido denunciado (publicación o comentario) con todas sus denuncias pendientes
 * agrupadas, para la pantalla de moderación.
 *
 * @param tipo "PUBLICACION" o "COMENTARIO".
 * @param texto título de la publicación o texto del comentario.
 * @param motivos cuántas denuncias hay por cada motivo.
 * @param detalles comentarios libres que dejaron quienes denunciaron.
 */
public record DenunciaGrupoResponse(
    String tipo,
    Long publicacionId,
    Long comentarioId,
    String texto,
    String imagen,
    Long autorId,
    String autorNombre,
    boolean oculto,
    long totalDenuncias,
    Map<String, Long> motivos,
    List<String> detalles,
    LocalDateTime ultimaDenuncia
) {}
