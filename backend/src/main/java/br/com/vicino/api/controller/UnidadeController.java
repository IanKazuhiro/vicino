package br.com.vicino.api.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.vicino.api.dto.UnidadeRequest;
import br.com.vicino.api.dto.UnidadeResponse;
import br.com.vicino.api.service.UnidadeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/unidades")
@RequiredArgsConstructor
public class UnidadeController {
    private final UnidadeService service;

    @PostMapping
    public ResponseEntity<UnidadeResponse> criar(@Valid @RequestBody UnidadeRequest request) {
        UnidadeResponse criado = service.criar(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();

        return ResponseEntity.created(location).body(criado);   
    }

    @GetMapping
    public List<UnidadeResponse> listar(@RequestParam(required = false) Long blocoId) {
        return blocoId == null ? service.listar() : service.listarPorBloco(blocoId);
    }

    @GetMapping("/{id}")
    public UnidadeResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }
}