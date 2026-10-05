package br.com.vicino.api.dto;

import br.com.vicino.api.model.Unidade;

public record UnidadeResponse(Long id,  Long blocoId, String blocoNome, String numero) {
    public static UnidadeResponse de(Unidade u) {
        return new UnidadeResponse(u.getId(), u.getBloco().getId(), u.getBloco().getNome(), u.getNumero());
    }
}
