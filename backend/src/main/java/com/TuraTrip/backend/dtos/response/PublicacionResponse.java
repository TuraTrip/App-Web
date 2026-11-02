package com.TuraTrip.backend.dtos.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Publicación tal y como la ven los clientes.
 *
 * {@code meGusta} dice si al usuario de la petición le gusta; sin sesión siempre es {@code false}.
 */
public record PublicacionResponse(
    Long id,
    String titulo,
    String descripcion,
    /** Portada (la primera foto). Se mantiene para los clientes que solo muestran una. */
    String imagen,
    /** Todas las fotos en orden, portada incluida (máx. 10). */
    List<ImagenResponse> imagenes,
    LocalDateTime fechaCreacion,
    String autorNombre,
    Long autorId,
    String autorFotoPerfil,
    /** El autor tiene Premium vigente: se muestra el check de verificado. */
    boolean autorVerificado,
    Long categoriaId,
    String categoriaNombre,
    String direccion,
    Double latitud,
    Double longitud,
    long totalMeGusta,
    boolean meGusta,
    /** Si el usuario de la petición la guardó (privado; sin sesión siempre es false). */
    boolean guardado,
    /** Oculta por denuncias, en revisión. Solo la ven su autor y los administradores. */
    boolean oculta,
    List<ComentarioResponse> comentarios
) {

    /** Completa lo que depende de quién pregunta: su "me gusta" y si la guardó. */
    public PublicacionResponse conInteraccion(boolean meGusta, boolean guardado) {
        return new PublicacionResponse(
            id, titulo, descripcion, imagen, imagenes, fechaCreacion, autorNombre, autorId, autorFotoPerfil, autorVerificado,
            categoriaId, categoriaNombre, direccion, latitud, longitud, totalMeGusta, meGusta, guardado, oculta, comentarios
        );
    }
}
