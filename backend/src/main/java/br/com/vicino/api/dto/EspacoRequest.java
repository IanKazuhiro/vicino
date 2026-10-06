package br.com.vicino.api.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;


public record EspacoRequest(
    @NotBlank @Size(max = 100) String nome,
    @Size(max = 500) String descricao,
    @NotNull LocalTime horaAbertura,
    @NotNull LocalTime horaFechamento,
    @NotNull @Positive Integer duracaoMinimaMinutos,
    @NotNull @Positive Integer duracaoMaximaMinutos,
    @NotNull @PositiveOrZero Integer antecedenciaMinimaHoras,
    @NotNull @Positive Integer antecedenciaMaximaDias,
    @NotNull @Positive Integer limiteReservasPorUnidade,
    @NotNull Boolean exigeAprovacao,
    @NotNull Boolean ativo) {
}