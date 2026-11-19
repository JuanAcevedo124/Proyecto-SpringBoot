package com.coworkhub.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.ZoneId;

/**
 * Valida al arrancar que la configuracion propia sea coherente. Si no lo es, la aplicacion no arranca.
 */
@Component
public class ValidadorConfiguracion {

    private static final Logger log = LoggerFactory.getLogger(ValidadorConfiguracion.class);

    private final String zonaHoraria;

    public ValidadorConfiguracion(@Value("${app.zona-horaria:}") String zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
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
        log.info("Configuracion validada. Zona horaria: {}",
                zonaHoraria == null || zonaHoraria.isBlank() ? ZoneId.systemDefault() : zonaHoraria);
    }
}
