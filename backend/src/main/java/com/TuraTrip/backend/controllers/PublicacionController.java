package com.TuraTrip.backend.controllers;

import com.TuraTrip.backend.dtos.request.PublicacionRequest;
import com.TuraTrip.backend.dtos.response.GuardadoResponse;
import com.TuraTrip.backend.dtos.response.MeGustaResponse;
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

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/publicaciones")
@RequiredArgsConstructor
public class PublicacionController {

    private final PublicacionService publicacionService;

    /** Busca en título, descripción, lugar, autor y categoría. Sin {@code q} devuelve todo. */
    @GetMapping("/buscar")
    public ResponseEntity<Page<PublicacionResponse>> buscar(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(publicacionService.buscar(q, page, size));
    }

    /** Publicaciones a menos de {@code radioKm} del punto, de la más cercana a la más lejana. */
    @GetMapping("/cercanas")
    public ResponseEntity<List<PublicacionResponse>> obtenerCercanas(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "50") double radioKm,
            @RequestParam(defaultValue = "50") int limite
    ) {
        return ResponseEntity.ok(publicacionService.obtenerCercanas(lat, lon, radioKm, limite));
    }

    /** Todas las publicaciones con ubicación (máx. 500), para pintarlas en el mapa. */
    @GetMapping("/mapa")
    public ResponseEntity<List<PublicacionResponse>> obtenerParaMapa() {
        return ResponseEntity.ok(publicacionService.obtenerParaMapa());
    }

    /** Pestaña "Siguiendo": publicaciones de las cuentas que sigue el usuario. Requiere sesión. */
    @GetMapping("/siguiendo")
    public ResponseEntity<Page<PublicacionResponse>> obtenerDeSeguidos(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(publicacionService.obtenerDeSeguidos(authentication.getName(), page, size));
    }

    /** "Lugares que me gustan" del usuario de la sesión. */
    @GetMapping("/me-gusta")
    public ResponseEntity<Page<PublicacionResponse>> obtenerConMeGusta(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(publicacionService.obtenerConMeGusta(authentication.getName(), page, size));
    }

    /** Publicaciones guardadas por el usuario de la sesión (privadas). */
    @GetMapping("/guardadas")
    public ResponseEntity<Page<PublicacionResponse>> obtenerGuardadas(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(publicacionService.obtenerGuardadas(authentication.getName(), page, size));
    }

    @PostMapping("/{id}/guardar")
    public ResponseEntity<GuardadoResponse> guardar(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(publicacionService.guardar(id, authentication.getName()));
    }

    @DeleteMapping("/{id}/guardar")
    public ResponseEntity<GuardadoResponse> quitarGuardado(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(publicacionService.quitarGuardado(id, authentication.getName()));
    }

    @PostMapping("/{id}/me-gusta")
    public ResponseEntity<MeGustaResponse> darMeGusta(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(publicacionService.darMeGusta(id, authentication.getName()));
    }

    @DeleteMapping("/{id}/me-gusta")
    public ResponseEntity<MeGustaResponse> quitarMeGusta(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(publicacionService.quitarMeGusta(id, authentication.getName()));
    }

    /** Edita los datos (no la foto). Solo el autor o un administrador. */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicacionResponse> editarPublicacion(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody PublicacionRequest request
    ) {
        return ResponseEntity.ok(publicacionService.editarPublicacion(id, authentication.getName(), request));
    }

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

    /**
     * Crea una publicación con 1 a 10 fotos en las partes {@code archivos} (en orden; la
     * primera es la portada). Por compatibilidad también acepta una sola foto en {@code archivo}.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PublicacionResponse> crearPublicacion(
            Authentication authentication,
            @Valid @RequestPart("publicacion") PublicacionRequest request,
            @RequestPart(value = "archivos", required = false) List<MultipartFile> archivos,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo
    ) {
        List<MultipartFile> fotos = new ArrayList<>();
        if (archivo != null && !archivo.isEmpty()) fotos.add(archivo);
        if (archivos != null) archivos.stream().filter(a -> a != null && !a.isEmpty()).forEach(fotos::add);

        String correoUsuario = authentication.getName();
        PublicacionResponse nuevaPublicacion = publicacionService.crearPublicacion(correoUsuario, request, fotos);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaPublicacion);
    }

    /**
     * Cambia las fotos de una publicación propia. {@code orden} es una lista JSON con el orden
     * final: ids de fotos existentes que se conservan y {@code "nueva:N"} para el N-ésimo
     * archivo de {@code archivos}. Ej.: {@code ["12", "nueva:0", "15"]}.
     */
    @PutMapping(value = "/{id}/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PublicacionResponse> reemplazarImagenes(
            @PathVariable Long id,
            Authentication authentication,
            @RequestPart("orden") List<String> orden,
            @RequestPart(value = "archivos", required = false) List<MultipartFile> archivos
    ) {
        return ResponseEntity.ok(publicacionService.reemplazarImagenes(
                id, authentication.getName(), orden, archivos == null ? List.of() : archivos));
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
