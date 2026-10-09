package br.com.vicino.api.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.vicino.api.dto.AgendaItemResponse;
import br.com.vicino.api.dto.ReservaRequest;
import br.com.vicino.api.dto.ReservaResponse;
import br.com.vicino.api.enums.PerfilEnum;
import br.com.vicino.api.enums.StatusReservaEnum;
import br.com.vicino.api.model.Espaco;
import br.com.vicino.api.model.Reserva;
import br.com.vicino.api.model.Unidade;
import br.com.vicino.api.model.Usuario;
import br.com.vicino.api.repository.EspacoRepository;
import br.com.vicino.api.repository.ReservaRepository;
import br.com.vicino.api.repository.UnidadeRepository;
import br.com.vicino.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private static final List<StatusReservaEnum> ATIVAS = List.of(StatusReservaEnum.PENDENTE, StatusReservaEnum.CONFIRMADA);

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UnidadeRepository unidadeRepository;
    private final EspacoRepository espacoRepository;
    private final Clock clock;

    @Transactional
    public ReservaResponse criar(Long usuarioId, ReservaRequest req) {
        Usuario usuario = buscarUsuario(usuarioId);
        Unidade unidade = resolverUnidade(usuario, req.unidadeId());
        Espaco espaco = espacoRepository.findById(req.espacoId()).orElseThrow(
            () -> erro(HttpStatus.NOT_FOUND, "Espaço não encontrado com o ID: " + req.espacoId()));
        if (!espaco.getAtivo()) {
            throw erro(HttpStatus.BAD_REQUEST, "O espaço " + espaco.getNome() + " está inativo.");
        }

        validarJanela(espaco, req.inicio(), req.fim());
        validarAntecedencia(espaco, req.inicio());

        long futuras = reservaRepository.countByUnidadeIdAndEspacoIdAndStatusInAndInicioAfter(
            unidade.getId(), espaco.getId(), ATIVAS, LocalDateTime.now(clock));
        if (futuras >= espaco.getLimiteReservasPorUnidade()) {
            throw erro(HttpStatus.CONFLICT, "A unidade já atingiu o limite de "
                + espaco.getLimiteReservasPorUnidade() + " reservas futuras neste espaço.");
        }

        if (reservaRepository.existsByEspacoIdAndStatusInAndInicioLessThanAndFimGreaterThan(
                espaco.getId(), ATIVAS, req.fim(), req.inicio())) {
            throw erro(HttpStatus.CONFLICT, "Já existe uma reserva neste horário.");
        }

        Reserva reserva = new Reserva();
        reserva.setEspaco(espaco);
        reserva.setUnidade(unidade);
        reserva.setCriadoPor(usuario);
        reserva.setInicio(req.inicio());
        reserva.setFim(req.fim());
        boolean precisaAprovacao = espaco.getExigeAprovacao() && usuario.getPerfil() == PerfilEnum.MORADOR;
        reserva.setStatus(precisaAprovacao ? StatusReservaEnum.PENDENTE : StatusReservaEnum.CONFIRMADA);

        try {
            return ReservaResponse.de(reservaRepository.saveAndFlush(reserva));
        } catch (DataIntegrityViolationException e) {
            throw erro(HttpStatus.CONFLICT, "Já existe uma reserva neste horário.");
        }
    }

    @Transactional
    public ReservaResponse aprovar(Long id) {
        return decidir(id, StatusReservaEnum.CONFIRMADA);
    }

    @Transactional
    public ReservaResponse recusar(Long id) {
        return decidir(id, StatusReservaEnum.RECUSADA);
    }

    @Transactional 
    public ReservaResponse cancelar(Long usuarioId, Long id) {
        Usuario usuario = buscarUsuario(usuarioId);
        Reserva reserva = buscarReserva(id);

        if (usuario.getPerfil() == PerfilEnum.PORTARIA) {
            throw erro(HttpStatus.FORBIDDEN,"A portaria não pode cancelar uma reserva");
        }

        boolean morador = usuario.getPerfil() == PerfilEnum.MORADOR;
        if(morador && !reserva.getUnidade().getId().equals(usuario.getUnidade().getId())) {
            throw erro(HttpStatus.FORBIDDEN, "O morador só pode cancelar as reservas da própria unidade!");
        }

        exigirStatus(reserva, StatusReservaEnum.PENDENTE, StatusReservaEnum.CONFIRMADA);

        if (morador) {
            int horas = reserva.getEspaco().getAntecedenciaMinimaHoras();
            LocalDateTime prazo = reserva.getInicio().minusHours(horas);

            if (LocalDateTime.now(clock).isAfter(prazo)) {
                throw erro(HttpStatus.BAD_REQUEST, "O prazo para cancelar a reserva expirou: O prazo para cancelamento era até " + horas + " horas antes do início.");
            }
        } else {
            exigirAntesDoInicio(reserva);
        }

        reserva.setStatus(StatusReservaEnum.CANCELADA);
        return ReservaResponse.de(reserva);
    }

    @Transactional(readOnly = true) 
    public ReservaResponse buscarPorId(Long usuarioId, Long id) {
        Usuario usuario = buscarUsuario(usuarioId);
        Reserva reserva = buscarReserva(id);
        if (usuario.getPerfil() == PerfilEnum.PORTARIA) {
            throw erro(HttpStatus.FORBIDDEN, "A portaria não tem acesso aos detalhes do agendamento!");
        }
        if (usuario.getPerfil() == PerfilEnum.MORADOR && !reserva.getUnidade().getId().equals(usuario.getUnidade().getId())) {
            throw erro(HttpStatus.FORBIDDEN, "O morador só pode ver as reservas da própria unidade");
        }
        return ReservaResponse.de(reserva);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> minhas(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        if(usuario.getUnidade() == null) {
            return List.of();
        }            
        return reservaRepository.findByUnidadeIdOrderByInicioDesc(usuario.getUnidade().getId()).stream().map(ReservaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listar(Long espacoId, StatusReservaEnum status) {
        return reservaRepository.buscar(espacoId, status)
            .stream().map(ReservaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<AgendaItemResponse> agenda(Long espacoId, LocalDate data) {
        if (!espacoRepository.existsById(espacoId)) {
            throw erro(HttpStatus.NOT_FOUND, "Espaço não encontrado com o ID: " + espacoId);
        }
        LocalDateTime inicioDoDia = data.atStartOfDay();
        LocalDateTime fimDoDia = data.plusDays(1).atStartOfDay();
        return reservaRepository.findByEspacoIdAndStatusInAndInicioLessThanAndFimGreaterThanOrderByInicio(
                espacoId, ATIVAS, fimDoDia, inicioDoDia)
            .stream().map(AgendaItemResponse::de).toList();
    }

    private Usuario buscarUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> erro(HttpStatus.UNAUTHORIZED, "Usuário não encontrado."));
        if (!usuario.getAtivo()) {
            throw erro(HttpStatus.FORBIDDEN, "Usuário inativo.");
        }
        return usuario;
    }

    private Unidade resolverUnidade(Usuario usuario, Long unidadeId) {
        if (usuario.getPerfil() == PerfilEnum.MORADOR) {
            Unidade propria = usuario.getUnidade();
            if (unidadeId != null && !unidadeId.equals(propria.getId())) {
                throw erro(HttpStatus.FORBIDDEN, "O morador só pode reservar para a própria unidade.");
            }
            return propria;
        }
        if (usuario.getPerfil() == PerfilEnum.PORTARIA) {
            throw erro(HttpStatus.FORBIDDEN, "A portaria não pode criar reservas.");
        }
        if (unidadeId == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe a unidade (unidadeId) da reserva.");
        }
        return unidadeRepository.findById(unidadeId)
            .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Unidade não encontrada com o ID: " + unidadeId));
    }

    private void validarJanela(Espaco espaco, LocalDateTime inicio, LocalDateTime fim) {
        if (!inicio.isBefore(fim)) {
            throw erro(HttpStatus.BAD_REQUEST, "O início da reserva deve ser anterior ao fim.");
        }
        if (!inicio.toLocalDate().equals(fim.toLocalDate())) {
            throw erro(HttpStatus.BAD_REQUEST, "A reserva deve começar e terminar no mesmo dia.");
        }
        if (inicio.toLocalTime().isBefore(espaco.getHoraAbertura())
                || fim.toLocalTime().isAfter(espaco.getHoraFechamento())) {
            throw erro(HttpStatus.BAD_REQUEST, "Fora do horário de funcionamento do espaço ("
                + espaco.getHoraAbertura() + " às " + espaco.getHoraFechamento() + ").");
        }
        long duracao = Duration.between(inicio, fim).toMinutes();
        if (duracao < espaco.getDuracaoMinimaMinutos() || duracao > espaco.getDuracaoMaximaMinutos()) {
            throw erro(HttpStatus.BAD_REQUEST, "A duração deve ficar entre "
                + espaco.getDuracaoMinimaMinutos() + " e " + espaco.getDuracaoMaximaMinutos() + " minutos.");
        }
    }

    private void validarAntecedencia(Espaco espaco, LocalDateTime inicio) {
        LocalDateTime agora = LocalDateTime.now(clock);
        if (inicio.isBefore(agora.plusHours(espaco.getAntecedenciaMinimaHoras()))) {
            throw erro(HttpStatus.BAD_REQUEST, "Este espaço exige pelo menos "
                + espaco.getAntecedenciaMinimaHoras() + " horas de antecedência.");
        }
        if (inicio.isAfter(agora.plusDays(espaco.getAntecedenciaMaximaDias()))) {
            throw erro(HttpStatus.BAD_REQUEST, "Este espaço só aceita reservas com até "
                + espaco.getAntecedenciaMaximaDias() + " dias de antecedência.");
        }
    }

    private ReservaResponse decidir(Long id, StatusReservaEnum novoStatus) {
        Reserva reserva = buscarReserva(id);
        exigirStatus(reserva, StatusReservaEnum.PENDENTE);
        exigirAntesDoInicio(reserva);
        reserva.setStatus(novoStatus);
        return ReservaResponse.de(reserva);
    }

    private Reserva buscarReserva(Long id) {
        return reservaRepository.findById(id).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Reserva não encontrada com o ID: " + id));
    }

    private void exigirStatus(Reserva reserva, StatusReservaEnum... permitidos) {
        if (!List.of(permitidos).contains(reserva.getStatus())) {
            throw erro(HttpStatus.CONFLICT, "A reserva está " + reserva.getStatus() + " e não permite esta operação.");
        }
    }

    private void exigirAntesDoInicio(Reserva reserva) {
        if (!LocalDateTime.now(clock).isBefore(reserva.getInicio())) {
            throw erro(HttpStatus.BAD_REQUEST, "A reserva já começou!");
        }
    }

    private ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }
}