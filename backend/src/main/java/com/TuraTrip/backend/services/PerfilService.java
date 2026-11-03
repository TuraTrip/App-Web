package com.TuraTrip.backend.services;

import java.util.List;

import com.TuraTrip.backend.dtos.request.EditarPerfilRequest;
import com.TuraTrip.backend.dtos.response.PerfilResponse;
import com.TuraTrip.backend.dtos.response.UsuarioResumenResponse;

public interface PerfilService {

    PerfilResponse obtenerPerfil(String correo);

    PerfilResponse obtenerPerfilPorId(Long id);

    PerfilResponse editarPerfil(
        String correo,
        EditarPerfilRequest request
    );

    PerfilResponse seguir(Long id, String correo);

    PerfilResponse dejarDeSeguir(Long id, String correo);

    /** Premium / perfil verificado. Modo demostración: sin cobro. */
    PerfilResponse activarPremium(String correo);

    PerfilResponse cancelarPremium(String correo);

    List<UsuarioResumenResponse> buscar(String texto);

    List<UsuarioResumenResponse> obtenerSeguidores(Long id);

    List<UsuarioResumenResponse> obtenerSeguidos(Long id);
}
