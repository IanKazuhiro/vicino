package br.com.vicino.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.vicino.api.model.AcessoVisitante;

public interface AcessoVisitanteRepository extends JpaRepository<AcessoVisitante, Long> {

    boolean existsByAutorizacaoIdAndSaidaEmIsNull(Long autorizacaoId);

    List<AcessoVisitante> findBySaidaEmIsNullOrderByEntradaEm();
}