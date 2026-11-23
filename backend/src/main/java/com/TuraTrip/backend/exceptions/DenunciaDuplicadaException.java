package com.TuraTrip.backend.exceptions;

/** El usuario ya denunció ese contenido. Se responde 409. */
public class DenunciaDuplicadaException extends RuntimeException {
    public DenunciaDuplicadaException(String message) {
        super(message);
    }
}
