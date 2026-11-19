package com.coworkhub.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.ZoneId;

/**
 * Valida al arrancar que la configuracion propia sea coherente. Si no lo es, la aplicacion no arranca
 * y el mensaje explica que corregir.
 */
@Component("validadorConfiguracion")
public class ValidadorConfiguracion {

    private static final Logger log = LoggerFactory.getLogger(ValidadorConfiguracion.class);
    static final int BYTES_MINIMOS_CLAVE = 32; // HS256 exige claves de al menos 256 bits

    private final String zonaHoraria;
    private final String secretoJwt;
    private final long expiracionMinutos;

    public ValidadorConfiguracion(@Value("${app.zona-horaria:}") String zonaHoraria,
                                  @Value("${app.jwt.secreto:}") String secretoJwt,
                                  @Value("${app.jwt.expiracion-minutos:60}") long expiracionMinutos) {
        this.zonaHoraria = zonaHoraria;
        this.secretoJwt = secretoJwt;
        this.expiracionMinutos = expiracionMinutos;
    }

    @PostConstruct
    void validar() {
        if (zonaHoraria != null && !zonaHoraria.isBlank()) {
            try {
                ZoneId.of(zonaHoraria);
            } catch (DateTimeException e) {
                throw new IllegalStateException("app.zona-horaria no es una zona valida: " + zonaHoraria, e);
            }
        }
        if (secretoJwt == null || secretoJwt.getBytes(StandardCharsets.UTF_8).length < BYTES_MINIMOS_CLAVE) {
            throw new IllegalStateException("app.jwt.secreto debe tener al menos " + BYTES_MINIMOS_CLAVE + " caracteres");
        }
        if (expiracionMinutos <= 0) {
            throw new IllegalStateException("app.jwt.expiracion-minutos debe ser mayor que 0");
        }
        log.info("Configuracion validada. Zona horaria: {}. Vigencia del JWT: {} min",
                zonaHoraria == null || zonaHoraria.isBlank() ? ZoneId.systemDefault() : zonaHoraria,
                expiracionMinutos);
    }
}
