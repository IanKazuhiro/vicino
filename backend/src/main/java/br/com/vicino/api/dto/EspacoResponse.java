package br.com.vicino.api.dto;

import java.time.LocalTime;

import br.com.vicino.api.model.Espaco;

public record EspacoResponse(
    Long id, String nome, String descricao,
    LocalTime horaAbertura, LocalTime horaFechamento,
    Integer duracaoMinimaMinutos, Integer duracaoMaximaMinutos,
    Integer antecedenciaMinimaHoras, Integer antecedenciaMaximaDias,
    Integer limiteReservasPorUnidade, Boolean exigeAprovacao, Boolean ativo) {

    public static EspacoResponse de(Espaco e) {
        return new EspacoResponse(
            e.getId(), e.getNome(), e.getDescricao(),
            e.getHoraAbertura(), e.getHoraFechamento(),
            e.getDuracaoMinimaMinutos(), e.getDuracaoMaximaMinutos(),
            e.getAntecedenciaMinimaHoras(), e.getAntecedenciaMaximaDias(),
            e.getLimiteReservasPorUnidade(), e.getExigeAprovacao(), e.getAtivo());
    }
}
