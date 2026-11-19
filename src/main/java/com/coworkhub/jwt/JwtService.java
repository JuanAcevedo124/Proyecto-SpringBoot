package com.coworkhub.jwt;

import com.coworkhub.negocio.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Emite y valida los JWT (HS256). El token lleva el usuario (sub), el rol y el miembroId,
 * asi que el filtro reconstruye el usuario sin consultar la base en cada solicitud (stateless).
 */
@Component
@DependsOn("validadorConfiguracion")
public class JwtService {

    private static final String CLAIM_ROL = "rol";
    private static final String CLAIM_MIEMBRO = "miembroId";

    private final SecretKey clave;
    private final Duration vigencia;
    private final Clock reloj;

    public JwtService(@Value("${app.jwt.secreto}") String secreto,
                      @Value("${app.jwt.expiracion-minutos:60}") long minutos,
                      Clock reloj) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.vigencia = Duration.ofMinutes(minutos);
        this.reloj = reloj;
    }

    public String generar(UsuarioAutenticado usuario) {
        Instant ahora = Instant.now(reloj);
        var builder = Jwts.builder()
                .subject(usuario.username())
                .claim(CLAIM_ROL, usuario.rol().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(vigencia)));
        if (usuario.miembroId() != null) {
            builder.claim(CLAIM_MIEMBRO, usuario.miembroId());
        }
        return builder.signWith(clave).compact();
    }

    /**
     * Valida firma y vencimiento y devuelve el usuario del token.
     *
     * @throws JwtException si el token fue alterado, esta vencido o es invalido
     */
    public UsuarioAutenticado validar(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(clave)
                .clock(() -> Date.from(Instant.now(reloj)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Number miembro = claims.get(CLAIM_MIEMBRO, Number.class);
        Role rol;
        try {
            rol = Role.valueOf(claims.get(CLAIM_ROL, String.class));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new JwtException("Rol invalido en el token");
        }
        return new UsuarioAutenticado(claims.getSubject(), null, rol, miembro == null ? null : miembro.longValue());
    }

    public long vigenciaEnSegundos() {
        return vigencia.toSeconds();
    }
}
