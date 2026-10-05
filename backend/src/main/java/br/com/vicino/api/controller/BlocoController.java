package br.com.vicino.api.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;  
import org.springframework.web.bind.annotation.PathVariable;  
import org.springframework.web.bind.annotation.PostMapping;  
import org.springframework.web.bind.annotation.RequestBody;  
import org.springframework.web.bind.annotation.RequestMapping;  
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.vicino.api.dto.BlocoRequest;
import br.com.vicino.api.dto.BlocoResponse;
import br.com.vicino.api.service.BlocoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController 
@RequestMapping ("/blocos")
@RequiredArgsConstructor
public class BlocoController {
    private final BlocoService service;

    @PostMapping
    public ResponseEntity<BlocoResponse> criar(@Valid @RequestBody BlocoRequest req) {
        BlocoResponse criado = service.criar(req);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();

        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    public List<BlocoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
        public BlocoResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }   

}
