package br.com.vicino.api.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.vicino.api.dto.UsuarioRequest;
import br.com.vicino.api.dto.UsuarioResponse;
import br.com.vicino.api.model.Unidade;
import br.com.vicino.api.model.Usuario;
import br.com.vicino.api.repository.UnidadeRepository;
import br.com.vicino.api.repository.UsuarioRepository;
import br.com.vicino.api.enums.PerfilEnum;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor  
public class UsuarioService {
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final UnidadeRepository unidadeRepository;

    @Transactional 
    public UsuarioResponse criar(UsuarioRequest req) {
        String email = req.email().trim().toLowerCase();

        if(usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário com esse e-mail.");
        }

        Unidade unidade = null;
        
        if(req.unidadeId() != null) {
            unidade = unidadeRepository.findById(req.unidadeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Unidade não encontrada com o ID: " + req.unidadeId()));
        }

        if (req.perfil() == PerfilEnum.MORADOR && unidade == null) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Morador precisa estar vinculado a uma unidade.");
        }

        Usuario u = new Usuario();
        u.setNome(req.nome().trim());
        u.setEmail(email);
        u.setSenhaHash(passwordEncoder.encode(req.senha()));
        u.setPerfil(req.perfil());
        u.setUnidade(unidade);

        return UsuarioResponse.de(usuarioRepository.save(u));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioResponse::de).toList();
    }

    @Transactional (readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Usuário não encontrada com o ID: " + id));

        return UsuarioResponse.de(usuario);
    }
}