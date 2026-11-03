package com.TuraTrip.backend.services;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.TuraTrip.backend.dtos.request.PublicacionRequest;
import com.TuraTrip.backend.dtos.response.PublicacionResponse;
import com.TuraTrip.backend.exceptions.AccesoNoAutorizadoException;
import com.TuraTrip.backend.exceptions.PublicacionNoEncontradaException;
import com.TuraTrip.backend.exceptions.UsuarioNoEncontradoException;
import com.TuraTrip.backend.mappers.PublicacionMapper;
import com.TuraTrip.backend.models.Categoria;
import com.TuraTrip.backend.models.Publicacion;
import com.TuraTrip.backend.models.Usuario;
import com.TuraTrip.backend.repositories.CategoriaRepository;
import com.TuraTrip.backend.repositories.PublicacionRepository;
import com.TuraTrip.backend.repositories.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicacionServiceImpl implements PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final PublicacionMapper publicacionMapper;
    private final StorageService storageService;

    @Override
    @Transactional
    public PublicacionResponse crearPublicacion(String correo, PublicacionRequest request, MultipartFile archivo) {

        // 1. Validar que el usuario exista
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        // 2. Guardar archivo utilizando el StorageService (devuelve la ruta relativa: ej. "publicaciones/uuid.jpg")
        String rutaRelativa = storageService.guardarArchivo(archivo, "publicaciones");

        // 3. Construir y guardar la publicación con la ruta relativa
        Categoria categoria = resolverCategoria(request.categoria());

        Publicacion publicacion = Publicacion.builder()
                .titulo(request.titulo())
                .descripcion(request.descripcion())
                .imagen(rutaRelativa)
                .fechaCreacion(LocalDateTime.now())
                .usuario(usuario)
                .categoria(categoria)
                .direccion(request.direccion())
                .latitud(request.latitud())
                .longitud(request.longitud())
                .build();

        Publicacion guardada = publicacionRepository.save(publicacion);

        return publicacionMapper.toResponse(guardada);
    }

    @Override
    @Transactional
    public PublicacionResponse editarPublicacion(Long id, String correo, PublicacionRequest request, MultipartFile archivo) {
        Publicacion publicacion = publicacionRepository.findById(id)
                .orElseThrow(() -> new PublicacionNoEncontradaException("La publicación no existe"));

        Usuario usuarioActual = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        // A diferencia de eliminar, un administrador no puede editar publicaciones ajenas
        boolean esPropietario = publicacion.getUsuario().getId().equals(usuarioActual.getId());

        if (!esPropietario) {
            throw new AccesoNoAutorizadoException("No tienes permisos para editar esta publicación");
        }

        publicacion.setTitulo(request.titulo());
        publicacion.setDescripcion(request.descripcion());
        publicacion.setCategoria(resolverCategoria(request.categoria()));
        publicacion.setDireccion(request.direccion());
        publicacion.setLatitud(request.latitud());
        publicacion.setLongitud(request.longitud());

        if (archivo != null && !archivo.isEmpty()) {
            String imagenAnterior = publicacion.getImagen();
            String imagenNueva = storageService.guardarArchivo(archivo, "publicaciones");
            publicacion.setImagen(imagenNueva);
            limpiarImagenAlTerminar(imagenAnterior, imagenNueva);
        }

        return publicacionMapper.toResponse(publicacion);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicacionResponse> obtenerTodasLasPublicaciones(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
        return publicacionRepository.findAll(pageable)
                .map(publicacionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicacionResponse> obtenerPublicacionesPorUsuarioId(Long usuarioId, int page, int size) {
        // Validar primero si el usuario existe para lanzar la excepción correspondiente si no se halla
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new UsuarioNoEncontradoException("Usuario no encontrado");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
        return publicacionRepository.findAllByUsuarioId(usuarioId, pageable)
                .map(publicacionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicacionResponse> obtenerPublicacionesPorCorreoUsuario(String correo, int page, int size) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
        return publicacionRepository.findAllByUsuario(usuario, pageable)
                .map(publicacionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PublicacionResponse obtenerPublicacionPorId(Long id) {
        Publicacion publicacion = publicacionRepository.findById(id)
                .orElseThrow(() -> new PublicacionNoEncontradaException("Publicación no encontrada"));

        return publicacionMapper.toResponse(publicacion);
    }

    @Override
    @Transactional
    public void eliminarPublicacion(Long id, String correoUsuario) {
        // 1. Buscar la publicación
        Publicacion publicacion = publicacionRepository.findById(id)
                .orElseThrow(() -> new PublicacionNoEncontradaException("La publicación no existe"));

        // 2. Buscar al usuario autenticado
        Usuario usuarioActual = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        // 3. Verificar si el usuario es ADMIN o si es el propietario de la publicación
        boolean esAdmin = usuarioActual.getRoles().stream()
                .anyMatch(rol -> rol.getNombre().equalsIgnoreCase("ADMIN"));

        boolean esPropietario = publicacion.getUsuario().getId().equals(usuarioActual.getId());

        if (!esAdmin && !esPropietario) {
            throw new AccesoNoAutorizadoException("No tienes permisos para eliminar esta publicación");
        }

        // 4. Eliminar el archivo del almacenamiento físico
        storageService.eliminarArchivo(publicacion.getImagen());

        // 5. Eliminar el registro en la base de datos
        publicacionRepository.delete(publicacion);
    }

    /** Busca la categoría por nombre (sin el "#" inicial) y la crea si no existe. Vacía equivale a sin categoría. */
    private Categoria resolverCategoria(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }

        String nombreCategoria = nombre.trim();
        if (nombreCategoria.startsWith("#")) {
            nombreCategoria = nombreCategoria.substring(1).trim();
        }

        final String nombreFinal = nombreCategoria; // Variable inmutable para la lambda

        if (nombreFinal.isBlank()) {
            return null;
        }

        return categoriaRepository.findByNombreIgnoreCase(nombreFinal)
                .orElseGet(() -> categoriaRepository.save(Categoria.builder().nombre(nombreFinal).build()));
    }

    /**
     * El disco no participa en la transacción: la foto anterior solo se borra si el cambio se
     * confirmó y, si se revierte, se borra la nueva para que la publicación no pierda su imagen
     * ni queden archivos huérfanos.
     */
    private void limpiarImagenAlTerminar(String imagenAnterior, String imagenNueva) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                storageService.eliminarArchivo(status == STATUS_COMMITTED ? imagenAnterior : imagenNueva);
            }
        });
    }
}
