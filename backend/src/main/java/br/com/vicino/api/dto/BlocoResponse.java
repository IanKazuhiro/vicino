package br.com.vicino.api.dto;

import br.com.vicino.api.model.Bloco;

public record BlocoResponse(Long id, String nome) {
    public static BlocoResponse de(Bloco bloco) {
        return new BlocoResponse(bloco.getId(), bloco.getNome());
    }
}
