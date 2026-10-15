# 0008 - Visitantes e LGPD

- **Status:** aceita e implementada
- **Data:** 2026-10-14

## Contexto

O segundo pilar do MVP é a liberação de visitantes ([Visão e escopo](Visao&Escopo.md)). Hoje a portaria não tem como saber quem está autorizado a entrar, nem registrar quem entrou e saiu. Ao mesmo tempo, o módulo guarda dados pessoais de terceiros, como nome, documento, telefone e placa, de pessoas que não são usuárias do sistema. Pela LGPD, esses dados só podem ser guardados enquanto forem necessários.

## Decisão

### Quem pode fazer o quê

| Ação | `MORADOR` | `SINDICO` / `ADMINISTRADOR` | `PORTARIA` |
|---|---|---|---|
| Autorizar visitante | Só para a própria unidade | Para qualquer unidade (`unidadeId` obrigatório) | Não |
| Cancelar autorização | Só da própria unidade | Qualquer uma | Não |
| "Minhas autorizações" | Da própria unidade | Lista vazia (sem unidade) | Lista vazia |
| Buscar autorizações válidas agora | Não | Sim | Sim |
| Registrar entrada e saída | Não | Sim | Sim |
| Ver quem está no condomínio | Não | Sim | Sim |

- **A autorização pertence à unidade,** como a reserva ([ADR 0007](0007-reservas.md)). Quem criou fica registrado em `criado_por`.
- **Quem registrou cada entrada e saída também fica gravado,** para a administração saber qual porteiro liberou cada acesso.
- **A autorização não passa por aprovação do síndico:** ela já nasce `ATIVA`.

### Período da autorização

- **Início e fim definidos por quem autoriza,** com no máximo **30 dias**. Um único modelo cobre a visita única (sábado das 18h às 23h) e a recorrente (a diarista durante a semana).
- **O início pode ser imediato ou já ter passado,** para autorizar quem chegou à portaria agora. Só o fim precisa estar no futuro.
- **Diferente das reservas, o período pode atravessar a meia-noite.**
- **"Expirada" não é um status gravado:** é calculada comparando o `fim` com o momento atual. Os status gravados são só `ATIVA` e `CANCELADA`.
- **Cancelar não expulsa ninguém:** se o visitante já entrou, a portaria ainda registra a saída.

### Entrada e saída

```
autorização ATIVA, dentro do período
        │
        ├──entrada──▶ acesso aberto ──saída──▶ acesso encerrado
        │                                          │
        └──────────── nova entrada ◀───────────────┘
```

- **Várias entradas por autorização:** dentro do período, o visitante pode entrar e sair quantas vezes precisar, e cada entrada gera um acesso.
- **Nunca duas entradas abertas ao mesmo tempo:** o service verifica antes, para dar uma mensagem clara, e um **índice único parcial** garante no banco, mesmo com duas requisições simultâneas:

  ```sql
  CREATE UNIQUE INDEX ux_acesso_aberto ON acesso_visitante (autorizacao_id) WHERE saida_em IS NULL;
  ```

  É o mesmo raciocínio da constraint `EXCLUDE` das reservas. O `registrarEntrada` usa `saveAndFlush` dentro de um `try`, e a violação vira 409.
- **A entrada exige a autorização `ATIVA` (409) e dentro do período (400).** A saída não confere nenhum dos dois, porque o visitante pode sair atrasado ou depois do cancelamento.

### Busca da portaria

- A portaria busca por nome ou documento entre as autorizações `ATIVA` com `inicio ≤ agora < fim`.
- **O documento é gravado e buscado só com letras e números, em maiúsculas.** "123.456.789-00" e "12345678900" encontram o mesmo visitante, e o porteiro não precisa adivinhar como o morador digitou. O nome é comparado sem diferenciar maiúsculas.

### Minimização dos dados (LGPD)

- **Obrigatórios só o nome e o documento,** que são o mínimo para a portaria identificar o visitante. Telefone e placa são opcionais.
- **Quem vê o quê:**
  - o morador vê só as autorizações da própria unidade;
  - a portaria vê nome e documento só das autorizações **válidas agora**, porque precisa conferir na entrada;
  - síndico e administrador veem tudo.
- **A resposta de acesso não traz o documento:** ele já foi conferido na entrada, pela autorização. Para registrar a saída e ver quem está no condomínio, bastam o nome, a placa e a unidade.

### Retenção e anonimização

- **90 dias depois do fim da autorização,** nome, documento, telefone e placa são apagados (gravados como `NULL`), e o `anonimizado_em` registra quando isso aconteceu.
- **A autorização e os acessos continuam:** a unidade, o período e os horários de entrada e saída ficam no banco sem identificar a pessoa. Assim continua possível, por exemplo, saber quantas visitas uma unidade recebeu.
- **Uma tarefa agendada roda todo dia às 03:00, na hora do condomínio:**
  - `AnonimizacaoVisitantesJob`, com `@Scheduled`, ligada por `@EnableScheduling` no `SchedulingConfig`;
  - o horário fica em `vicino.visitantes.anonimizacao-cron` e o prazo em `vicino.visitantes.retencao-dias`;
  - o `zone` do `@Scheduled` usa o fuso do condomínio, para não depender do fuso da máquina (o Railway roda em UTC).
- **A regra fica no service** (`VisitanteService.anonimizarExpiradas()`), e a tarefa só a chama e registra no log. O "agora" vem do `Clock` ([ADR 0007](0007-reservas.md)), o que permite testar com `Clock.fixed`.
- **O banco protege a regra:** a constraint `ck_autorizacao_dados` exige nome e documento enquanto `anonimizado_em` estiver vazio. Uma autorização só fica sem esses dados se tiver sido anonimizada.

## Alternativas consideradas

- **Validade por tempo de permanência** (por exemplo, "4 horas a partir da entrada"): não cobre a diarista que vem a semana toda, e obrigaria o morador a calcular horários.
- **Status `EXPIRADA` gravado:** exigiria uma tarefa para atualizar o status na virada do horário, e o dado ficaria errado até ela rodar. Calcular pelo `fim` é sempre exato.
- **Apagar a autorização inteira depois do prazo:** perderia o histórico de acessos, que não identifica ninguém depois da anonimização e continua útil para a administração.
- **Um registro por visita, sem separar autorização e acesso:** não permite várias entradas na mesma autorização, nem saber quem está no condomínio agora.
- **Anonimizar na própria consulta** (esconder os dados depois de 90 dias, mas mantê-los no banco): não atende a LGPD, porque o dado continuaria guardado.
- **Guardar o documento como foi digitado:** a busca da portaria falharia sempre que o porteiro digitasse com uma pontuação diferente da do morador.

## Consequências

- **Os dados pessoais saem do banco em até 1 dia depois de completar 90 dias** do fim da autorização, porque a tarefa roda uma vez por dia.
- **A anonimização não tem volta:** depois dela, não há como saber quem foi o visitante. Se algum dia for preciso guardar por mais tempo, por exemplo por exigência legal, basta mudar `vicino.visitantes.retencao-dias`; as autorizações já anonimizadas continuam assim.
- **As respostas da API trazem `nome` e `documento` nulos** nas autorizações anonimizadas, e o `anonimizadoEm` explica o motivo.
- **Se a aplicação estiver parada às 03:00,** a anonimização daquele dia não roda, mas acontece na próxima execução, porque a busca pega todas as autorizações vencidas que ainda não foram anonimizadas.
- **Com mais de uma instância da aplicação,** a tarefa rodaria em todas. É inofensivo, porque a segunda execução não encontra nada para anonimizar, mas, se o deploy passar a usar várias instâncias, vale um bloqueio distribuído (por exemplo, ShedLock).
- **Os testes ficam em duas coleções do Postman** ("Vicino - Visitantes (autorizações)" e "Vicino - Visitantes (portaria)"). A anonimização foi testada com autorizações inseridas direto no banco e a tarefa rodando a cada minuto.
