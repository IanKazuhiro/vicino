package br.com.vicino.api.dto;

import java.time.Instant;
import java.time.LocalDateTime;

import br.com.vicino.api.enums.StatusReservaEnum;
import br.com.vicino.api.model.Reserva;
import br.com.vicino.api.model.Unidade;

public record ReservaResponse(Long id, Long espacoId, String espacoNome, Long unidadeId, String unidade, LocalDateTime inicio, LocalDateTime fim, StatusReservaEnum status, Instant criadoEm) {
    public static ReservaResponse de(Reserva r) {
        Unidade u = r.getUnidade();
        return new ReservaResponse(
            r.getId(),
            r.getEspaco().getId(), r.getEspaco().getNome(),
            u.getId(), u.getBloco().getNome() + " - " + u.getNumero(),
            r.getInicio(), r.getFim(),
            r.getStatus(),
            r.getCriadoEm());
    }
}
