package br.com.vicino.api.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record ReservaRequest(
    @NotNull Long espacoId,
    Long unidadeId,
    @NotNull LocalDateTime inicio,
    @NotNull LocalDateTime fim) {
}
