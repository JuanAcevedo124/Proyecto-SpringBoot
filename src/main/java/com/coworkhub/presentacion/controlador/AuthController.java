package com.coworkhub.presentacion.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.coworkhub.jwt.UsuarioAutenticado;
import com.coworkhub.negocio.service.AutenticacionService;
import com.coworkhub.presentacion.dto.AuthDto;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticacion", description = "Inicio de sesion con JWT (RF-11, RF-14)")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AutenticacionService autenticacion;

    public AuthController(AutenticacionService autenticacion) {
        this.autenticacion = autenticacion;
    }

    @Operation(summary = "Inicia sesion y devuelve un JWT", description = "Publico. Devuelve un token Bearer que vence en 60 minutos. Usuarios de prueba: admin/admin123, recepcion/recep123, ana/ana123, luis/luis123.")
    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @PostMapping("/login")
    public AuthDto.LoginResponse login(@Valid @RequestBody AuthDto.LoginRequest request) {
        return autenticacion.login(request);
    }

    @Operation(summary = "Datos del usuario autenticado", description = "Acceso: autenticado. Devuelve usuario, rol y miembroId (null para ADMIN y RECEPTIONIST).")
    @GetMapping("/perfil")
    public AuthDto.PerfilResponse perfil(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return new AuthDto.PerfilResponse(usuario.username(), usuario.rol(), usuario.miembroId());
    }
}
