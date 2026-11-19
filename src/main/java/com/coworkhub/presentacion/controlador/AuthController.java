package com.coworkhub.presentacion.controlador;

import com.coworkhub.jwt.UsuarioAutenticado;
import com.coworkhub.presentacion.dto.AuthDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/perfil")
    public AuthDto.PerfilResponse perfil(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return new AuthDto.PerfilResponse(usuario.username(), usuario.rol(), usuario.miembroId());
    }
}
