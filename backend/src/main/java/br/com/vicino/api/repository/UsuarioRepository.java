package br.com.vicino.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.vicino.api.model.Usuario; 
import br.com.vicino.api.enums.PerfilEnum;
import java.util.Optional;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByEmail(String email);
    List<Usuario> findByUnidadeIdAndPerfil(Long unidadeId, PerfilEnum perfil);  
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByNome(String nome);
}
