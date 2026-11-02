package com.TuraTrip.backend.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.TuraTrip.backend.dtos.request.PublicacionRequest;
import com.TuraTrip.backend.dtos.response.GuardadoResponse;
import com.TuraTrip.backend.dtos.response.MeGustaResponse;
import com.TuraTrip.backend.dtos.response.PublicacionResponse;

public interface PublicacionService {
    /** Entre 1 y 10 fotos; la primera es la portada. */
    PublicacionResponse crearPublicacion(String correo, PublicacionRequest request, List<MultipartFile> archivos);
    PublicacionResponse editarPublicacion(Long id, String correo, PublicacionRequest request);
    PublicacionResponse reemplazarImagenes(Long id, String correo, List<String> orden, List<MultipartFile> nuevas);
    Page<PublicacionResponse> obtenerTodasLasPublicaciones(int page, int size);
    PublicacionResponse obtenerPublicacionPorId(Long id);
    Page<PublicacionResponse> obtenerPublicacionesPorUsuarioId(Long usuarioId, int page, int size);
    Page<PublicacionResponse> obtenerPublicacionesPorCorreoUsuario(String correo, int page, int size);
    Page<PublicacionResponse> buscar(String texto, int page, int size);
    Page<PublicacionResponse> obtenerDeSeguidos(String correo, int page, int size);
    Page<PublicacionResponse> obtenerConMeGusta(String correo, int page, int size);
    List<PublicacionResponse> obtenerCercanas(double latitud, double longitud, double radioKm, int limite);
    List<PublicacionResponse> obtenerParaMapa();
    MeGustaResponse darMeGusta(Long id, String correo);
    MeGustaResponse quitarMeGusta(Long id, String correo);
    /** Guardados: privados, solo los ve quien los guardó. */
    Page<PublicacionResponse> obtenerGuardadas(String correo, int page, int size);
    GuardadoResponse guardar(Long id, String correo);
    GuardadoResponse quitarGuardado(Long id, String correo);
    void eliminarPublicacion(Long id, String correoUsuario);
}
