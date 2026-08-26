package com.helpdesk.api.service;

import com.helpdesk.api.dto.UsuarioResponse;
import com.helpdesk.api.model.Rol;
import com.helpdesk.api.model.Usuario;
import com.helpdesk.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioResponse ascenderASoporte(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setRol(Rol.SOPORTE);
        usuarioRepository.save(usuario);

        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .build();
    }
}
