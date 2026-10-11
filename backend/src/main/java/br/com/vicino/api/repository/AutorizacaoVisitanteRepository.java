package br.com.vicino.api.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.vicino.api.enums.StatusAutorizacaoEnum;
import br.com.vicino.api.model.AutorizacaoVisitante;

public interface AutorizacaoVisitanteRepository extends JpaRepository<AutorizacaoVisitante, Long>{
    List<AutorizacaoVisitante> findByUnidadeIdOrderByInicioDesc(Long unidadeId);
    @Query("""
    SELECT a FROM AutorizacaoVisitante a
    WHERE a.status = :status
      AND a.inicio <= :agora AND a.fim > :agora
      AND (LOWER(a.nome) LIKE LOWER(CONCAT('%', :nome, '%'))
           OR a.documento LIKE CONCAT('%', :documento, '%'))
    ORDER BY a.nome
    """)
    List<AutorizacaoVisitante> buscarValidas(
        @Param("status") StatusAutorizacaoEnum status,
        @Param("agora") LocalDateTime agora,
        @Param("nome") String nome,
        @Param("documento") String documento);
    List<AutorizacaoVisitante> findByFimBeforeAndAnonimizadoEmIsNull(LocalDateTime limite);
}