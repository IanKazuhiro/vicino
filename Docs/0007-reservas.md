# 0007 - Reservas de espaços comuns

- **Status:** aceita e implementada
- **Data:** 2026-10-08

## Contexto

Os espaços comuns guardam as próprias regras: horário de funcionamento, duração, antecedência, limite por unidade e aprovação ([ADR 0006](0006-espacos-comuns-e-janela-de-reserva.md)). Faltava o módulo que aplica essas regras: o morador reserva um horário, o síndico decide quando o espaço exige aprovação, e todos precisam consultar o que está ocupado sem expor dados de outras unidades.

## Decisão

### Quem pode fazer o quê

| Ação | `MORADOR` | `SINDICO` / `ADMINISTRADOR` | `PORTARIA` |
|---|---|---|---|
| Reservar | Só para a própria unidade | Para qualquer unidade (`unidadeId` obrigatório) | Não |
| Aprovar ou recusar | Não | Sim | Não |
| Cancelar | Só da própria unidade, até a antecedência mínima do espaço | Qualquer uma, até o início | Não |
| Ver detalhe (`GET /reservas/{id}`) | Só da própria unidade | Todas | Não |
| Listagem geral (`GET /reservas`) | Não | Sim, com filtros | Não |
| "Minhas reservas" | Da própria unidade | Lista vazia (sem unidade) | Lista vazia |
| Agenda de um espaço | Sim | Sim | Sim |

- **A reserva pertence à unidade, não à pessoa.** O limite e o "minhas reservas" contam por unidade. Quem criou fica registrado em `criado_por`.
- **O perfil é lido do banco a cada requisição,** não do token. Um usuário desativado depois do login recebe 403 nas reservas, mesmo com o token ainda válido.

### Ciclo de vida

```
PENDENTE ──aprovar──▶ CONFIRMADA ──cancelar──▶ CANCELADA
    │                                              ▲
    ├──recusar──▶ RECUSADA                         │
    └──────────────────cancelar────────────────────┘
```

- **Status inicial:** `PENDENTE` só quando o espaço exige aprovação **e** quem reserva é morador; em todos os outros casos, `CONFIRMADA`. Quando o síndico reserva, quem aprovaria é ele mesmo.
- **`PENDENTE` já ocupa o horário,** até o síndico decidir. Assim duas unidades não disputam o mesmo horário, e aprovar nunca gera conflito.
- **`RECUSADA` e `CANCELADA` liberam o horário.**
- **Transição inválida** (por exemplo, aprovar o que já está `CONFIRMADA`) responde **409**. Prazo vencido ou reserva já iniciada responde **400**.

### Regras da criação

Validadas nesta ordem, das mais baratas às que consultam outras reservas:

1. Usuário ativo e unidade resolvida pelo perfil (403, 400, 404).
2. Espaço existe e está ativo (404, 400).
3. Janela: início antes do fim, no mesmo dia, dentro do horário do espaço, com duração entre a mínima e a máxima (400).
4. Antecedência mínima (em horas) e máxima (em dias) (400).
5. Limite de reservas futuras `PENDENTE` e `CONFIRMADA` da unidade naquele espaço (409).
6. Conflito com outra reserva ativa do espaço (409).

O fim é **exclusivo**: 18:00–19:00 e 19:00–20:00 não conflitam.

### Conflito garantido pelo banco

A migration V7 usa a extensão `btree_gist` e uma constraint `EXCLUDE`:

```sql
EXCLUDE USING gist (espaco_id WITH =, tsrange(inicio, fim) WITH &&)
    WHERE (status IN ('PENDENTE', 'CONFIRMADA'))
```

O service verifica o conflito antes, para responder com uma mensagem clara. Mas só o banco vê duas gravações simultâneas em ordem. Por isso o `criar` usa `saveAndFlush` dentro de um `try`, e a violação da constraint vira 409 em vez de 500.

### Datas e fuso

- `inicio` e `fim` são `TIMESTAMP` **sem** fuso (`LocalDateTime`), em hora do condomínio.
- O fuso fica na propriedade `vicino.fuso-horario` (`America/Manaus`), e um bean `Clock` (`ClockConfig`) fornece o "agora" de todas as regras de tempo. Isso evita depender do fuso da máquina: o banco local está em `America/La_Paz`, e o Railway roda em UTC. Também permite testes com `Clock.fixed`.

### Agenda e LGPD

A agenda é vista por qualquer usuário autenticado, inclusive pela portaria e por outros moradores, então devolve **só** `inicio`, `fim` e `status` (`AgendaItemResponse`). Quem reservou fica visível apenas para a própria unidade e para o síndico ou administrador.

## Alternativas consideradas

- **Reserva por dia inteiro ou por turnos fixos:** descartada no [ADR 0006](0006-espacos-comuns-e-janela-de-reserva.md); a janela configurável cobre os dois casos.
- **`PENDENTE` sem ocupar o horário:** várias unidades poderiam pedir o mesmo horário, e aprovar uma exigiria recusar as outras. É mais complexo e confunde os moradores.
- **Conflito verificado só no service:** duas requisições simultâneas poderiam passar pela verificação ao mesmo tempo e gravar reservas sobrepostas.
- **`TIMESTAMPTZ` para o período:** exigiria converter fusos em toda consulta por dia. A hora local do condomínio é o que o morador escolhe e o que a portaria vê.
- **Edição de reservas (`PUT`):** fica fora do MVP. Para mudar o horário, o morador cancela e reserva de novo.

## Consequências

- **A reserva não atravessa a meia-noite,** como a janela do espaço.
- **Mudar as regras de um espaço não altera reservas já feitas,** mas o prazo de cancelamento usa a antecedência mínima **atual** do espaço.
- **Um único fuso por instalação:** coerente com o condomínio único do [ADR 0002](0002-condominio-unico-no-mvp.md).
- **A extensão `btree_gist` precisa estar disponível** no PostgreSQL de cada ambiente. Está no local; é preciso confirmar no Railway antes do deploy.
- **As listagens fazem uma consulta extra por reserva** para carregar espaço, unidade e bloco (`LAZY`). Com o volume de um condomínio isso é aceitável; um `@EntityGraph` resolve se for preciso.
- **Os testes ficam numa coleção do Postman** ("Vicino - Reservas (transições e consultas)"), rodada pelo Collection Runner. Ainda não há testes automatizados no projeto.
