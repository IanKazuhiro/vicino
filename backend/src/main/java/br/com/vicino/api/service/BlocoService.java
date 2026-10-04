package br.com.vicino.api.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.vicino.api.model.Bloco;
import br.com.vicino.api.dto.BlocoRequest;
import br.com.vicino.api.dto.BlocoResponse;
import br.com.vicino.api.repository.BlocoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlocoService {
    private final BlocoRepository rep;


    //Regra de negócio: Não permitir a criação de blocos com nomes duplicados
    @Transactional
    public BlocoResponse criar(BlocoRequest req) {
        String nome = req.nome().trim();

        if (rep.existsByNome(nome)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um bloco com esse nome." + nome);
        }

        Bloco bloco = new Bloco();
        bloco.setNome(nome);
        return BlocoResponse.de(rep.save(bloco));
    }

    //Lista todos os blocos cadastrados no sistema
    @Transactional 
    public List<BlocoResponse> listar() {
        return rep.findAll().stream().map(BlocoResponse::de).toList();
    }

    @Transactional
    public BlocoResponse buscarPorId(Long id) {
        Bloco bloco = rep.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bloco não encontrado com o ID: " + id));
        return BlocoResponse.de(bloco);
    }
}
