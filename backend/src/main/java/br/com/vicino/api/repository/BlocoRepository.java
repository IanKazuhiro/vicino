package br.com.vicino.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.vicino.api.model.Bloco;
import java.util.Optional;

public interface BlocoRepository extends JpaRepository<Bloco, Long> {
    boolean existsByNome(String nome);
    
    Optional<Bloco> findByNome(String nome);
}