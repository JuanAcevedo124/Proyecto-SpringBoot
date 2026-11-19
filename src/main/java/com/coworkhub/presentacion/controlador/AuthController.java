package com.coworkhub.presentacion.controlador;

import com.coworkhub.jwt.UsuarioAutenticado;
import com.coworkhub.negocio.service.AutenticacionService;
import com.coworkhub.presentacion.dto.AuthDto;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AutenticacionService autenticacion;

    public AuthController(AutenticacionService autenticacion) {
        this.autenticacion = autenticacion;
    }

    @PostMapping("/login")
    public AuthDto.LoginResponse login(@Valid @RequestBody AuthDto.LoginRequest request) {
        return autenticacion.login(request);
    }

    @GetMapping("/perfil")
    public AuthDto.PerfilResponse perfil(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return new AuthDto.PerfilResponse(usuario.username(), usuario.rol(), usuario.miembroId());
    }
}
