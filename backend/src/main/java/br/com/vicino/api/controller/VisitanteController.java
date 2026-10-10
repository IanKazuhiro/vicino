package br.com.vicino.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.vicino.api.dto.AutorizacaoVisitanteRequest;
import br.com.vicino.api.dto.AutorizacaoVisitanteResponse;
import br.com.vicino.api.service.VisitanteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/visitantes")
@RequiredArgsConstructor
public class VisitanteController {
    private final VisitanteService service;

    @PostMapping("/autorizacoes")
    public ResponseEntity<AutorizacaoVisitanteResponse> autorizar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AutorizacaoVisitanteRequest request) {
        AutorizacaoVisitanteResponse criada = service.autorizar(Long.valueOf(jwt.getSubject()), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping("/autorizacoes/minhas")
    public List<AutorizacaoVisitanteResponse> minhas(@AuthenticationPrincipal Jwt jwt) {
        return service.minhas(Long.valueOf(jwt.getSubject()));
    }

    @PostMapping("/autorizacoes/{id}/cancelar")
    public AutorizacaoVisitanteResponse cancelar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.cancelar(Long.valueOf(jwt.getSubject()), id);
    }
}