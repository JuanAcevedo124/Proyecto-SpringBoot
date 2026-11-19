package com.coworkhub.jwt;

import com.coworkhub.presentacion.excepcion.ManejadoresSeguridad;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lee "Authorization: Bearer <token>" en cada solicitud. Si el token es valido, autentica la solicitud;
 * si no, deja el motivo en un atributo para que el AuthenticationEntryPoint responda 401.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            String token = cabecera.substring(PREFIJO.length()).trim();
            try {
                UsuarioAutenticado usuario = jwtService.validar(token);
                var auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (ExpiredJwtException e) {
                SecurityContextHolder.clearContext();
                request.setAttribute(ManejadoresSeguridad.ATRIBUTO_ERROR_TOKEN, "El token esta vencido");
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
                request.setAttribute(ManejadoresSeguridad.ATRIBUTO_ERROR_TOKEN, "El token es invalido o fue alterado");
            }
        }
        chain.doFilter(request, response);
    }
}
