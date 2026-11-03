package com.TuraTrip.backend.controllers;

import com.TuraTrip.backend.dtos.request.EditarPerfilRequest;
import com.TuraTrip.backend.dtos.response.ComentarioResponse;
import com.TuraTrip.backend.dtos.response.PerfilResponse;
import com.TuraTrip.backend.services.ComentarioService;
import com.TuraTrip.backend.dtos.response.UsuarioResumenResponse;
import java.util.List;
import com.TuraTrip.backend.services.PerfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;
    private final ComentarioService comentarioService;

    @GetMapping("/perfil")
    public ResponseEntity<PerfilResponse> obtenerPerfil(
            Authentication authentication) {

        PerfilResponse perfil = perfilService.obtenerPerfil(authentication.getName());
        return ResponseEntity.ok(perfil);
    }

    @GetMapping("/perfil/{id}")
    public ResponseEntity<PerfilResponse> obtenerPerfilPorId(
            @PathVariable Long id) {

        PerfilResponse perfil = perfilService.obtenerPerfilPorId(id);
        return ResponseEntity.ok(perfil);
    }

    @PutMapping("/perfil")
    public ResponseEntity<PerfilResponse> editarPerfil(
            Authentication authentication,
            @Valid @RequestBody EditarPerfilRequest request) {

        PerfilResponse actualizado = perfilService.editarPerfil(
            authentication.getName(), request);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/{id}/comentarios")
    public ResponseEntity<Page<ComentarioResponse>> obtenerComentariosDeUsuario(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ComentarioResponse> comentarios =
                comentarioService.obtenerComentariosPorUsuarioId(id, page, size);
        return ResponseEntity.ok(comentarios);
    }

    /** Activa Premium 30 días (perfil verificado). Modo demostración: no se cobra nada. */
    @PostMapping("/suscripcion")
    public ResponseEntity<PerfilResponse> activarPremium(Authentication authentication) {
        return ResponseEntity.ok(perfilService.activarPremium(authentication.getName()));
    }

    @DeleteMapping("/suscripcion")
    public ResponseEntity<PerfilResponse> cancelarPremium(Authentication authentication) {
        return ResponseEntity.ok(perfilService.cancelarPremium(authentication.getName()));
    }

    /** Búsqueda pública de cuentas por nombre (máx. 20). No expone correos. */
    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioResumenResponse>> buscar(@RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok(perfilService.buscar(q));
    }

    @PostMapping("/{id}/seguir")
    public ResponseEntity<PerfilResponse> seguir(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(perfilService.seguir(id, authentication.getName()));
    }

    @DeleteMapping("/{id}/seguir")
    public ResponseEntity<PerfilResponse> dejarDeSeguir(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(perfilService.dejarDeSeguir(id, authentication.getName()));
    }

    @GetMapping("/{id}/seguidores")
    public ResponseEntity<List<UsuarioResumenResponse>> obtenerSeguidores(@PathVariable Long id) {
        return ResponseEntity.ok(perfilService.obtenerSeguidores(id));
    }

    @GetMapping("/{id}/seguidos")
    public ResponseEntity<List<UsuarioResumenResponse>> obtenerSeguidos(@PathVariable Long id) {
        return ResponseEntity.ok(perfilService.obtenerSeguidos(id));
    }
}
