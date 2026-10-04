package br.com.vicino.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BlocoRequest (
    @NotBlank 
    @Size(max = 100)
    String nome ) {
}