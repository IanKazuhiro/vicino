package br.com.vicino.api.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.com.vicino.api.model.Usuario;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class TokenService {
    private final JwtEncoder jwtEncoder;

    @Value("${vicino.jwt.expiracao-minutos}")
    private long expiracaoMinutos;

    public Jwt gerar(Usuario usuario) {
        Instant agora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("vicino-api")
            .subject(usuario.getId().toString())
            .issuedAt(agora)
            .expiresAt(agora.plus(expiracaoMinutos, ChronoUnit.MINUTES))
            .claim("perfil", usuario.getPerfil().name())
            .build();
        
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims));
    }
}