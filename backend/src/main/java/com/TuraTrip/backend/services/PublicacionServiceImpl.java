package com.TuraTrip.backend.services;

import com.TuraTrip.backend.dtos.request.PublicacionRequest;
import com.TuraTrip.backend.dtos.response.GuardadoResponse;
import com.TuraTrip.backend.dtos.response.MeGustaResponse;
import com.TuraTrip.backend.dtos.response.PublicacionResponse;
import com.TuraTrip.backend.exceptions.AccesoNoAutorizadoException;
import com.TuraTrip.backend.exceptions.ArchivoInvalidoException;
import com.TuraTrip.backend.exceptions.PublicacionNoEncontradaException;
import com.TuraTrip.backend.exceptions.UsuarioNoEncontradoException;
import com.TuraTrip.backend.mappers.PublicacionMapper;
import com.TuraTrip.backend.models.Categoria;
import com.TuraTrip.backend.models.Guardado;
import com.TuraTrip.backend.models.MeGusta;
import com.TuraTrip.backend.models.Publicacion;
import com.TuraTrip.backend.models.PublicacionImagen;
import com.TuraTrip.backend.models.Usuario;
import com.TuraTrip.backend.repositories.CategoriaRepository;
import com.TuraTrip.backend.repositories.GuardadoRepository;
import com.TuraTrip.backend.repositories.MeGustaRepository;
import com.TuraTrip.backend.repositories.PublicacionRepository;
import com.TuraTrip.backend.repositories.UsuarioRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicacionServiceImpl implements PublicacionService {

    private static final Sort MAS_RECIENTES = Sort.by("fechaCreacion").descending();

    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final MeGustaRepository meGustaRepository;
    private final GuardadoRepository guardadoRepository;
    private final PublicacionMapper publicacionMapper;
    private final StorageService storageService;
    private final UsuarioActual usuarioActual;

    @Override
    @Transactional
    public PublicacionResponse crearPublicacion(String correo, PublicacionRequest request, List<MultipartFile> archivos) {

        // 1. Validar que el usuario exista y que lleguen entre 1 y 10 fotos
        Usuario usuario = buscarUsuario(correo);
        validarCantidad(archivos.size());

        // 2. Construir la publicación con sus datos
        Publicacion publicacion = Publicacion.builder()
                .fechaCreacion(LocalDateTime.now())
                .usuario(usuario)
                .build();
        aplicarDatos(publicacion, request);

        // 3. Guardar cada archivo (StorageService valida tipo y tamaño) y enlazarlo en orden
        List<String> guardadas = new ArrayList<>();
        try {
            for (MultipartFile archivo : archivos) {
                guardadas.add(storageService.guardarArchivo(archivo, "publicaciones"));
            }
        } catch (RuntimeException e) {
            // Si una foto falla, no se dejan huérfanas en disco las que ya se guardaron.
            guardadas.forEach(storageService::eliminarArchivo);
            throw e;
        }
        ponerImagenes(publicacion, guardadas);

        Publicacion guardada = publicacionRepository.save(publicacion);

        return publicacionMapper.toResponse(guardada);
    }

    @Override
    @Transactional
    public PublicacionResponse editarPublicacion(Long id, String correo, PublicacionRequest request) {
        Publicacion publicacion = buscarPublicacion(id);
        verificarPermiso(publicacion, buscarUsuario(correo), "No tienes permisos para editar esta publicación");

        // Solo los datos; las fotos se cambian con reemplazarImagenes().
        aplicarDatos(publicacion, request);
        return aRespuesta(publicacionRepository.save(publicacion));
    }

    /**
     * Deja las fotos de la publicación exactamente como indica {@code orden}: cada elemento
     * es el id de una foto existente que se conserva, o "nueva:N" para el N-ésimo archivo
     * de {@code nuevas}. Las fotos que no aparecen se borran del disco.
     */
    @Override
    @Transactional
    public PublicacionResponse reemplazarImagenes(
            Long id, String correo, List<String> orden, List<MultipartFile> nuevas) {
        Publicacion publicacion = buscarPublicacion(id);
        verificarPermiso(publicacion, buscarUsuario(correo), "No tienes permisos para editar esta publicación");
        validarCantidad(orden.size());

        Map<Long, String> existentes = publicacion.getImagenes().stream()
                .collect(Collectors.toMap(PublicacionImagen::getId, PublicacionImagen::getRuta));

        // Primero se valida todo, para no guardar archivos si la petición es incorrecta.
        for (String elemento : orden) {
            if (elemento.startsWith(PREFIJO_NUEVA)) {
                int indice = indiceNueva(elemento);
                if (indice < 0 || indice >= nuevas.size()) {
                    throw new ArchivoInvalidoException("Foto nueva no encontrada: " + elemento);
                }
            } else if (!existentes.containsKey(idExistente(elemento))) {
                throw new ArchivoInvalidoException("La foto " + elemento + " no pertenece a esta publicación");
            }
        }

        List<String> guardadas = new ArrayList<>();
        Map<Integer, String> rutasNuevas = new HashMap<>();
        try {
            for (int i = 0; i < nuevas.size(); i++) {
                String ruta = storageService.guardarArchivo(nuevas.get(i), "publicaciones");
                guardadas.add(ruta);
                rutasNuevas.put(i, ruta);
            }
        } catch (RuntimeException e) {
            guardadas.forEach(storageService::eliminarArchivo);
            throw e;
        }

        List<String> rutas = orden.stream()
                .map(e -> e.startsWith(PREFIJO_NUEVA) ? rutasNuevas.get(indiceNueva(e)) : existentes.get(idExistente(e)))
                .toList();

        // Las fotos que ya no están se borran del disco después de guardar los cambios.
        Set<String> quitadas = new HashSet<>(existentes.values());
        quitadas.removeAll(rutas);

        ponerImagenes(publicacion, rutas);
        Publicacion actualizada = publicacionRepository.save(publicacion);
        quitadas.forEach(storageService::eliminarArchivo);
        return aRespuesta(actualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicacionResponse> obtenerTodasLasPublicaciones(int page, int size) {
        return aRespuestas(publicacionRepository.findAllByOcultaFalse(PageRequest.of(page, size, MAS_RECIENTES)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicacionResponse> obtenerPublicacionesPorUsuarioId(Long usuarioId, int page, int size) {
        // Validar primero si el usuario existe para lanzar la excepción correspondiente si no se halla
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new UsuarioNoEncontradoException("Usuario no encontrado");
        }
        // El propio autor ve también las ocultas (marcadas "en revisión"); los demás, no.
        boolean esPropio = usuarioActual.id().map(usuarioId::equals).orElse(false);
        Pageable pageable = PageRequest.of(page, size, MAS_RECIENTES);
        return aRespuestas(esPropio
                ? publicacionRepository.findAllByUsuarioId(usuarioId, pageable)
                : publicacionRepository.findAllByUsuarioIdAndOcultaFalse(usuarioId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicacionResponse> obtenerPublicacionesPorCorreoUsuario(String correo, int page, int size) {
        Usuario usuario = buscarUsuario(correo);
        return aRespuestas(publicacionRepository.findAllByUsuario(usuario, PageRequest.of(page, size, MAS_RECIENTES)));
    }

    @Override
    @Transactional(readOnly = true)
    public PublicacionResponse obtenerPublicacionPorId(Long id) {
        Publicacion publicacion = buscarPublicacion(id);
        // Una publicación oculta solo la ven su autor y los administradores (para revisarla).
        if (Boolean.TRUE.equals(publicacion.getOculta())) {
            boolean puedeVerla = usuarioActual.obtener()
                    .map(u -> u.getId().equals(publicacion.getUsuario().getId())
                            || u.getRoles().stream().anyMatch(r -> r.getNombre().equalsIgnoreCase("ADMIN")))
                    .orElse(false);
            if (!puedeVerla) throw new PublicacionNoEncontradaException("Publicación no encontrada");
        }
        return aRespuesta(publicacion);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicacionResponse> buscar(String texto, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, MAS_RECIENTES);
        if (texto == null || texto.isBlank()) {
            return aRespuestas(publicacionRepository.findAllByOcultaFalse(pageable));
        }
        return aRespuestas(publicacionRepository.findAll(contieneTodasLasPalabras(texto), pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicacionResponse> obtenerDeSeguidos(String correo, int page, int size) {
        Usuario usuario = buscarUsuario(correo);
        return aRespuestas(publicacionRepository.findDeSeguidos(usuario.getId(), PageRequest.of(page, size, MAS_RECIENTES)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicacionResponse> obtenerConMeGusta(String correo, int page, int size) {
        Usuario usuario = buscarUsuario(correo);
        // El orden lo fija la consulta (fecha del "me gusta"), no la fecha de la publicación.
        return aRespuestas(publicacionRepository.findConMeGustaDe(usuario.getId(), PageRequest.of(page, size)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicacionResponse> obtenerCercanas(double latitud, double longitud, double radioKm, int limite) {
        List<Long> ids = publicacionRepository
                .findIdsCercanas(latitud, longitud, radioKm, Math.min(limite, 200))
                .stream().map(Number::longValue).toList();
        if (ids.isEmpty()) return List.of();

        // findAllById no respeta el orden: se reordena por distancia con la lista de ids.
        Map<Long, Publicacion> porId = publicacionRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Publicacion::getId, Function.identity()));
        List<Publicacion> ordenadas = ids.stream().map(porId::get).filter(p -> p != null).toList();
        return aRespuestas(ordenadas);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicacionResponse> obtenerParaMapa() {
        return aRespuestas(publicacionRepository.findTop500ByOcultaFalseAndLatitudIsNotNullAndLongitudIsNotNullOrderByFechaCreacionDesc());
    }

    @Override
    @Transactional
    public MeGustaResponse darMeGusta(Long id, String correo) {
        Publicacion publicacion = buscarPublicacion(id);
        Usuario usuario = buscarUsuario(correo);

        // Idempotente: darlo dos veces no duplica ni falla.
        if (meGustaRepository.findByUsuarioIdAndPublicacionId(usuario.getId(), id).isEmpty()) {
            meGustaRepository.save(MeGusta.builder()
                    .usuario(usuario)
                    .publicacion(publicacion)
                    .fechaCreacion(LocalDateTime.now())
                    .build());
        }
        return new MeGustaResponse(true, meGustaRepository.countByPublicacionId(id));
    }

    @Override
    @Transactional
    public MeGustaResponse quitarMeGusta(Long id, String correo) {
        buscarPublicacion(id);
        Usuario usuario = buscarUsuario(correo);
        meGustaRepository.findByUsuarioIdAndPublicacionId(usuario.getId(), id)
                .ifPresent(meGustaRepository::delete);
        return new MeGustaResponse(false, meGustaRepository.countByPublicacionId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicacionResponse> obtenerGuardadas(String correo, int page, int size) {
        Usuario usuario = buscarUsuario(correo);
        // Las más recién guardadas primero.
        return aRespuestas(publicacionRepository.findGuardadasPor(usuario.getId(), PageRequest.of(page, size)));
    }

    @Override
    @Transactional
    public GuardadoResponse guardar(Long id, String correo) {
        Publicacion publicacion = buscarPublicacion(id);
        Usuario usuario = buscarUsuario(correo);
        // Idempotente, igual que el "me gusta".
        if (guardadoRepository.findByUsuarioIdAndPublicacionId(usuario.getId(), id).isEmpty()) {
            guardadoRepository.save(Guardado.builder()
                    .usuario(usuario)
                    .publicacion(publicacion)
                    .fechaCreacion(LocalDateTime.now())
                    .build());
        }
        return new GuardadoResponse(true);
    }

    @Override
    @Transactional
    public GuardadoResponse quitarGuardado(Long id, String correo) {
        buscarPublicacion(id);
        Usuario usuario = buscarUsuario(correo);
        guardadoRepository.findByUsuarioIdAndPublicacionId(usuario.getId(), id)
                .ifPresent(guardadoRepository::delete);
        return new GuardadoResponse(false);
    }

    @Override
    @Transactional
    public void eliminarPublicacion(Long id, String correoUsuario) {
        Publicacion publicacion = buscarPublicacion(id);
        verificarPermiso(publicacion, buscarUsuario(correoUsuario), "No tienes permisos para eliminar esta publicación");

        // Eliminar todas las fotos del almacenamiento físico y luego el registro
        List<String> rutas = new ArrayList<>(publicacion.getImagenes().stream().map(PublicacionImagen::getRuta).toList());
        if (!rutas.contains(publicacion.getImagen())) rutas.add(publicacion.getImagen());
        publicacionRepository.delete(publicacion);
        rutas.forEach(storageService::eliminarArchivo);
    }

    // ---------------------------------------------------------------------------------------

    private static final int MAX_IMAGENES = 10;
    private static final String PREFIJO_NUEVA = "nueva:";

    private void validarCantidad(int cantidad) {
        if (cantidad < 1) {
            throw new ArchivoInvalidoException("La publicación necesita al menos una foto");
        }
        if (cantidad > MAX_IMAGENES) {
            throw new ArchivoInvalidoException("Máximo " + MAX_IMAGENES + " fotos por publicación");
        }
    }

    /** Sustituye la lista de fotos por {@code rutas}, en ese orden; la primera es la portada. */
    private void ponerImagenes(Publicacion publicacion, List<String> rutas) {
        publicacion.getImagenes().clear();
        for (int i = 0; i < rutas.size(); i++) {
            publicacion.getImagenes().add(PublicacionImagen.builder()
                    .publicacion(publicacion)
                    .ruta(rutas.get(i))
                    .orden(i)
                    .build());
        }
        publicacion.setImagen(rutas.get(0));
    }

    private static int indiceNueva(String elemento) {
        try {
            return Integer.parseInt(elemento.substring(PREFIJO_NUEVA.length()));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static Long idExistente(String elemento) {
        try {
            return Long.parseLong(elemento);
        } catch (NumberFormatException e) {
            throw new ArchivoInvalidoException("Elemento de orden inválido: " + elemento);
        }
    }

    // ---------------------------------------------------------------------------------------

    /** Copia los datos editables. Latitud y longitud solo se guardan si llegan las dos. */
    private void aplicarDatos(Publicacion publicacion, PublicacionRequest request) {
        publicacion.setTitulo(request.titulo().trim());
        publicacion.setDescripcion(request.descripcion().trim());
        publicacion.setCategoria(buscarCategoria(request.categoria()));

        boolean conCoordenadas = request.latitud() != null && request.longitud() != null;
        publicacion.setLatitud(conCoordenadas ? request.latitud() : null);
        publicacion.setLongitud(conCoordenadas ? request.longitud() : null);
        publicacion.setDireccion(
                request.direccion() != null && !request.direccion().isBlank() ? request.direccion().trim() : null);
    }

    /**
     * Busca la categoría por nombre (sin el "#" inicial) y, si no existe, la crea: es como
     * funciona la generación automática de hashtags de la web (SUG-20).
     */
    private Categoria buscarCategoria(String nombre) {
        if (nombre == null || nombre.isBlank()) return null;
        String limpio = nombre.trim().replaceFirst("^#", "").trim();
        if (limpio.isBlank()) return null;
        return categoriaRepository.findByNombreIgnoreCase(limpio)
                .orElseGet(() -> categoriaRepository.save(Categoria.builder().nombre(limpio).build()));
    }

    private void verificarPermiso(Publicacion publicacion, Usuario usuario, String mensaje) {
        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(rol -> rol.getNombre().equalsIgnoreCase("ADMIN"));
        boolean esPropietario = publicacion.getUsuario().getId().equals(usuario.getId());
        if (!esAdmin && !esPropietario) {
            throw new AccesoNoAutorizadoException(mensaje);
        }
    }

    /**
     * Cada palabra debe aparecer en el título, la descripción, la dirección, el autor o la
     * categoría. La intercalación utf8mb4_unicode_ci de MySQL ya ignora tildes y mayúsculas.
     */
    private Specification<Publicacion> contieneTodasLasPalabras(String texto) {
        List<String> palabras = Arrays.stream(texto.trim().split("\\s+"))
                .filter(p -> !p.isBlank())
                .limit(6)
                .toList();

        return (root, query, cb) -> {
            Join<Object, Object> autor = root.join("usuario");
            Join<Object, Object> categoria = root.join("categoria", JoinType.LEFT);
            Predicate[] porPalabra = palabras.stream().map(palabra -> {
                String patron = "%" + escaparLike(palabra) + "%";
                return cb.or(
                        cb.like(root.<String>get("titulo"), patron, '\\'),
                        cb.like(root.<String>get("descripcion"), patron, '\\'),
                        cb.like(root.<String>get("direccion"), patron, '\\'),
                        cb.like(autor.<String>get("nombre"), patron, '\\'),
                        cb.like(categoria.<String>get("nombre"), patron, '\\')
                );
            }).toArray(Predicate[]::new);
            return cb.and(cb.isFalse(root.<Boolean>get("oculta")), cb.and(porPalabra));
        };
    }

    private static String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private PublicacionResponse aRespuesta(Publicacion publicacion) {
        return aRespuestas(List.of(publicacion)).get(0);
    }

    private Page<PublicacionResponse> aRespuestas(Page<Publicacion> pagina) {
        Interacciones i = interaccionesDe(pagina.getContent());
        return pagina.map(p -> conInteraccion(p, i));
    }

    private List<PublicacionResponse> aRespuestas(List<Publicacion> publicaciones) {
        Interacciones i = interaccionesDe(publicaciones);
        return publicaciones.stream().map(p -> conInteraccion(p, i)).toList();
    }

    private PublicacionResponse conInteraccion(Publicacion p, Interacciones i) {
        return publicacionMapper.toResponse(p)
                .conInteraccion(i.meGustan().contains(p.getId()), i.guardadas().contains(p.getId()));
    }

    /** Lo que le gusta y lo que guardó quien pregunta, de toda una página. */
    private record Interacciones(Set<Long> meGustan, Set<Long> guardadas) {}

    /** Dos consultas por página, no por publicación. Sin sesión, todo vacío. */
    private Interacciones interaccionesDe(Collection<Publicacion> publicaciones) {
        if (publicaciones.isEmpty()) return new Interacciones(Set.of(), Set.of());
        return usuarioActual.id()
                .map(uid -> {
                    List<Long> ids = publicaciones.stream().map(Publicacion::getId).toList();
                    return new Interacciones(
                            meGustaRepository.findIdsConMeGusta(uid, ids),
                            guardadoRepository.findIdsGuardados(uid, ids));
                })
                .orElse(new Interacciones(Set.of(), Set.of()));
    }

    private Publicacion buscarPublicacion(Long id) {
        return publicacionRepository.findById(id)
                .orElseThrow(() -> new PublicacionNoEncontradaException("Publicación no encontrada"));
    }

    private Usuario buscarUsuario(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
    }
}
