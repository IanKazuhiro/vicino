package br.com.vicino.api.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.vicino.api.dto.UnidadeRequest;
import br.com.vicino.api.dto.UnidadeResponse;
import br.com.vicino.api.model.Bloco;
import br.com.vicino.api.model.Unidade;
import br.com.vicino.api.repository.BlocoRepository;
import br.com.vicino.api.repository.UnidadeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UnidadeService {

    private final UnidadeRepository unidadeRep;
    private final BlocoRepository blocoRep;

    @Transactional
    public UnidadeResponse criar(UnidadeRequest req) {
        String numero = req.numero().trim();

        Bloco bloco = blocoRep.findById(req.blocoId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bloco não encontrado com o ID: " + req.blocoId()));

        if (unidadeRep.existsByBlocoIdAndNumero(bloco.getId(), numero)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe a unidade " + numero + " nesse bloco.");
        }

        Unidade unidade = new Unidade();
        unidade.setBloco(bloco);
        unidade.setNumero(numero);
        return UnidadeResponse.de(unidadeRep.save(unidade)); 
    } 

    @Transactional(readOnly = true)
    public List<UnidadeResponse> listar() {
        return unidadeRep.findAll().stream().map(UnidadeResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<UnidadeResponse> listarPorBloco(Long blocoId) {
        return unidadeRep.findByBlocoId(blocoId).stream().map(UnidadeResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public UnidadeResponse buscarPorId(Long id) {
        Unidade unidade = unidadeRep.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidade não encontrada com o ID: " + id));
        return UnidadeResponse.de(unidade);
    }
}