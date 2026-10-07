package br.com.vicino.api.service;

import org.springframework.stereotype.Service;

import br.com.vicino.api.repository.ReservaRepository;
import br.com.vicino.api.dto.ReservaRequest;
import br.com.vicino.api.dto.ReservaResponse;
import br.com.vicino.api.model.Reserva;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ReservaService {
    private final ReservaRepository repository;
    
}
