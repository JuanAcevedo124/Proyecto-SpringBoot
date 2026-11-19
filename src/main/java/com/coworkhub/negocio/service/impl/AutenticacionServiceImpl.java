package com.coworkhub.negocio.service.impl;

import com.coworkhub.jwt.JwtService;
import com.coworkhub.jwt.UsuarioAutenticado;
import com.coworkhub.negocio.service.AutenticacionService;
import com.coworkhub.presentacion.dto.AuthDto;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AutenticacionServiceImpl implements AutenticacionService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AutenticacionServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /** Credenciales invalidas: el AuthenticationManager lanza BadCredentialsException y el advice responde 401. */
    @Override
    public AuthDto.LoginResponse login(AuthDto.LoginRequest r) {
        Authentication auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(r.usuario(), r.contrasena()));
        UsuarioAutenticado usuario = (UsuarioAutenticado) auth.getPrincipal();
        return new AuthDto.LoginResponse(jwtService.generar(usuario), "Bearer", jwtService.vigenciaEnSegundos(),
                usuario.username(), usuario.rol(), usuario.miembroId());
    }
}
