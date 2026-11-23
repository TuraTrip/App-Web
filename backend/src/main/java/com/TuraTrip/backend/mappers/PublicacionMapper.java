package com.TuraTrip.backend.mappers;

import com.TuraTrip.backend.dtos.response.ComentarioResponse;
import com.TuraTrip.backend.dtos.response.ImagenResponse;
import com.TuraTrip.backend.models.PublicacionImagen;
import com.TuraTrip.backend.dtos.response.PublicacionResponse;
import com.TuraTrip.backend.models.Publicacion;
import com.TuraTrip.backend.services.StorageService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {StorageService.class, ComentarioMapper.class})
public interface PublicacionMapper {

    @Mapping(source = "usuario.nombre", target = "autorNombre")
    @Mapping(source = "usuario.id", target = "autorId")
    @Mapping(target = "autorFotoPerfil", expression = "java(publicacion.getUsuario() != null && publicacion.getUsuario().getFotoPerfil() != null ? storageService.construirUrlPublica(publicacion.getUsuario().getFotoPerfil()) : null)")
    @Mapping(target = "autorVerificado", expression = "java(publicacion.getUsuario() != null && publicacion.getUsuario().isVerificado())")
    @Mapping(target = "imagen", expression = "java(storageService.construirUrlPublica(publicacion.getImagen()))")
    @Mapping(target = "categoriaId", expression = "java(publicacion.getCategoria() != null ? publicacion.getCategoria().getId() : null)")
    @Mapping(target = "categoriaNombre", expression = "java(publicacion.getCategoria() != null ? publicacion.getCategoria().getNombre() : null)")
    // Recién creada, la fórmula aún no se ha evaluado y llega en null.
    @Mapping(target = "totalMeGusta", expression = "java(publicacion.getTotalMeGusta() != null ? publicacion.getTotalMeGusta() : 0L)")
    // Dependen de quién pregunta: los completa PublicacionServiceImpl con conInteraccion().
    @Mapping(target = "meGusta", constant = "false")
    @Mapping(target = "guardado", constant = "false")
    @Mapping(target = "oculta", expression = "java(Boolean.TRUE.equals(publicacion.getOculta()))")
    PublicacionResponse toResponse(Publicacion publicacion);

    /** MapStruct lo usa para convertir la lista `imagenes` de cada publicación. */
    @Mapping(target = "url", expression = "java(storageService.construirUrlPublica(imagen.getRuta()))")
    ImagenResponse toImagenResponse(PublicacionImagen imagen);

}
