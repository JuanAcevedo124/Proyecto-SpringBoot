package com.coworkhub.config;

import com.coworkhub.jwt.JwtAuthenticationFilter;
import com.coworkhub.presentacion.excepcion.ManejadoresSeguridad;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Seguridad - etapa 2: JWT stateless. POST /auth/login valida usuario y contrasena (BCrypt) y entrega un token;
 * el {@link JwtAuthenticationFilter} lo valida en cada solicitud. No hay sesion ni Basic Auth.
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
    public SecurityFilterChain filtros(HttpSecurity http, JwtAuthenticationFilter filtroJwt,
                                       ManejadoresSeguridad manejadores) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(manejadores).accessDeniedHandler(manejadores))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(RUTAS_PUBLICAS).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** El filtro JWT es un @Component: se evita que Spring Boot lo registre ademas como filtro de servlet. */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> noRegistrarFiltroJwt(JwtAuthenticationFilter filtro) {
        FilterRegistrationBean<JwtAuthenticationFilter> registro = new FilterRegistrationBean<>(filtro);
        registro.setEnabled(false);
        return registro;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService usuarios, PasswordEncoder encoder) {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider(usuarios);
        proveedor.setPasswordEncoder(encoder);
        return new ProviderManager(proveedor);
    }
}
