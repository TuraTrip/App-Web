package com.TuraTrip.backend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.TuraTrip.backend.dtos.response.ComentarioResponse;
import com.TuraTrip.backend.models.Comentario;
import com.TuraTrip.backend.services.StorageService;

@Mapper(componentModel = "spring", uses = {StorageService.class})
public interface ComentarioMapper {

    @Mapping(source = "usuario.id", target = "autorId")
    @Mapping(source = "usuario.nombre", target = "autorNombre")
    @Mapping(
        target = "autorFotoPerfil",
        expression = "java(comentario.getUsuario() != null && comentario.getUsuario().getFotoPerfil() != null ? storageService.construirUrlPublica(comentario.getUsuario().getFotoPerfil()) : null)"
    )
    @Mapping(source = "publicacion.id", target = "publicacionId")
    @Mapping(source = "publicacion.titulo", target = "publicacionTitulo")
    @Mapping(
        target = "autorVerificado",
        expression = "java(comentario.getUsuario() != null && comentario.getUsuario().isVerificado())"
    )
    ComentarioResponse toResponse(Comentario comentario);

}
