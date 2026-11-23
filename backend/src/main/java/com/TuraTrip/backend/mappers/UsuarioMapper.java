package com.TuraTrip.backend.mappers;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.TuraTrip.backend.dtos.response.PerfilResponse;
import com.TuraTrip.backend.dtos.response.RolResponse;
import com.TuraTrip.backend.dtos.response.UsuarioResponse;
import com.TuraTrip.backend.dtos.response.UsuarioResumenResponse;
import com.TuraTrip.backend.models.Rol;
import com.TuraTrip.backend.models.Usuario;
import com.TuraTrip.backend.services.StorageService;

@Mapper(componentModel = "spring", uses = {StorageService.class})
public interface UsuarioMapper {

    @Mapping(target = "fotoPerfil", expression = "java(storageService.construirUrlPublica(usuario.getFotoPerfil()))")
    UsuarioResponse toResponse(Usuario usuario);

    @Mapping(target = "roles", expression = "java(mapRolesAStrings(usuario.getRoles()))")
    @Mapping(
        target = "fotoPerfil",
        expression = "java(storageService.construirUrlPublica(usuario.getFotoPerfil()))"
    )
    // Las estadísticas dependen de otras tablas y de quién pregunta: las completa PerfilServiceImpl.
    @Mapping(target = "totalPublicaciones", ignore = true)
    @Mapping(target = "seguidores", ignore = true)
    @Mapping(target = "siguiendo", ignore = true)
    @Mapping(target = "loSigo", ignore = true)
    PerfilResponse toPerfilResponse(Usuario usuario);

    @Mapping(target = "fotoPerfil", expression = "java(storageService.construirUrlPublica(usuario.getFotoPerfil()))")
    UsuarioResumenResponse toResumen(Usuario usuario);

    default RolResponse rolToRolResponse(Rol rol) {
        if (rol == null) {
            return null;
        }
        return new RolResponse(rol.getId(), rol.getNombre(), rol.getDescripcion());
    }

    default Set<String> mapRolesAStrings(Set<Rol> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .filter(Objects::nonNull)
                .map(rol -> rol.getNombre())
                .collect(Collectors.toSet());
    }
}
