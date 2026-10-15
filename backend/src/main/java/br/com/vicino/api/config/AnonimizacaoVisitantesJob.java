package br.com.vicino.api.config;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.vicino.api.service.VisitanteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnonimizacaoVisitantesJob {
    private final VisitanteService visitanteService;

    @Scheduled(cron = "${vicino.visitantes.anonimizacao-cron}", zone = "${vicino.fuso-horario}")
    public void executar() {
        int total = visitanteService.anonimizarExpiradas();
        log.info("Anonimização de visitantes: {} autorização(ões) anonimizada(s).", total);
    }
}