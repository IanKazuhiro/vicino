package br.com.vicino.api.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public record AutorizacaoVisitanteRequest(
    Long unidadeId,
    @NotBlank @Size(max = 150) String nome,
    @NotBlank @Size(max = 20) String documento,
    @Size(max = 20) String telefone,
    @Size(max = 10) String placa,
    @NotNull LocalDateTime inicio,
    @NotNull LocalDateTime fim) {
}