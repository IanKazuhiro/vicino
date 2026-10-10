package br.com.vicino.api.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.vicino.api.dto.AutorizacaoVisitanteRequest;
import br.com.vicino.api.dto.AutorizacaoVisitanteResponse;
import br.com.vicino.api.enums.PerfilEnum;
import br.com.vicino.api.enums.StatusAutorizacaoEnum;
import br.com.vicino.api.model.AutorizacaoVisitante;
import br.com.vicino.api.model.Unidade;
import br.com.vicino.api.model.Usuario;
import br.com.vicino.api.repository.AutorizacaoVisitanteRepository;
import br.com.vicino.api.repository.UnidadeRepository;
import br.com.vicino.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class VisitanteService {

    private static final int DURACAO_MAXIMA_DIAS = 30;
    private final AutorizacaoVisitanteRepository autorizacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UnidadeRepository unidadeRepository;
    private final Clock clock;

    @Transactional
    public AutorizacaoVisitanteResponse autorizar(Long usuarioId, AutorizacaoVisitanteRequest req) {
        Usuario usuario = buscarUsuario(usuarioId);
        Unidade unidade = resolverUnidade(usuario, req.unidadeId());
        validarPeriodo(req.inicio(), req.fim());

        AutorizacaoVisitante autorizacao = new AutorizacaoVisitante();
        autorizacao.setUnidade(unidade);
        autorizacao.setCriadoPor(usuario);
        autorizacao.setNome(req.nome().trim());
        autorizacao.setDocumento(req.documento().trim());
        autorizacao.setTelefone(limpar(req.telefone()));
        String placa = limpar(req.placa());
        autorizacao.setPlaca(placa == null ? null : placa.toUpperCase());
        autorizacao.setInicio(req.inicio());
        autorizacao.setFim(req.fim());
        autorizacao.setStatus(StatusAutorizacaoEnum.ATIVA);

        return AutorizacaoVisitanteResponse.de(autorizacaoRepository.save(autorizacao));
    }

    @Transactional(readOnly = true)
    public List<AutorizacaoVisitanteResponse> minhas(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        if (usuario.getUnidade() == null) {
            return List.of();
        }
        return autorizacaoRepository.findByUnidadeIdOrderByInicioDesc(usuario.getUnidade().getId()).stream().map(AutorizacaoVisitanteResponse::de).toList();
    }

    @Transactional
    public AutorizacaoVisitanteResponse cancelar(Long usuarioId, Long id) {
        Usuario usuario = buscarUsuario(usuarioId);
        AutorizacaoVisitante autorizacao = buscarAutorizacao(id);

        if (usuario.getPerfil() == PerfilEnum.PORTARIA) {
            throw erro(HttpStatus.FORBIDDEN, "A portaria não pode cancelar autorizações.");
        }
        if (usuario.getPerfil() == PerfilEnum.MORADOR
                && !autorizacao.getUnidade().getId().equals(usuario.getUnidade().getId())) {
            throw erro(HttpStatus.FORBIDDEN, "O morador só pode cancelar autorizações da própria unidade.");
        }
        if (autorizacao.getStatus() != StatusAutorizacaoEnum.ATIVA) {
            throw erro(HttpStatus.CONFLICT, "A autorização está " + autorizacao.getStatus() + " e não pode ser cancelada.");
        }
        if (!LocalDateTime.now(clock).isBefore(autorizacao.getFim())) {
            throw erro(HttpStatus.BAD_REQUEST, "A autorização já terminou.");
        }

        autorizacao.setStatus(StatusAutorizacaoEnum.CANCELADA);
        return AutorizacaoVisitanteResponse.de(autorizacao);
    }

    private Usuario buscarUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow(() -> erro(HttpStatus.UNAUTHORIZED, "Usuário não encontrado."));
        if (!usuario.getAtivo()) {
            throw erro(HttpStatus.FORBIDDEN, "Usuário inativo.");
        }
        return usuario;
    }

    private Unidade resolverUnidade(Usuario usuario, Long unidadeId) {
        if (usuario.getPerfil() == PerfilEnum.MORADOR) {
            Unidade propria = usuario.getUnidade();
            if (unidadeId != null && !unidadeId.equals(propria.getId())) {
                throw erro(HttpStatus.FORBIDDEN, "O morador só pode autorizar visitantes para a própria unidade.");
            }
            return propria;
        }
        if (usuario.getPerfil() == PerfilEnum.PORTARIA) {
            throw erro(HttpStatus.FORBIDDEN, "A portaria não pode autorizar visitantes.");
        }
        if (unidadeId == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe a unidade (unidadeId) da autorização.");
        }
        return unidadeRepository.findById(unidadeId).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Unidade não encontrada com o ID: " + unidadeId));
    }

    private void validarPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (!inicio.isBefore(fim)) {
            throw erro(HttpStatus.BAD_REQUEST, "O início da autorização deve ser anterior ao fim.");
        }
        if (!fim.isAfter(LocalDateTime.now(clock))) {
            throw erro(HttpStatus.BAD_REQUEST, "O fim da autorização já passou.");
        }
        if (fim.isAfter(inicio.plusDays(DURACAO_MAXIMA_DIAS))) {
            throw erro(HttpStatus.BAD_REQUEST, "A autorização pode durar no máximo " + DURACAO_MAXIMA_DIAS + " dias.");
        }
    }

    private String limpar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    private ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }

    private AutorizacaoVisitante buscarAutorizacao(Long id) {
        return autorizacaoRepository.findById(id).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Autorização não encontrada com o ID: " + id));
    }
}