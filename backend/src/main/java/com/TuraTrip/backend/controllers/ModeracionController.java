package com.TuraTrip.backend.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.TuraTrip.backend.dtos.request.DenunciaRequest;
import com.TuraTrip.backend.dtos.response.DenunciaGrupoResponse;
import com.TuraTrip.backend.dtos.response.MensajeResponse;
import com.TuraTrip.backend.services.ModeracionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Denuncias (cualquier usuario con sesión) y moderación (solo administradores).
 *
 * Las rutas {@code /api/v1/admin/**} exigen el rol ADMIN en SecurityConfig.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ModeracionController {

    private final ModeracionService moderacionService;

    @PostMapping("/publicaciones/{id}/denunciar")
    public ResponseEntity<MensajeResponse> denunciarPublicacion(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody DenunciaRequest request
    ) {
        moderacionService.denunciarPublicacion(id, authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MensajeResponse("Gracias. Revisaremos la publicación."));
    }

    @PostMapping("/publicaciones/{publicacionId}/comentarios/{comentarioId}/denunciar")
    public ResponseEntity<MensajeResponse> denunciarComentario(
            @PathVariable Long publicacionId,
            @PathVariable Long comentarioId,
            Authentication authentication,
            @Valid @RequestBody DenunciaRequest request
    ) {
        moderacionService.denunciarComentario(publicacionId, comentarioId, authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MensajeResponse("Gracias. Revisaremos el comentario."));
    }

    @GetMapping("/admin/denuncias")
    public ResponseEntity<List<DenunciaGrupoResponse>> obtenerPendientes() {
        return ResponseEntity.ok(moderacionService.obtenerPendientes());
    }

    @PostMapping("/admin/publicaciones/{id}/restaurar")
    public ResponseEntity<Void> restaurarPublicacion(@PathVariable Long id) {
        moderacionService.restaurarPublicacion(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/publicaciones/{id}/ocultar")
    public ResponseEntity<Void> ocultarPublicacion(@PathVariable Long id) {
        moderacionService.ocultarPublicacion(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/comentarios/{id}/restaurar")
    public ResponseEntity<Void> restaurarComentario(@PathVariable Long id) {
        moderacionService.restaurarComentario(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/comentarios/{id}/ocultar")
    public ResponseEntity<Void> ocultarComentario(@PathVariable Long id) {
        moderacionService.ocultarComentario(id);
        return ResponseEntity.noContent().build();
    }
}
