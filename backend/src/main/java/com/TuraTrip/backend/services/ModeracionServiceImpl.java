package com.TuraTrip.backend.services;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TuraTrip.backend.dtos.request.DenunciaRequest;
import com.TuraTrip.backend.dtos.response.DenunciaGrupoResponse;
import com.TuraTrip.backend.exceptions.ArchivoInvalidoException;
import com.TuraTrip.backend.exceptions.DenunciaDuplicadaException;
import com.TuraTrip.backend.exceptions.PublicacionNoEncontradaException;
import com.TuraTrip.backend.exceptions.ResourceNotFoundException;
import com.TuraTrip.backend.exceptions.UsuarioNoEncontradoException;
import com.TuraTrip.backend.models.Comentario;
import com.TuraTrip.backend.models.Denuncia;
import com.TuraTrip.backend.models.Publicacion;
import com.TuraTrip.backend.models.Usuario;
import com.TuraTrip.backend.repositories.ComentarioRepository;
import com.TuraTrip.backend.repositories.DenunciaRepository;
import com.TuraTrip.backend.repositories.PublicacionRepository;
import com.TuraTrip.backend.repositories.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModeracionServiceImpl implements ModeracionService {

    /** Con este número de denuncias de personas distintas, el contenido se oculta solo. */
    static final long UMBRAL_OCULTAR = 3;

    private final DenunciaRepository denunciaRepository;
    private final PublicacionRepository publicacionRepository;
    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final StorageService storageService;

    @Override
    @Transactional
    public void denunciarPublicacion(Long publicacionId, String correo, DenunciaRequest request) {
        Publicacion publicacion = buscarPublicacion(publicacionId);
        Usuario usuario = buscarUsuario(correo);

        if (publicacion.getUsuario().getId().equals(usuario.getId())) {
            throw new ArchivoInvalidoException("No puedes denunciar tu propia publicación");
        }
        if (denunciaRepository.existsByUsuarioIdAndPublicacionId(usuario.getId(), publicacionId)) {
            throw new DenunciaDuplicadaException("Ya denunciaste esta publicación. La estamos revisando.");
        }

        denunciaRepository.save(nueva(usuario, request).publicacion(publicacion).build());

        if (denunciaRepository.countByPublicacionIdAndEstado(publicacionId, Denuncia.Estado.PENDIENTE) >= UMBRAL_OCULTAR) {
            publicacion.setOculta(true);
        }
    }

    @Override
    @Transactional
    public void denunciarComentario(Long publicacionId, Long comentarioId, String correo, DenunciaRequest request) {
        Comentario comentario = buscarComentario(comentarioId);
        if (!comentario.getPublicacion().getId().equals(publicacionId)) {
            throw new ResourceNotFoundException("El comentario no existe");
        }
        Usuario usuario = buscarUsuario(correo);

        if (comentario.getUsuario().getId().equals(usuario.getId())) {
            throw new ArchivoInvalidoException("No puedes denunciar tu propio comentario");
        }
        if (denunciaRepository.existsByUsuarioIdAndComentarioId(usuario.getId(), comentarioId)) {
            throw new DenunciaDuplicadaException("Ya denunciaste este comentario. Lo estamos revisando.");
        }

        denunciaRepository.save(nueva(usuario, request).comentario(comentario).build());

        if (denunciaRepository.countByComentarioIdAndEstado(comentarioId, Denuncia.Estado.PENDIENTE) >= UMBRAL_OCULTAR) {
            comentario.setOculto(true);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DenunciaGrupoResponse> obtenerPendientes() {
        List<Denuncia> pendientes = denunciaRepository.findAllByEstadoOrderByFechaCreacionDesc(Denuncia.Estado.PENDIENTE);

        // Agrupa por contenido ("P<id>" o "C<id>") conservando el orden: lo más reciente primero.
        Map<String, List<Denuncia>> porContenido = pendientes.stream().collect(Collectors.groupingBy(
                d -> d.getPublicacion() != null ? "P" + d.getPublicacion().getId() : "C" + d.getComentario().getId(),
                LinkedHashMap::new,
                Collectors.toList()));

        return porContenido.values().stream()
                .map(this::aGrupo)
                // Lo más denunciado arriba; a igualdad, lo más reciente.
                .sorted(Comparator.comparingLong(DenunciaGrupoResponse::totalDenuncias).reversed()
                        .thenComparing(DenunciaGrupoResponse::ultimaDenuncia, Comparator.reverseOrder()))
                .toList();
    }

    @Override
    @Transactional
    public void restaurarPublicacion(Long publicacionId) {
        buscarPublicacion(publicacionId).setOculta(false);
        denunciaRepository.cambiarEstadoDePublicacion(publicacionId, Denuncia.Estado.PENDIENTE, Denuncia.Estado.RESUELTA);
    }

    @Override
    @Transactional
    public void restaurarComentario(Long comentarioId) {
        buscarComentario(comentarioId).setOculto(false);
        denunciaRepository.cambiarEstadoDeComentario(comentarioId, Denuncia.Estado.PENDIENTE, Denuncia.Estado.RESUELTA);
    }

    @Override
    @Transactional
    public void ocultarPublicacion(Long publicacionId) {
        buscarPublicacion(publicacionId).setOculta(true);
    }

    @Override
    @Transactional
    public void ocultarComentario(Long comentarioId) {
        buscarComentario(comentarioId).setOculto(true);
    }

    // ---------------------------------------------------------------------------------------

    private Denuncia.DenunciaBuilder nueva(Usuario usuario, DenunciaRequest request) {
        String detalle = request.detalle() == null || request.detalle().isBlank() ? null : request.detalle().trim();
        return Denuncia.builder()
                .usuario(usuario)
                .motivo(request.motivo())
                .detalle(detalle)
                .fechaCreacion(LocalDateTime.now());
    }

    private DenunciaGrupoResponse aGrupo(List<Denuncia> denuncias) {
        Denuncia primera = denuncias.get(0);
        Map<String, Long> motivos = denuncias.stream()
                .collect(Collectors.groupingBy(d -> d.getMotivo().name(), TreeMap::new, Collectors.counting()));
        List<String> detalles = denuncias.stream().map(Denuncia::getDetalle).filter(Objects::nonNull).toList();
        LocalDateTime ultima = denuncias.stream().map(Denuncia::getFechaCreacion)
                .max(Comparator.naturalOrder()).orElse(primera.getFechaCreacion());

        if (primera.getPublicacion() != null) {
            Publicacion p = primera.getPublicacion();
            return new DenunciaGrupoResponse(
                    "PUBLICACION", p.getId(), null, p.getTitulo(), storageService.construirUrlPublica(p.getImagen()),
                    p.getUsuario().getId(), p.getUsuario().getNombre(), Boolean.TRUE.equals(p.getOculta()),
                    denuncias.size(), motivos, detalles, ultima);
        }
        Comentario c = primera.getComentario();
        return new DenunciaGrupoResponse(
                "COMENTARIO", c.getPublicacion().getId(), c.getId(), c.getContenido(), null,
                c.getUsuario().getId(), c.getUsuario().getNombre(), Boolean.TRUE.equals(c.getOculto()),
                denuncias.size(), motivos, detalles, ultima);
    }

    private Publicacion buscarPublicacion(Long id) {
        return publicacionRepository.findById(id)
                .orElseThrow(() -> new PublicacionNoEncontradaException("Publicación no encontrada"));
    }

    private Comentario buscarComentario(Long id) {
        return comentarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El comentario no existe"));
    }

    private Usuario buscarUsuario(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
    }
}
