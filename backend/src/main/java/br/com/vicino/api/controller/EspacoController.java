package br.com.vicino.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.vicino.api.dto.EspacoRequest;
import br.com.vicino.api.dto.EspacoResponse;
import br.com.vicino.api.service.EspacoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController 
@RequestMapping("/espacos")
@RequiredArgsConstructor 
public class EspacoController {
    private final EspacoService espacoService;

    @PostMapping
    public ResponseEntity<EspacoResponse> criar(@Valid @RequestBody EspacoRequest request) {
        EspacoResponse criado = espacoService.criar(request);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(criado.id())
            .toUri();
        return ResponseEntity.created(location).body(criado);
    }
     
    @PutMapping("/{id}")
    public EspacoResponse atualizar(@PathVariable Long id, @Valid @RequestBody EspacoRequest request) {
        return espacoService.atualizar(id, request);
    }

    @GetMapping
    public List<EspacoResponse> listar() {
        return espacoService.listar();
    }
    
    @GetMapping("/{id}")
    public EspacoResponse buscarPorId(@PathVariable Long id) {
        return espacoService.buscarPorId(id);
    }    
}
