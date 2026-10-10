package br.com.vicino.api.dto;

import java.time.LocalDateTime;

import br.com.vicino.api.model.AcessoVisitante;
import br.com.vicino.api.model.AutorizacaoVisitante;
import br.com.vicino.api.model.Unidade;

public record AcessoVisitanteResponse(Long id, Long autorizacaoId, String nome, String placa, String unidade, LocalDateTime entradaEm, LocalDateTime saidaEm) {
    public static AcessoVisitanteResponse de(AcessoVisitante acesso) {
        AutorizacaoVisitante a = acesso.getAutorizacao();
        Unidade u = a.getUnidade();
        return new AcessoVisitanteResponse(
            acesso.getId(),
            a.getId(), a.getNome(), a.getPlaca(),
            u.getBloco().getNome() + " - " + u.getNumero(),
            acesso.getEntradaEm(), acesso.getSaidaEm());
    }
}