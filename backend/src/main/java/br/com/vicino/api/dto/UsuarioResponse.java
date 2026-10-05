package br.com.vicino.api.dto;

import br.com.vicino.api.enums.PerfilEnum;
import br.com.vicino.api.model.Usuario;

public record UsuarioResponse(Long id, String nome, String email, PerfilEnum perfil, Long unidadeId, boolean ativo) {
    public static UsuarioResponse de(Usuario u) {
        Long unidadeId = u.getUnidade() == null ? null : u.getUnidade().getId();
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil(), unidadeId, u.getAtivo());
    }
}