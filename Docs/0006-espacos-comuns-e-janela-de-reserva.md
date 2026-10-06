# 0006 - Espaços comuns e janela de reserva configurável

- **Status:** aceita
- **Data:** 2026-10-06

## Contexto

O condomínio tem espaços com usos muito diferentes: a quadra costuma ser reservada por hora, a churrasqueira por algumas horas e o salão de festas praticamente pelo dia todo. O síndico ou o administrador precisa definir, para cada espaço, a janela de tempo e as regras de reserva, sem que isso exija mudanças no código.

## Decisão

Cada espaço guarda as próprias regras, editáveis por `SINDICO` e `ADMINISTRADOR` (`POST /espacos` e `PUT /espacos/{id}`):

| Regra | Campos |
|---|---|
| Horário de funcionamento | `horaAbertura`, `horaFechamento` (`LocalTime`) |
| Duração de cada reserva | `duracaoMinimaMinutos`, `duracaoMaximaMinutos` |
| Antecedência | `antecedenciaMinimaHoras`, `antecedenciaMaximaDias` |
| Limite por unidade | `limiteReservasPorUnidade` (reservas futuras da unidade naquele espaço) |
| Aprovação | `exigeAprovacao`: se ligado, a reserva fica pendente até o síndico aprovar |
| Disponibilidade | `ativo`: desativa o espaço sem apagá-lo |

Com horário e duração mínima e máxima, o mesmo modelo cobre os três formatos:

| Espaço | Horário | Duração | Resultado |
|---|---|---|---|
| Quadra | 08:00–22:00 | 60–60 min | Blocos fixos de 1 hora |
| Churrasqueira | 10:00–22:00 | 120–360 min | De 2 a 6 horas |
| Salão de festas | 09:00–23:59 | 900–900 min | Praticamente o dia inteiro |

**Validações**, em três camadas:

1. **DTO (Bean Validation):** campos obrigatórios, números positivos e tamanhos de texto.
2. **Service (`validarRegras`):** regras que comparam campos entre si:
   - a abertura vem antes do fechamento;
   - a duração mínima não passa da máxima;
   - a duração máxima cabe no horário de funcionamento;
   - a antecedência mínima (em horas) é menor que a máxima (em dias).
3. **Banco (`CHECK` na V6):** última linha de defesa para horário, duração, antecedência e limite.

A edição usa `PUT` com o recurso completo; não há `PATCH` no MVP.

## Alternativas consideradas

- **Reserva por dia inteiro:** simples, mas não atende a quadra.
- **Turnos fixos cadastrados** (manhã, tarde, noite): exige uma tabela de turnos por espaço e é menos flexível para durações variáveis.
- **Regras fixas no código:** qualquer mudança de regra exigiria um novo deploy.

## Consequências

- **A janela não atravessa a meia-noite** (`horaAbertura < horaFechamento`). Uma festa até as 2h da manhã não cabe nesse modelo. Isso simplifica os cálculos de reserva; se surgir a necessidade, o modelo precisará mudar.
- O mesmo horário vale para todos os dias da semana. Não há horários diferentes por dia nem bloqueio de feriados.
- As regras de antecedência, duração e limite são guardadas aqui, mas só passam a ser aplicadas quando o módulo de reservas existir.
- Alterar as regras de um espaço não afeta reservas já feitas. O comportamento exato será definido no módulo de reservas.
- A regra "inadimplente pode reservar?" ficou fora: o controle financeiro não faz parte do MVP.
