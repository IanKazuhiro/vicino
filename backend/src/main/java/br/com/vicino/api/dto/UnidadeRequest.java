package br.com.vicino.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UnidadeRequest(@NotNull Long blocoId, @NotBlank @Size(max = 20) String numero) {
    
}
