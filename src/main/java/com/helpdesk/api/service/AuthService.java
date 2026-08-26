package com.helpdesk.api.service;

import com.helpdesk.api.dto.*;
import com.helpdesk.api.model.*;
import com.helpdesk.api.repository.*;
import com.helpdesk.api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public UsuarioResponse registro(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.USUARIO)
                .build();

        usuarioRepository.save(usuario);

        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String accessToken = tokenProvider.generateAccessToken(usuario.getEmail(), usuario.getRol().name());
        String refreshToken = createRefreshToken(usuario);

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token no válido"));

        if (!refreshToken.isValido()) {
            throw new RuntimeException("Refresh token no válido");
        }

        Usuario usuario = refreshToken.getUsuario();
        String newAccessToken = tokenProvider.generateAccessToken(usuario.getEmail(), usuario.getRol().name());

        // Rotar refresh token
        refreshToken.setRevocado(true);
        refreshTokenRepository.save(refreshToken);

        String newRefreshToken = createRefreshToken(usuario);

        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    public void logout(Usuario usuario) {
        refreshTokenRepository.deleteByUsuarioId(usuario.getId());
    }

    private String createRefreshToken(Usuario usuario) {
        String tokenValue = tokenProvider.generateRefreshToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .usuario(usuario)
                .expiraEn(LocalDateTime.now().plusDays(7))
                .revocado(false)
                .build();
        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }
}
