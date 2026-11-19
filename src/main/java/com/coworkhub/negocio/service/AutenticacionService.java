package com.coworkhub.negocio.service;

import com.coworkhub.presentacion.dto.AuthDto;

/** RF-11: inicio de sesion con usuario y contrasena. */
public interface AutenticacionService {

    /** Valida las credenciales y devuelve un JWT que vence en 60 minutos. */
    AuthDto.LoginResponse login(AuthDto.LoginRequest request);
}
