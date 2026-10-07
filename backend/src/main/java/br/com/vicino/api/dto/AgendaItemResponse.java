package br.com.vicino.api.dto;

import java.time.LocalDateTime;

import br.com.vicino.api.enums.StatusReservaEnum;
import br.com.vicino.api.model.Reserva;

public record AgendaItemResponse(LocalDateTime inicio, LocalDateTime fim, StatusReservaEnum status) {

    public static AgendaItemResponse de(Reserva r) {
        return new AgendaItemResponse(r.getInicio(), r.getFim(), r.getStatus());
    }
}
