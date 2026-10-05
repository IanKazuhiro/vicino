package br.com.vicino.api.dto;

import br.com.vicino.api.enums.PerfilEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(@NotBlank @Size(max = 150) String nome, 
                             @NotBlank @Email @Size(max = 150) String email, 
                             @NotBlank @Size(min = 8, max = 72) String senha,
                             @NotNull PerfilEnum perfil,
                             Long unidadeId
) { }