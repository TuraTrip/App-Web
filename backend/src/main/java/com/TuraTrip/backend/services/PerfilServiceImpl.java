package com.TuraTrip.backend.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.TuraTrip.backend.dtos.request.EditarPerfilRequest;
import com.TuraTrip.backend.dtos.response.PerfilResponse;
import com.TuraTrip.backend.dtos.response.UsuarioResumenResponse;
import com.TuraTrip.backend.exceptions.UsuarioNoEncontradoException;
import com.TuraTrip.backend.mappers.UsuarioMapper;
import com.TuraTrip.backend.models.Seguimiento;
import com.TuraTrip.backend.models.Usuario;
import com.TuraTrip.backend.repositories.PublicacionRepository;
import com.TuraTrip.backend.repositories.SeguimientoRepository;
import com.TuraTrip.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PerfilServiceImpl implements PerfilService {

    /** Duración de cada activación de Premium. */
    private static final long DIAS_PREMIUM = 30;

    private final UsuarioRepository usuarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final UsuarioMapper usuarioMapper;
    private final UsuarioActual usuarioActual;

    @Override
    @Transactional(readOnly = true)
    public PerfilResponse obtenerPerfil(String correo) {
        Usuario usuario = buscarPorCorreo(correo);
        return conEstadisticas(usuario, Optional.of(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilResponse obtenerPerfilPorId(Long id) {
        return conEstadisticas(buscarPorId(id), usuarioActual.obtener());
    }

    @Override
    @Transactional
    public PerfilResponse editarPerfil(String correo, EditarPerfilRequest request) {
        Usuario usuario = buscarPorCorreo(correo);

        if (request.nombre() != null && !request.nombre().isBlank()) {
            usuario.setNombre(request.nombre().trim());
        }
        // Una biografía vacía borra la anterior; null la deja como está.
        if (request.biografia() != null) {
            usuario.setBiografia(request.biografia().isBlank() ? null : request.biografia().trim());
        }

        usuarioRepository.save(usuario);

        return conEstadisticas(usuario, Optional.of(usuario));
    }

    @Override
    @Transactional
    public PerfilResponse seguir(Long id, String correo) {
        Usuario yo = buscarPorCorreo(correo);
        Usuario otro = buscarPorId(id);

        if (!yo.getId().equals(otro.getId())
                && !seguimientoRepository.existsBySeguidorIdAndSeguidoId(yo.getId(), otro.getId())) {
            seguimientoRepository.save(Seguimiento.builder()
                    .seguidor(yo)
                    .seguido(otro)
                    .fechaCreacion(LocalDateTime.now())
                    .build());
        }
        return conEstadisticas(otro, Optional.of(yo));
    }

    @Override
    @Transactional
    public PerfilResponse dejarDeSeguir(Long id, String correo) {
        Usuario yo = buscarPorCorreo(correo);
        Usuario otro = buscarPorId(id);
        seguimientoRepository.findBySeguidorIdAndSeguidoId(yo.getId(), otro.getId())
                .ifPresent(seguimientoRepository::delete);
        return conEstadisticas(otro, Optional.of(yo));
    }

    /**
     * Activa Premium por {@link #DIAS_PREMIUM} días. MODO DEMOSTRACIÓN: no hay cobro.
     * Si ya estaba activo, se extiende desde la fecha de fin actual.
     */
    @Override
    @Transactional
    public PerfilResponse activarPremium(String correo) {
        Usuario usuario = buscarPorCorreo(correo);
        LocalDateTime desde = usuario.isVerificado() ? usuario.getPremiumHasta() : LocalDateTime.now();
        usuario.setPremiumHasta(desde.plusDays(DIAS_PREMIUM));
        usuarioRepository.save(usuario);
        return conEstadisticas(usuario, Optional.of(usuario));
    }

    @Override
    @Transactional
    public PerfilResponse cancelarPremium(String correo) {
        Usuario usuario = buscarPorCorreo(correo);
        usuario.setPremiumHasta(null);
        usuarioRepository.save(usuario);
        return conEstadisticas(usuario, Optional.of(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResumenResponse> buscar(String texto) {
        if (texto == null || texto.isBlank()) return List.of();
        return usuarioRepository
                .findTop20ByNombreContainingIgnoreCaseAndHabilitadoTrueOrderByNombreAsc(texto.trim())
                .stream().map(usuarioMapper::toResumen).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResumenResponse> obtenerSeguidores(Long id) {
        buscarPorId(id);
        return seguimientoRepository.findSeguidores(id).stream().map(usuarioMapper::toResumen).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResumenResponse> obtenerSeguidos(Long id) {
        buscarPorId(id);
        return seguimientoRepository.findSeguidos(id).stream().map(usuarioMapper::toResumen).toList();
    }

    /**
     * Completa el perfil con contadores y con si {@code visitante} lo sigue.
     * El correo y la fecha de fin de Premium solo se muestran en el perfil propio.
     */
    private PerfilResponse conEstadisticas(Usuario usuario, Optional<Usuario> visitante) {
        boolean esPropio = visitante.map(v -> v.getId().equals(usuario.getId())).orElse(false);
        boolean loSigo = !esPropio && visitante
                .map(v -> seguimientoRepository.existsBySeguidorIdAndSeguidoId(v.getId(), usuario.getId()))
                .orElse(false);

        return usuarioMapper.toPerfilResponse(usuario).conEstadisticas(
                esPropio,
                publicacionRepository.countByUsuarioId(usuario.getId()),
                seguimientoRepository.countBySeguidoId(usuario.getId()),
                seguimientoRepository.countBySeguidorId(usuario.getId()),
                loSigo
        );
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
    }

    private Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
            .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
    }
}
