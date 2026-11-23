package com.TuraTrip.backend.services;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.TuraTrip.backend.models.Usuario;
import com.TuraTrip.backend.repositories.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Usuario que hace la petición, si envió un token válido.
 *
 * JwtTokenFilter rellena el contexto de seguridad también en las rutas públicas cuando llega
 * el token, así que aquí se puede saber, por ejemplo, si al visitante le gusta una publicación
 * sin obligar a iniciar sesión para ver el feed.
 */
@Component
@RequiredArgsConstructor
public class UsuarioActual {

    private final UsuarioRepository usuarioRepository;

    public Optional<Usuario> obtener() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return usuarioRepository.findByCorreo(auth.getName());
    }

    public Optional<Long> id() {
        return obtener().map(Usuario::getId);
    }
}
