package com.coworkhub.negocio.service.impl;

import com.coworkhub.jwt.UsuarioAutenticado;
import com.coworkhub.negocio.model.UserAccount;
import com.coworkhub.persistence.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Carga la cuenta desde user_accounts para que Spring Security verifique la contrasena BCrypt. */
@Service
public class UsuarioDetallesServiceImpl implements UserDetailsService {

    private final UserAccountRepository cuentas;

    public UsuarioDetallesServiceImpl(UserAccountRepository cuentas) {
        this.cuentas = cuentas;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        UserAccount cuenta = cuentas.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        Long miembroId = cuenta.getMember() == null ? null : cuenta.getMember().getId();
        return new UsuarioAutenticado(cuenta.getUsername(), cuenta.getPassword(), cuenta.getRole(), miembroId);
    }
}
