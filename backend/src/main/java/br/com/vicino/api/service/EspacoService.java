package br.com.vicino.api.service;

import java.time.Duration;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.vicino.api.dto.EspacoRequest;
import br.com.vicino.api.dto.EspacoResponse;
import br.com.vicino.api.model.Espaco;
import br.com.vicino.api.repository.EspacoRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class EspacoService {
    private final EspacoRepository espacoRepository;

    @Transactional 
    public EspacoResponse criar(EspacoRequest request) {
        String nome = request.nome().trim();
        if(espacoRepository.existsByNome(nome)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um espaço com o nome: " + nome + ".");
        }
        validarRegras(request);

        Espaco espaco = new Espaco();
        preencher(espaco, request, nome);
        return EspacoResponse.de(espacoRepository.save(espaco));
    }

    @Transactional
    public EspacoResponse atualizar(Long id, EspacoRequest request) {
        Espaco espaco = espacoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Espaço não encontrado com o ID: " + id));
        
        String nome = request.nome().trim();
        if (espacoRepository.existsByNomeAndIdNot(nome, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um espaço com o nome: " + nome);
        }

        validarRegras(request);

        preencher(espaco, request, nome);
        return EspacoResponse.de(espaco);
    }

    @Transactional(readOnly = true)
    public List<EspacoResponse> listar() {
        return espacoRepository.findAll(Sort.by("nome")).stream().map(EspacoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public EspacoResponse buscarPorId(Long id) {
        Espaco espaco = espacoRepository.findById(id).
            orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Não há espaços cadastrados com o ID: " + id));

        return EspacoResponse.de(espaco);
    }

    private void validarRegras(EspacoRequest req) {
        if (!req.horaAbertura().isBefore(req.horaFechamento())) {
            throw erro("O horário de abertura deve ser anterior ao de fechamento.");
        }
        if (req.duracaoMinimaMinutos() > req.duracaoMaximaMinutos()) {
            throw erro("A duração mínima não pode ser maior que a duração máxima.");
        }
        long janelaMinutos = Duration.between(req.horaAbertura(), req.horaFechamento()).toMinutes();
        if (req.duracaoMaximaMinutos() > janelaMinutos) {
            throw erro("A duração máxima (" + req.duracaoMaximaMinutos() + " min) não cabe no horário de funcionamento (" + janelaMinutos + " min).");
        }
        if (req.antecedenciaMinimaHoras() >= req.antecedenciaMaximaDias() * 24) {
            throw erro("A antecedência mínima deve ser menor que a antecedência máxima.");
        }

    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }

    private void preencher(Espaco espaco, EspacoRequest request, String nome) {
        espaco.setNome(nome);
        espaco.setDescricao(request.descricao());
        espaco.setHoraAbertura(request.horaAbertura());
        espaco.setHoraFechamento(request.horaFechamento());
        espaco.setDuracaoMinimaMinutos(request.duracaoMinimaMinutos());
        espaco.setDuracaoMaximaMinutos(request.duracaoMaximaMinutos());
        espaco.setAntecedenciaMinimaHoras(request.antecedenciaMinimaHoras());
        espaco.setAntecedenciaMaximaDias(request.antecedenciaMaximaDias());
        espaco.setLimiteReservasPorUnidade(request.limiteReservasPorUnidade());
        espaco.setExigeAprovacao(request.exigeAprovacao());
        espaco.setAtivo(request.ativo());
    }
}