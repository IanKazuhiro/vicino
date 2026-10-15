package br.com.vicino.api.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.vicino.api.dto.AcessoVisitanteResponse;
import br.com.vicino.api.dto.AutorizacaoVisitanteRequest;
import br.com.vicino.api.dto.AutorizacaoVisitanteResponse;
import br.com.vicino.api.enums.PerfilEnum;
import br.com.vicino.api.enums.StatusAutorizacaoEnum;
import br.com.vicino.api.model.AcessoVisitante;
import br.com.vicino.api.model.AutorizacaoVisitante;
import br.com.vicino.api.model.Unidade;
import br.com.vicino.api.model.Usuario;
import br.com.vicino.api.repository.AcessoVisitanteRepository;
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
    private final AcessoVisitanteRepository acessoRepository;
    private final Clock clock;
    @Value ("${vicino.visitantes.retencao-dias}")
    private int retencaoDias;

    @Transactional
    public AutorizacaoVisitanteResponse autorizar(Long usuarioId, AutorizacaoVisitanteRequest req) {
        Usuario usuario = buscarUsuario(usuarioId);
        Unidade unidade = resolverUnidade(usuario, req.unidadeId());
        validarPeriodo(req.inicio(), req.fim());

        AutorizacaoVisitante autorizacao = new AutorizacaoVisitante();
        autorizacao.setUnidade(unidade);
        autorizacao.setCriadoPor(usuario);
        autorizacao.setNome(req.nome().trim());
        String documento = normalizarDocumento(req.documento());
        if (documento.isEmpty()) {
            throw erro(HttpStatus.BAD_REQUEST, "O documento deve conter letras ou números.");
        }
        autorizacao.setDocumento(documento);
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

    @Transactional(readOnly = true)
    public List<AutorizacaoVisitanteResponse> buscarValidas(String busca) {
        String nome = busca == null ? "" : busca.trim();
        String documento = normalizarDocumento(nome);
        if (documento.isEmpty()) {
            documento = nome;
        }
        return autorizacaoRepository.buscarValidas(StatusAutorizacaoEnum.ATIVA, LocalDateTime.now(clock), nome, documento)
            .stream().map(AutorizacaoVisitanteResponse::de).toList();
    }

    @Transactional 
    public AcessoVisitanteResponse registrarEntrada(Long usuarioId, Long autorizacaoId) {
        Usuario usuario = buscarUsuario(usuarioId);
        AutorizacaoVisitante autorizacao = buscarAutorizacao(autorizacaoId);
        LocalDateTime agora = LocalDateTime.now(clock);

        if (autorizacao.getStatus() != StatusAutorizacaoEnum.ATIVA) {
            throw erro(HttpStatus.CONFLICT, "A autorização está " + autorizacao.getStatus() + " e não permite entrada.");
        }

        if (agora.isBefore(autorizacao.getInicio()) || !agora.isBefore(autorizacao.getFim())) {
            throw erro(HttpStatus.BAD_REQUEST, "Fora do período da autorização (" + autorizacao.getInicio() + " a " + autorizacao.getFim() + ").");
        }

        if (acessoRepository.existsByAutorizacaoIdAndSaidaEmIsNull(autorizacaoId)) {
            throw erro(HttpStatus.CONFLICT, "O visitante já está no condomínio. Registre uma saída antes de uma nova entrada.");
        }

        AcessoVisitante acesso = new AcessoVisitante();
        acesso.setAutorizacao(autorizacao);
        acesso.setEntradaEm(agora);
        acesso.setRegistradoEntradaPor(usuario);
        
        try {
            return AcessoVisitanteResponse.de(acessoRepository.saveAndFlush(acesso));
        } catch (DataIntegrityViolationException e) {
            throw erro(HttpStatus.CONFLICT, "O visitante já está no condomínio. Registre uma saída antes de uma nova entrada.");
        }
    }

    @Transactional
    public AcessoVisitanteResponse registrarSaida(Long usuarioId, Long acessoId) {
        Usuario usuario = buscarUsuario(usuarioId);
        AcessoVisitante acesso = acessoRepository.findById(acessoId)
            .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Acesso não encontrado com o ID: " + acessoId));

        if (acesso.getSaidaEm() != null) {
            throw erro(HttpStatus.CONFLICT, "A saída deste visitante já foi registrada.");
        }

        acesso.setSaidaEm(LocalDateTime.now(clock));
        acesso.setRegistradoSaidaPor(usuario);
        return AcessoVisitanteResponse.de(acesso);
    }

    @Transactional(readOnly = true)
    public List<AcessoVisitanteResponse> presentes() {
        return acessoRepository.findBySaidaEmIsNullOrderByEntradaEm()
            .stream().map(AcessoVisitanteResponse::de).toList();
    }

    @Transactional
    public int anonimizarExpiradas() {
        LocalDateTime limite = LocalDateTime.now(clock).minusDays(retencaoDias);
        List<AutorizacaoVisitante> expiradas = autorizacaoRepository.findByFimBeforeAndAnonimizadoEmIsNull(limite);
        Instant agora = clock.instant();

        for (AutorizacaoVisitante autorizacao : expiradas) {
            autorizacao.setNome(null);
            autorizacao.setDocumento(null);
            autorizacao.setTelefone(null);
            autorizacao.setPlaca(null);
            autorizacao.setAnonimizadoEm(agora);
        }
        return expiradas.size();
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

    private String normalizarDocumento(String documento) {
        return documento.replaceAll("[^0-9A-Za-z]", "").toUpperCase();
    }
}