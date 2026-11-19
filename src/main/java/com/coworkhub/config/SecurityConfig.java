package com.coworkhub.config;

import com.coworkhub.presentacion.excepcion.ManejadoresSeguridad;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad - etapa 1: HTTP Basic contra las cuentas de user_accounts (BCrypt), sin sesion.
 * Los permisos por rol y por duenio se declaran con @PreAuthorize en los controladores.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    static final String[] RUTAS_PUBLICAS = {
            "/auth/login", "/actuator/health", "/actuator/health/**", "/actuator/info",
            "/doc/**", "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/error"
    };

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http, ManejadoresSeguridad manejadores) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(b -> b.authenticationEntryPoint(manejadores))
                .exceptionHandling(e -> e.authenticationEntryPoint(manejadores).accessDeniedHandler(manejadores))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(RUTAS_PUBLICAS).permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
