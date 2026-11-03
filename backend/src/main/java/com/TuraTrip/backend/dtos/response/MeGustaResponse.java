package com.TuraTrip.backend.dtos.response;

/** Estado del "me gusta" tras darlo o quitarlo. */
public record MeGustaResponse(
    boolean meGusta,
    long totalMeGusta
) {}
