package br.com.vicino.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.vicino.api.model.Espaco;

public interface EspacoRepository extends JpaRepository<Espaco, Long> {
    boolean existsByNome(String nome);
    boolean existsByNomeAndIdNot(String nome, Long id);       
}
