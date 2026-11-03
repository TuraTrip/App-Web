package com.TuraTrip.backend.services;

import java.util.List;

import com.TuraTrip.backend.dtos.request.DenunciaRequest;
import com.TuraTrip.backend.dtos.response.DenunciaGrupoResponse;

/** Denuncias de usuarios y revisión por parte de los administradores. */
public interface ModeracionService {

    void denunciarPublicacion(Long publicacionId, String correo, DenunciaRequest request);

    void denunciarComentario(Long publicacionId, Long comentarioId, String correo, DenunciaRequest request);

    /** Solo administradores: contenidos con denuncias pendientes, agrupados. */
    List<DenunciaGrupoResponse> obtenerPendientes();

    /** Vuelve a mostrar el contenido y da sus denuncias por resueltas. */
    void restaurarPublicacion(Long publicacionId);

    void restaurarComentario(Long comentarioId);

    /** Oculta el contenido sin esperar a que llegue al umbral de denuncias. */
    void ocultarPublicacion(Long publicacionId);

    void ocultarComentario(Long comentarioId);
}
