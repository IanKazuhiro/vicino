package br.com.vicino.api.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.com.vicino.api.enums.StatusReservaEnum;
import br.com.vicino.api.model.Reserva;



public interface ReservaRepository extends JpaRepository<Reserva, Long>{
    
    boolean existsByEspacoIdAndStatusInAndInicioLessThanAndFimGreaterThan(Long espacoId, Collection<StatusReservaEnum> status, LocalDateTime fim, LocalDateTime inicio);

    long countByUnidadeIdAndEspacoIdAndStatusInAndInicioAfter(Long unidadeId, Long espacoId, Collection<StatusReservaEnum> status, LocalDateTime agora);

    List<Reserva> findByUnidadeIdOrderByInicioDesc(Long unidadeId);

    List<Reserva> findByEspacoIdAndStatusInAndInicioLessThanAndFimGreaterThanOrderByInicio(
        Long espacoId, Collection<StatusReservaEnum> status, LocalDateTime fimDoDia, LocalDateTime inicioDoDia);

    @Query("""
        SELECT r FROM Reserva r
        WHERE (:espacoId IS NULL OR r.espaco.id = :espacoId)
          AND (:status IS NULL OR r.status = :status)
        ORDER BY r.inicio DESC
        """)
    List<Reserva> buscar(@Param("espacoId") Long espacoId, @Param("status") StatusReservaEnum status);
}
