package com.TuraTrip.backend.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TuraTrip.backend.dtos.request.ComentarioRequest;
import com.TuraTrip.backend.dtos.response.ComentarioResponse;
import com.TuraTrip.backend.exceptions.AccesoNoAutorizadoException;
import com.TuraTrip.backend.exceptions.PublicacionNoEncontradaException;
import com.TuraTrip.backend.exceptions.ResourceNotFoundException;
import com.TuraTrip.backend.exceptions.UsuarioNoEncontradoException;
import com.TuraTrip.backend.mappers.ComentarioMapper;
import com.TuraTrip.backend.models.Comentario;
import com.TuraTrip.backend.models.Publicacion;
import com.TuraTrip.backend.models.Usuario;
import com.TuraTrip.backend.repositories.ComentarioRepository;
import com.TuraTrip.backend.repositories.PublicacionRepository;
import com.TuraTrip.backend.repositories.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComentarioServiceImpl implements ComentarioService {

    private final ComentarioRepository comentarioRepository;

    private final UsuarioRepository usuarioRepository;

    private final PublicacionRepository publicacionRepository;

    private final ComentarioMapper comentarioMapper;

    @Override
    @Transactional
    public ComentarioResponse crearComentario(
            Long publicacionId,
            String correoUsuario,
            ComentarioRequest request
    ) {

        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() ->
                        new UsuarioNoEncontradoException("Usuario no encontrado"));

        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() ->
                        new PublicacionNoEncontradaException("Publicación no encontrada"));

        Comentario comentario = Comentario.builder()
                .contenido(request.contenido())
                .fechaCreacion(LocalDateTime.now())
                .usuario(usuario)
                .publicacion(publicacion)
                .build();

        Comentario guardado = comentarioRepository.save(comentario);

        return comentarioMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComentarioResponse> obtenerComentarios(Long publicacionId) {

        if (!publicacionRepository.existsById(publicacionId)) {
            throw new PublicacionNoEncontradaException("Publicación no encontrada");
        }

        return comentarioRepository
                .findByPublicacionIdAndOcultoFalseOrderByFechaCreacionAsc(publicacionId)
                .stream()
                .map(comentarioMapper::toResponse)
                .toList();

    }

    @Override
    @Transactional
    public ComentarioResponse editarComentario(
            Long publicacionId,
            Long comentarioId,
            String correoUsuario,
            ComentarioRequest request
    ) {

        Comentario comentario = obtenerComentarioValidado(publicacionId, comentarioId);

        Usuario usuarioActual = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() ->
                        new UsuarioNoEncontradoException("Usuario no encontrado"));

        boolean esPropietario = comentario.getUsuario().getId().equals(usuarioActual.getId());

        if (!esPropietario) {
            throw new AccesoNoAutorizadoException("No tienes permisos para editar este comentario");
        }

        comentario.setContenido(request.contenido());

        return comentarioMapper.toResponse(comentario);
    }

    @Override
    @Transactional
    public void eliminarComentario(Long publicacionId, Long comentarioId, String correoUsuario) {

        Comentario comentario = obtenerComentarioValidado(publicacionId, comentarioId);

        Usuario usuarioActual = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() ->
                        new UsuarioNoEncontradoException("Usuario no encontrado"));

        boolean esAdmin = usuarioActual.getRoles().stream()
                .anyMatch(rol -> rol.getNombre().equalsIgnoreCase("ADMIN"));

        boolean esPropietario = comentario.getUsuario().getId().equals(usuarioActual.getId());

        // El dueño de la publicación también puede borrar comentarios de su publicación.
        boolean esDuenoPublicacion = comentario.getPublicacion().getUsuario().getId().equals(usuarioActual.getId());

        if (!esAdmin && !esPropietario && !esDuenoPublicacion) {
            throw new AccesoNoAutorizadoException("No tienes permisos para eliminar este comentario");
        }

        comentarioRepository.delete(comentario);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComentarioResponse> obtenerComentariosPorUsuarioId(Long usuarioId, int page, int size) {

        if (!usuarioRepository.existsById(usuarioId)) {
            throw new UsuarioNoEncontradoException("Usuario no encontrado");
        }

        Pageable pageable = PageRequest.of(page, size);

        return comentarioRepository
                .findByUsuarioIdAndOcultoFalseOrderByFechaCreacionDesc(usuarioId, pageable)
                .map(comentarioMapper::toResponse);
    }

    private Comentario obtenerComentarioValidado(Long publicacionId, Long comentarioId) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado"));

        if (!comentario.getPublicacion().getId().equals(publicacionId)) {
            throw new ResourceNotFoundException("Comentario no encontrado");
        }

        return comentario;
    }

}
