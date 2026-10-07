package br.com.vicino.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.vicino.api.service.ReservaService;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/reservas")
@RequiredArgsConstructor 
public class ReservaController {
    private final ReservaService service;
}
