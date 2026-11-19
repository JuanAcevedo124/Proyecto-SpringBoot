package com.coworkhub.jwt;

import com.coworkhub.negocio.model.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Principal de Spring Security: usuario, rol y miembroId (null para ADMIN y RECEPTIONIST).
 * Al autenticar con usuario/contrasena lleva el hash; al reconstruirse desde un JWT, la contrasena es null.
 */
public record UsuarioAutenticado(String username, String password, Role rol, Long miembroId) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
