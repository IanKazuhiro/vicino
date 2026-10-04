package br.com.vicino.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.vicino.api.model.Unidade;
import java.util.List;
import java.util.Optional;

public interface UnidadeRepository extends JpaRepository<Unidade, Long> {
    boolean existsByBlocoIdAndNumero(Long blocoId, String numero);
    
    Optional<Unidade> findByBlocoIdAndNumero(Long  blocoId, String numero);

    List<Unidade> findByBlocoId(Long blocoId);
}