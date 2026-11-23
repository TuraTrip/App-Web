-- 9. Suscripción Premium (perfil verificado).
-- Por ahora se activa sin cobro (modo demostración); el usuario es Premium mientras
-- premium_hasta esté en el futuro.
ALTER TABLE usuarios
    ADD COLUMN premium_hasta DATETIME(6) NULL;
