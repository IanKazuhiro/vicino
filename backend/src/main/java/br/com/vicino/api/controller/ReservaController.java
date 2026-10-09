package br.com.vicino.api.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.vicino.api.dto.AgendaItemResponse;
import br.com.vicino.api.dto.ReservaRequest;
import br.com.vicino.api.dto.ReservaResponse;
import br.com.vicino.api.enums.StatusReservaEnum;
import br.com.vicino.api.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/reservas")
@RequiredArgsConstructor 
public class ReservaController {
    private final ReservaService service;

    @PostMapping
    public ResponseEntity<ReservaResponse> criar (@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ReservaRequest request) {
        Long usuarioId = Long.valueOf(jwt.getSubject());
        ReservaResponse criada = service.criar(usuarioId, request);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(criada.id())
            .toUri();
        
        return ResponseEntity.created(location).body(criada);
    }

    @PostMapping("/{id}/aprovar")
    public ReservaResponse aprovar(@PathVariable Long id) { 
        return service.aprovar(id);
    }
    
    @PostMapping("/{id}/recusar")
    public ReservaResponse recusar(@PathVariable Long id) { 
        return service.recusar(id);
    }

    @PostMapping("/{id}/cancelar")
    public ReservaResponse cancelar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) { 
        return service.cancelar(Long.valueOf(jwt.getSubject()), id);
    }

    @GetMapping("/{id}")
    public ReservaResponse buscarPorId(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.buscarPorId(Long.valueOf(jwt.getSubject()), id);
    }

    @GetMapping("/minhas")
    public List<ReservaResponse> minhas(@AuthenticationPrincipal Jwt jwt) {
        return service.minhas(Long.valueOf(jwt.getSubject()));
    }

    @GetMapping
    public List<ReservaResponse> listar(@RequestParam(required = false) Long espacoId,
                                        @RequestParam(required = false) StatusReservaEnum status) {
        return service.listar(espacoId, status);
    }

    @GetMapping("/agenda")
    public List<AgendaItemResponse> agenda(@RequestParam Long espacoId,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.agenda(espacoId, data);
    }
}