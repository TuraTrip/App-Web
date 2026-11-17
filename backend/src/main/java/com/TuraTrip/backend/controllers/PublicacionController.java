package com.TuraTrip.backend.controllers;

import com.TuraTrip.backend.dtos.request.PublicacionRequest;
import com.TuraTrip.backend.dtos.response.PublicacionResponse;
import com.TuraTrip.backend.services.PublicacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/publicaciones")
@RequiredArgsConstructor
public class PublicacionController {

    private final PublicacionService publicacionService;

    @GetMapping
    public ResponseEntity<Page<PublicacionResponse>> obtenerTodasLasPublicaciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<PublicacionResponse> publicaciones = publicacionService.obtenerTodasLasPublicaciones(page, size);
        return ResponseEntity.ok(publicaciones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicacionResponse> obtenerPublicacionPorId(@PathVariable Long id) {
        PublicacionResponse publicacion = publicacionService.obtenerPublicacionPorId(id);
        return ResponseEntity.ok(publicacion);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PublicacionResponse> crearPublicacion(
            Authentication authentication,
            @Valid @RequestPart("publicacion") PublicacionRequest request,
            @RequestPart("archivo") MultipartFile archivo
    ) {
        String correoUsuario = authentication.getName();
        PublicacionResponse nuevaPublicacion = publicacionService.crearPublicacion(correoUsuario, request, archivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaPublicacion);
    }

    /**
     * Edita los datos de la publicación. Solo el autor.
     * La parte {@code archivo} es opcional: si no llega, se conserva la foto actual.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PublicacionResponse> editarPublicacion(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestPart("publicacion") PublicacionRequest request,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo
    ) {
        String correoUsuario = authentication.getName();
        PublicacionResponse actualizada = publicacionService.editarPublicacion(id, correoUsuario, request, archivo);
        return ResponseEntity.ok(actualizada);
    }

    @GetMapping("/mias")
    public ResponseEntity<Page<PublicacionResponse>> obtenerMisPublicaciones(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String correoUsuario = authentication.getName();
        Page<PublicacionResponse> misPublicaciones = publicacionService.obtenerPublicacionesPorCorreoUsuario(correoUsuario, page, size);
        return ResponseEntity.ok(misPublicaciones);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Page<PublicacionResponse>> obtenerPublicacionesPorUsuarioId(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<PublicacionResponse> publicaciones = publicacionService.obtenerPublicacionesPorUsuarioId(usuarioId, page, size);
        return ResponseEntity.ok(publicaciones);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPublicacion(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String correoUsuario = authentication.getName();
        publicacionService.eliminarPublicacion(id, correoUsuario);
        return ResponseEntity.noContent().build();
    }
}
