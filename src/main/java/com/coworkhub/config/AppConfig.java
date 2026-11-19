package com.coworkhub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Beans transversales de la aplicacion.
 * <ul>
 *   <li>{@code relojAplicacion} (@Primary): la "hora actual" del negocio. Se inyecta en lugar de
 *   llamar a {@code LocalDateTime.now()} para poder fijar la hora en las pruebas.</li>
 *   <li>{@code relojUtc}: se usa con {@code @Qualifier} para los timestamps de los errores.</li>
 *   <li>{@code PasswordEncoder}: BCrypt para las contrasenas.</li>
 * </ul>
 */
@Configuration
public class AppConfig {

    @Bean
    @Primary
    public Clock relojAplicacion(@Value("${app.zona-horaria:}") String zona) {
        return (zona == null || zona.isBlank())
                ? Clock.systemDefaultZone()
                : Clock.system(ZoneId.of(zona));
    }

    @Bean
    public Clock relojUtc() {
        return Clock.systemUTC();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
