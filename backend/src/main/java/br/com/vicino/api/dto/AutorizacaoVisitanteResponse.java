package br.com.vicino.api.dto;

import java.time.Instant;
import java.time.LocalDateTime;

import br.com.vicino.api.enums.StatusAutorizacaoEnum;
import br.com.vicino.api.model.AutorizacaoVisitante;
import br.com.vicino.api.model.Unidade;

public record AutorizacaoVisitanteResponse(Long id, Long unidadeId, String unidade, String nome, String documento, String telefone, String placa, LocalDateTime inicio, LocalDateTime fim, StatusAutorizacaoEnum status, Instant criadoEm, Instant anonimizadoEm) {
    public static AutorizacaoVisitanteResponse de(AutorizacaoVisitante a) {
        Unidade u = a.getUnidade();
        return new AutorizacaoVisitanteResponse(
            a.getId(),
            u.getId(), u.getBloco().getNome() + " - " + u.getNumero(),
            a.getNome(), a.getDocumento(), a.getTelefone(), a.getPlaca(),
            a.getInicio(), a.getFim(),
            a.getStatus(),
            a.getCriadoEm(), a.getAnonimizadoEm());
    }
}