package br.com.vicino.api.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.vicino.api.dto.LoginRequest;
import br.com.vicino.api.dto.LoginResponse;
import br.com.vicino.api.model.Usuario;
import br.com.vicino.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();

        Usuario usuario = 
        usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null || !usuario.getAtivo() || !passwordEncoder.matches(req.senha(), usuario.getSenhaHash())){
            throw new ResponseStatusException
            (HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
        }
        
        Jwt jwt = tokenService.gerar(usuario);
        return new LoginResponse(jwt.getTokenValue(), "Bearer", jwt.getExpiresAt());
    }
}