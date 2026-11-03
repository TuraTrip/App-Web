package com.TuraTrip.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.TuraTrip.backend.dtos.request.PublicacionRequest;
import com.TuraTrip.backend.dtos.response.PublicacionResponse;

public interface PublicacionService {
    PublicacionResponse crearPublicacion(String correo, PublicacionRequest request, MultipartFile archivo);
    /** {@code archivo} es opcional: si es nulo o está vacío, se conserva la foto actual. */
    PublicacionResponse editarPublicacion(Long id, String correo, PublicacionRequest request, MultipartFile archivo);
    Page<PublicacionResponse> obtenerTodasLasPublicaciones(int page, int size);
    PublicacionResponse obtenerPublicacionPorId(Long id);
    Page<PublicacionResponse> obtenerPublicacionesPorUsuarioId(Long usuarioId, int page, int size);
    Page<PublicacionResponse> obtenerPublicacionesPorCorreoUsuario(String correo, int page, int size);
    void eliminarPublicacion(Long id, String correoUsuario);
}
