# Progresso do projeto

Registro do que foi feito a cada commit, do que está pendente e dos próximos passos. **Atualize este arquivo junto com cada commit.**

## Estado atual

- **Back-end:** cadastro de blocos, unidades e usuários (criar, listar e buscar por id), de **espaços comuns** (criar, editar, listar e buscar por id) e o módulo completo de **reservas** (criar, aprovar, recusar, cancelar, detalhe, minhas, listagem do síndico e agenda). Tudo testado no Postman; as reservas, com uma coleção automatizada.
- **Segurança:** login com JWT (`POST /auth/login`) e controle de acesso por perfil. Cadastros e edições só para `SINDICO` e `ADMINISTRADOR`; consultas para qualquer usuário autenticado, com regras de visibilidade nas reservas (ver [ADR 0004](0004-perfis-e-permissoes.md), [ADR 0005](0005-autenticacao-jwt.md) e [ADR 0007](0007-reservas.md)). Senhas salvas com hash BCrypt.
- **Visitantes:** módulo completo. Autorizações (criar, "minhas" e cancelar), rotas da portaria (busca, entrada, saída e presentes) e anonimização diária dos dados pessoais pela LGPD ([ADR 0008](0008-visitantes-e-lgpd.md)). Testado com duas coleções do Postman e com dados inseridos no banco.
- **Front-end:** em construção. O setup está pronto (etapa 1 de 10), mas ainda não há telas.
  - **Visual:** protótipo aprovado de Login e Home em [`prototipo/`](prototipo/), com a paleta "Vizinhança calorosa" e glassmorphism ([ADR 0009](0009-estilo-do-front-end.md)).
  - **Arquitetura:** React com TypeScript (Vite) e CSS Modules, com contratos híbridos: as telas com back-end usam a API real, e as outras só mock ([ADR 0010](0010-arquitetura-do-front-end.md)).
  - **Construção:** em 10 etapas, com o código escrito pelo Claude e os commits feitos pelo Ian.

## Histórico de commits

### `pendente` (2026-10-15): front-end, etapa 1 (setup e design tokens)

- **Projeto em `frontend/`** montado à mão, sem o assistente interativo do Vite, para controlar cada arquivo.
- **Versões instaladas** (as mais recentes, sem nenhuma vulnerabilidade apontada pelo npm):
  - React 19.3 e React Router 8;
  - Vite 8, TypeScript 6 e Vitest 5;
  - ESLint 10 com typescript-eslint;
  - TanStack Query 5, React Hook Form 7, Zod 4 e date-fns 4;
  - Axios e lucide-react.
- **TypeScript `strict`** com regras extras: variáveis e parâmetros não usados, `verbatimModuleSyntax`, `erasableSyntaxOnly`.
- **Qualidade:** ESLint (regras do React Hooks e do Fast Refresh) com `eslint-config-prettier` no fim, Prettier (aspas simples, vírgula no fim, 100 colunas), e Vitest com jsdom e Testing Library (`src/test/setup.ts`).
- **Scripts:** `dev`, `build`, `lint`, `format`, `format:check`, `test` e `test:watch`.
- **Design system em CSS:**
  - `tokens.css`: cores, tintas, tipografia, raios, espaçamentos, sombras, movimento e medidas;
  - `globals.css`: reset, foco visível em tudo, `.sr-only` e `prefers-reduced-motion`;
  - `glass.css`: as receitas glass, glass-strong, glass-dark, glass-nav e glass-input:
    - com `prefers-reduced-transparency`, o vidro fica mais opaco (70% a 90%) e mantém o desfoque;
    - sem `backdrop-filter`, o fallback é quase opaco.
- **`BackgroundBlobs`:** variantes `login` (4 manchas) e `app` (3), nas posições do protótipo, com uma flutuação lenta desligada por `prefers-reduced-motion`.
- **`index.html`:** `lang="pt-BR"`, fontes Fraunces e Manrope e favicon com a casa com coração.
- **Proxy do Vite:** `/api` → `http://localhost:8080`, sem o prefixo. O `.env.example` traz `VITE_API_URL=/api` e `VITE_USE_MOCKS=true`.
- **Estrutura de pastas do guia** criada, com `.gitkeep` nas pastas ainda vazias.
- **Tela provisória** para conferir o vidro e as fontes. É substituída pelo router na etapa 3.
- **`.claude/launch.json`:** configuração que abre o `npm run dev` no navegador do Claude Code.
- **Verificação:**
  - `lint`, `build` e 2 testes do `BackgroundBlobs` passaram;
  - no navegador, as fontes carregaram, não houve erro no console nem rolagem horizontal em 360 e 1440 px, e o vidro ficou igual ao protótipo.

**Ajuste no fallback:** na primeira versão, o vidro aparecia quase opaco na máquina do Ian.
- **Causa:** o Windows está com **"Efeitos de transparência" desligado**, e o navegador repassa isso como `prefers-reduced-transparency: reduce`. O guia mandava trocar o vidro por branco a 92% nesse caso.
- **Correção:** como essa configuração é comum, a regra ficou mais suave, mantendo o desfoque com mais opacidade (ver o ADR 0009).
- **Conferido no navegador:** fundo a 70% e `blur(20px)`.

- Registro do hash `7408a17` na entrada da etapa 0.

### `7408a17` (2026-10-14): front-end, etapa 0 (protótipo e decisões)

- **`Docs/prototipo/`:** o protótipo aprovado (`prototipo-login.html` e `prototipo-home.html`) e o guia do front-end (`PROMPT-FRONTEND-VICINO.md`), que são a fonte da verdade visual.
- **ADR 0009 (estilo do front-end):**
  - **tecnologia:** React, TypeScript, Vite, **CSS Modules** com variáveis CSS, lucide-react, Fraunces e Manrope;
  - **visual "Vizinhança calorosa":** paleta off-white, oliva, terracota e areia, com o `--color-accent` `#AD5D2F` escurecido para contraste AA;
  - **glassmorphism:** receitas glass, glass-strong, glass-dark e glass-nav, com fallback quase opaco sem `backdrop-filter` ou com `prefers-reduced-transparency`, e manchas desfocadas no fundo;
  - **layout:** barra inferior no celular e lateral a partir de 1024 px.
  
  Uma primeira versão, com Tailwind e um visual plano verde-água escolhido entre três direções, foi substituída no mesmo dia pelo protótipo, antes de qualquer código. O registro está na seção "Revisão" do ADR.
- **ADR 0010 (arquitetura do front-end):**
  - **contratos híbridos:** login, espaços, reservas e visitantes usam as rotas e os DTOs reais; encomendas, avisos, notificações, primeiro acesso e esqueci a senha existem só como mock;
  - **adaptações das telas ao back-end:** horários pela `/reservas/agenda` e status do visitante calculado pelo período;
  - **camada de serviços:** real ou mock por `VITE_USE_MOCKS`;
  - **sessão:** `localStorage` ou `sessionStorage`, 401 leva ao login, e `GET /usuarios/me` novo no back-end;
  - **proxy do Vite:** dispensa o CORS no desenvolvimento;
  - **escopo:** só o morador.
- **Visão e Escopo:** encomendas, mural e notificações aparecem só como demonstração e continuam fora do MVP.
- **README:** a stack do front-end, os ADRs 0009 e 0010 no índice e a pasta do protótipo.
- Registro do hash `21e22a8` na entrada da parte 4 de visitantes.

### `21e22a8` (2026-10-14): visitantes, parte 4 (LGPD e fechamento do módulo)

- **`application.properties`:** `vicino.visitantes.anonimizacao-cron=0 0 3 * * *` (todo dia às 03:00), ao lado da `retencao-dias=90` criada na parte 1.
- **`SchedulingConfig`:** `@EnableScheduling`, o primeiro agendamento do projeto. Sem ele, o Spring ignora o `@Scheduled` em silêncio.
- **`VisitanteService.anonimizarExpiradas()`:**
  - busca as autorizações com `fim` anterior a `agora − 90 dias` e ainda sem `anonimizado_em`;
  - grava `null` em nome, documento, telefone e placa e preenche o `anonimizado_em` com `clock.instant()`, por *dirty checking*;
  - devolve quantas autorizações anonimizou;
  - o prazo vem da propriedade, com o `@Value` do Spring num campo não `final`, como no `TokenService`.
- **`AnonimizacaoVisitantesJob`** (pacote `config`, ao lado do `AdminInicial`):
  - `@Scheduled(cron = "${vicino.visitantes.anonimizacao-cron}", zone = "${vicino.fuso-horario}")`. O `zone` faz as 03:00 serem na hora do condomínio, também no Railway, que roda em UTC;
  - só chama o service e registra no log quantas autorizações foram anonimizadas.
- **Teste da anonimização:**
  - três autorizações inseridas direto no banco: uma vencida há 91 dias, com um acesso; uma vencida há 89 dias; e uma que terminou há 1 hora;
  - a aplicação subiu com a variável de ambiente `VICINO_VISITANTES_ANONIMIZACAOCRON="0 * * * * *"`, para a tarefa rodar a cada minuto sem mudar o arquivo;
  - resultado: só a de 91 dias foi anonimizada (nome, documento, telefone e placa nulos e `anonimizado_em` preenchido), e o acesso dela continuou com os horários e quem registrou; a de 89 dias manteve os dados; nas execuções seguintes, nada mais foi alterado.
- **Pendência da parte 2 resolvida:** cancelar a autorização que já tinha terminado respondeu 400 ("A autorização já terminou."), e ela continuou `ATIVA` no banco.
- **Documentação:**
  - ADR 0008 (visitantes e LGPD);
  - `api.md` com a anonimização e o link para o ADR;
  - Visão e Escopo com as três perguntas sobre visitantes respondidas;
  - este arquivo e o README.
- Registro dos hashes `5ebce59` e `73b250a` na entrada da parte 3.

### `5ebce59` e `73b250a` (2026-10-11): visitantes, parte 3 (portaria)

A parte 3 foi para o GitHub em dois commits com a mesma mensagem. O `5ebce59` levou o código e o `api.md`; o `73b250a`, este arquivo e o README, que tinham ficado de fora porque o `git add` foi rodado dentro de `backend/`.

- **Documento normalizado:** gravado e buscado só com letras e números, em maiúsculas ("123.456.789-00" vira "12345678900"). Assim o porteiro encontra o visitante digitando o documento com ou sem pontuação. Um documento que fica vazio depois da limpeza (por exemplo, "---") dá 400. As 3 autorizações de teste anteriores, todas `CANCELADA`, ficaram com a pontuação; como nunca aparecem na busca, não foi preciso migration.
- **`AutorizacaoVisitanteRepository.buscarValidas`:** recebe dois parâmetros, o `nome` (texto como digitado, comparado sem diferenciar maiúsculas) e o `documento` (texto sem pontuação).
- **`VisitanteService`:**
  - **`buscarValidas(busca)`:**
    - sem busca, lista todas as autorizações válidas agora;
    - uma busca só com pontuação usa o próprio texto no lugar do documento, para não virar `''` e listar tudo.
  - **`registrarEntrada`:**
    - autorização existente (404), `ATIVA` (409) e dentro do período (400);
    - sem outra entrada aberta (409), verificado com `existsBy...` e garantido pelo índice único `ux_acesso_aberto`: o `saveAndFlush` dentro do `try` converte a violação em 409, como nas reservas;
    - grava quem registrou a entrada.
  - **`registrarSaida`:** acesso existente (404) e ainda sem saída (409). Não confere período nem status, porque o visitante pode sair atrasado ou depois do cancelamento. Grava a saída e quem a registrou por *dirty checking*.
  - **`presentes`:** acessos sem saída, da entrada mais antiga para a mais recente.
  - Mudança em relação ao plano: uma entrada numa autorização que não está `ATIVA` responde **409**, e não 400, seguindo a convenção do projeto (status que não permite a operação → 409; horário → 400).
- **`VisitanteController`:**
  - `GET /visitantes/autorizacoes/validas?busca=` (opcional);
  - `POST /visitantes/autorizacoes/{id}/entrada` (201, sem `Location`);
  - `POST /visitantes/acessos/{id}/saida` (200);
  - `GET /visitantes/presentes`.
- **`SecurityConfig`:** as quatro rotas só para `PORTARIA`, `SINDICO` e `ADMINISTRADOR`. O morador recebe 403 (LGPD: a busca e os presentes mostram visitantes de todas as unidades).
- **Testes:** coleção nova no Postman, "Vicino - Visitantes (portaria)", com 32 requisições e 63 testes, todos aprovados no Collection Runner. Os cenários:
  - documento gravado sem pontuação e documento só com pontuação (400);
  - busca pelo documento com pontuação, pelo nome em minúsculas, sem parâmetro e só com pontuação, sem mostrar autorizações futuras nem canceladas;
  - morador barrado na busca, nos presentes, na entrada e na saída (403);
  - entrada (201), entrada repetida (409), fora do período (400), em autorização cancelada (409) e inexistente (404);
  - presentes com o visitante e sem o documento;
  - saída (200), saída repetida (409) e de acesso inexistente (404);
  - nova entrada depois da saída (201);
  - cancelamento com o visitante dentro: ele continua nos presentes, e o síndico registra a saída (200);
  - limpeza.

  O banco confirmou:
  - 3 autorizações criadas, com o documento sem pontuação e a placa em maiúsculas;
  - 2 acessos na mesma autorização, sem nenhum acesso extra das entradas recusadas, com a portaria registrando a entrada e a primeira saída e o síndico registrando a segunda;
  - nenhum acesso aberto nem autorização `ATIVA` ao final.
- **Documentação:** seção da portaria e formato do acesso no `api.md`, permissões, este arquivo e o README.
- Registro do hash `d193f7e` na entrada da documentação da parte 2.

### `d193f7e` (2026-10-10): documentação da parte 2 de visitantes

- A documentação da parte 2 não entrou no commit `f61cba5`, que levou só o código. Este commit traz a entrada abaixo, a seção de visitantes no `api.md`, o README e o registro do hash `6342ff2`.

### `f61cba5` (2026-10-10): visitantes, parte 2 (autorizações)

- **`VisitanteService`:**
  - **`autorizar`:**
    - `buscarUsuario` e `resolverUnidade` foram **repetidos** do `ReservaService`, de propósito: são poucas linhas, as mensagens são diferentes ("reservar" e "autorizar visitantes"), e extrair agora obrigaria a mexer no `ReservaService`, que já está testado. A extração fica para quando um terceiro módulo precisar da mesma regra.
    - O morador autoriza só para a própria unidade (403 para outra). Síndico e admin informam o `unidadeId` (400 se faltar, 404 se não existir). A portaria recebe 403.
    - `validarPeriodo`: o início é anterior ao fim, o fim está no futuro e a duração é de no máximo 30 dias (400). O início pode já ter passado.
    - Nome e documento com `trim()`. O telefone e a placa passam por `limpar` (texto em branco vira `null`), e a placa é gravada em maiúsculas.
    - Status sempre `ATIVA`, gravado com `save`, porque não há constraint de conflito para converter em 409.
  - **`minhas`:** autorizações da unidade, do início mais recente para o mais antigo. Quem não tem unidade recebe uma lista vazia.
  - **`cancelar`:**
    - A portaria recebe 403, e o morador só cancela autorizações da própria unidade (403).
    - A autorização precisa estar `ATIVA` (409) e o período não pode ter terminado (400). O cancelamento durante o período é permitido.
    - O novo status é gravado por *dirty checking*.
- **`VisitanteController`:** `@RequestMapping("/visitantes")`, já pensando nas rotas da portaria da parte 3.
  - `POST /visitantes/autorizacoes` devolve 201 **sem `Location`**, porque não há `GET` por id.
  - `GET /visitantes/autorizacoes/minhas`.
  - `POST /visitantes/autorizacoes/{id}/cancelar`.
- **`SecurityConfig`:** `POST /visitantes/autorizacoes` só para `MORADOR`, `SINDICO` e `ADMINISTRADOR`. As outras duas rotas exigem só autenticação, e o service confere unidade e perfil.
- **Testes:** coleção nova no Postman, "Vicino - Visitantes (autorizações)", com 31 requisições e 57 testes, todos aprovados no Collection Runner. Os cenários:
  - criação com `trim`, placa em maiúsculas e telefone vazio virando `null` (201);
  - início no passado (201);
  - fim antes do início, fim no passado e mais de 30 dias (400);
  - sem documento e nome em branco (400);
  - morador para outra unidade (403), portaria (403) e sem token (401);
  - síndico sem unidade (400), com unidade inexistente (404) e para a unidade 102 (201);
  - "minhas" do morador, só com a própria unidade e em ordem, e "minhas" vazia para síndico e portaria;
  - cancelamento de outra unidade (403), pela portaria (403), durante o período (200), repetido (409) e de autorização inexistente (404);
  - limpeza: o síndico cancela as autorizações restantes.

  O banco confirmou 3 autorizações criadas, com os valores normalizados, e 3 atualizações para `CANCELADA`. As requisições recusadas não gravaram nada.
- **Documentação:** ficou para o commit seguinte (ver a entrada acima).

### `6342ff2` (2026-10-10): documentação da parte 1 de visitantes

- A documentação da parte 1 não entrou no commit `a357750`, porque as alterações se perderam antes dele. Este commit traz a entrada abaixo, o "Estado atual", os "Próximos passos" e o README.
- Registro do hash `9ce12cf` na entrada das reservas.

### `a357750` (2026-10-10): visitantes, parte 1

Base do módulo de visitantes, ainda sem endpoints. Decisões do módulo:
- **Quem autoriza:** o `MORADOR`, para a própria unidade; `SINDICO` e `ADMINISTRADOR`, para qualquer unidade. A `PORTARIA` não autoriza, só registra a entrada e a saída.
- **Período:** início e fim definidos por quem autoriza, com no máximo 30 dias. Diferente das reservas, o período pode atravessar a meia-noite. Cobre a visita única (sábado das 18h às 23h) e a recorrente (a diarista durante a semana).
- **Dados do visitante:** nome e documento obrigatórios; telefone e placa opcionais.
- **Várias entradas por autorização:** o visitante pode entrar e sair mais de uma vez dentro do período, mas nunca com duas entradas abertas ao mesmo tempo.
- **LGPD:** 90 dias depois do fim da autorização, nome, documento, telefone e placa são anonimizados. Os horários de entrada e saída continuam, sem identificar a pessoa.

Feito nesta parte:
- **Propriedade `vicino.visitantes.retencao-dias=90`:** o prazo de retenção dos dados pessoais, usado pela tarefa de anonimização (parte 4).
- **`StatusAutorizacaoEnum`:** `ATIVA` e `CANCELADA`. "Expirada" não é gravada: é calculada comparando o `fim` com o momento atual.
- **Migration V8** (`V8__criar_visitantes.sql`), escrita com a aplicação parada:
  - tabela `autorizacao_visitante`:
    - unidade, `criado_por` e os dados do visitante;
    - `inicio` e `fim` (`TIMESTAMP` sem fuso, em hora do condomínio), status, `criado_em` e `anonimizado_em`;
    - `CHECK` de período e de status;
    - `CHECK` `ck_autorizacao_dados`: nome e documento são obrigatórios até a anonimização;
    - índices em `unidade_id` e em `(inicio, fim)`.
  - tabela `acesso_visitante`:
    - autorização, `entrada_em` e `saida_em`, e quem registrou cada uma;
    - `CHECK` de que a saída não vem antes da entrada, e de que a saída e quem a registrou são preenchidos juntos.
  - **índice único parcial** `ux_acesso_aberto` (`WHERE saida_em IS NULL`): o próprio banco impede duas entradas abertas para a mesma autorização, como o `EXCLUDE` faz nas reservas.
- **Entidades `AutorizacaoVisitante` e `AcessoVisitante`:**
  - `@ManyToOne` `LAZY`;
  - `nome` e `documento` sem `nullable = false`, porque a anonimização grava `null` neles;
  - `registradoSaidaPor` opcional;
  - `entradaEm` preenchida pelo service com o `Clock`, sem `@PrePersist`.
- **Repositories:**
  - `AutorizacaoVisitanteRepository`:
    - autorizações da unidade;
    - `buscarValidas`, com `@Query` que filtra as autorizações `ATIVA` com `inicio ≤ agora < fim` por nome (sem diferenciar maiúsculas) ou documento;
    - autorizações a anonimizar.
  - `AcessoVisitanteRepository`: entrada aberta de uma autorização e lista de quem está no condomínio.
- **DTOs:**
  - `AutorizacaoVisitanteRequest`, com `@Size` nos mesmos limites das colunas;
  - `AutorizacaoVisitanteResponse`, com `anonimizadoEm` para explicar nome e documento nulos;
  - `AcessoVisitanteResponse`, usado na entrada, na saída e na lista de presentes, sem o documento (LGPD).
- Conferido no banco: V8 registrada com sucesso, colunas, constraints e índices criados. A aplicação sobe com `ddl-auto=validate`, então as entidades batem com as tabelas.

### `9ce12cf` (2026-10-08): reservas, parte 3 (consultas e fechamento do módulo)

- **`ReservaService`:**
  - `buscarPorId`: o morador só vê reservas da própria unidade, a portaria não vê detalhes (403), e síndico e admin veem todas;
  - `minhas`: reservas da unidade do usuário, da mais recente para a mais antiga, ou lista vazia para quem não tem unidade;
  - `listar`: usa a `@Query` com filtros opcionais de espaço e status;
  - `agenda`: reservas `PENDENTE` e `CONFIRMADA` que ocupam o dia, devolvidas como `AgendaItemResponse`, sem dados da unidade (LGPD).
- **`ReservaController`:** `GET /reservas/{id}`, `/minhas`, `GET /reservas?espacoId=&status=` e `/agenda?espacoId=&data=`, esta com `@DateTimeFormat(iso = DATE)`. Com o `GET /reservas/{id}`, o `Location` devolvido pela criação passa a funcionar.
- **`SecurityConfig`:** `GET /reservas` (listagem geral) só para `SINDICO` e `ADMINISTRADOR`.
- **Coleção do Postman,** agora "Vicino - Reservas (transições e consultas)", com 46 requisições: 16 novos testes de consulta, todos aprovados. Os testes cobrem:
  - o `Location`;
  - o detalhe para dono, outra unidade e portaria;
  - reserva inexistente;
  - "minhas" para morador e para síndico;
  - os filtros da listagem e o status inválido;
  - a agenda sem dados da unidade, sem canceladas e recusadas e em ordem;
  - a agenda com espaço inexistente e sem a data.
- **Documentação:** ADR 0007 (reservas), `api.md` com todas as rotas de reservas, este arquivo e o README.

### `ff6b679` (2026-10-08): reservas, parte 3 (rotas das transições)

- **`ReservaController`:** `POST /reservas/{id}/aprovar`, `/recusar` e `/cancelar`. Ações sem corpo, que devolvem 200 com a reserva atualizada. Só o `cancelar` recebe o usuário logado (`@AuthenticationPrincipal Jwt`), porque precisa saber quem está cancelando.
- **`SecurityConfig`:** aprovar e recusar só para `SINDICO` e `ADMINISTRADOR`. O cancelamento fica para qualquer usuário autenticado, e o service confere o dono e o prazo.
- **`ReservaService`:** mensagens do `cancelar` corrigidas.
- **Testes automatizados no Postman:** coleção "Vicino - Reservas (transições)", no workspace do Postman, rodada pelo Collection Runner.
  - Ela prepara os dados que faltam: unidade 102, usuário da portaria e espaço "Salão de Festas" com aprovação.
  - Ela executa os cenários e cancela as reservas criadas no fim, para poder ser rodada de novo.
  - As senhas ficam só no *Current value* das variáveis, nunca na coleção sincronizada.
- Cenários aprovados:
  - aprovar uma `PENDENTE` (200 `CONFIRMADA`) e aprovar de novo (409);
  - morador tentando aprovar (403);
  - recusar (200 `RECUSADA`);
  - morador cancelando reserva de outra unidade (403) e portaria cancelando (403);
  - morador cancelando fora do prazo (400, com a Quadra temporariamente em 48 h);
  - cancelar dentro do prazo (200 `CANCELADA`), com o horário liberado para nova reserva (201);
  - cancelar de novo (409);
  - síndico reservando espaço com aprovação já `CONFIRMADA` (201).

  O banco confirmou os status finais de todas as reservas.

### `941f5dd` (2026-10-08): reservas, parte 3 em andamento (transições no service)

- **`ReservaService`:**
  - `aprovar` e `recusar` usam o auxiliar `decidir`: a reserva precisa estar `PENDENTE` (409) e não pode ter começado (400);
  - `cancelar`: a portaria recebe 403; o morador só cancela reservas da própria unidade (403) e até `inicio − antecedenciaMinimaHoras` (400); síndico e admin cancelam até o início (400); o status precisa ser `PENDENTE` ou `CONFIRMADA` (409);
  - auxiliares `buscarReserva` (404), `exigirStatus` (varargs, 409) e `exigirAntesDoInicio` (400);
  - as transições usam *dirty checking*, sem `save()`.
- Ainda sem endpoints neste commit; as rotas e os testes vieram no commit seguinte.

### `3eb12d1` (2026-10-07): reservas, parte 2 (criação)

- **`ReservaRequest`:** o componente `id` foi renomeado para `espacoId`.
- **`ReservaService.criar(usuarioId, req)`:**
  - carrega o usuário logado do banco, e um usuário inativo recebe 403 mesmo com token válido;
  - `resolverUnidade`: o morador usa a própria unidade (403 se informar outra), a portaria recebe 403, e síndico e admin precisam informar `unidadeId` (400 se faltar, 404 se não existir);
  - o espaço precisa existir (404) e estar ativo (400);
  - `validarJanela`: início antes do fim, mesmo dia, dentro do horário do espaço e duração entre a mínima e a máxima (400);
  - `validarAntecedencia` com o `Clock` do condomínio (400);
  - limite de reservas futuras por unidade (409) e conflito de horário (409);
  - status `PENDENTE` só quando o espaço exige aprovação e quem reserva é morador; nos demais casos, `CONFIRMADA`;
  - `saveAndFlush` dentro de `try`, convertendo a violação da constraint `EXCLUDE` (reservas simultâneas) em 409.
- **`ReservaController`:** `POST /reservas`. O usuário logado vem de `@AuthenticationPrincipal Jwt` (`sub` = id do usuário). Responde 201 com `Location`.
- **`SecurityConfig`:** `POST /reservas` só para `MORADOR`, `SINDICO` e `ADMINISTRADOR`.
- Testado no Postman: criação confirmada (201), conflito (409), horário vizinho com fim exclusivo (201), limite por unidade (409), fora do horário (400), duração inválida (400), antecedência mínima e máxima (400), espaço com aprovação gerando `PENDENTE` (201) e portaria barrada (403).

**Observação:** o `Location` aponta para `/reservas/{id}`, que ainda não tem `GET`. O link passa a funcionar quando esse endpoint existir.

### `7d5e54f` (2026-10-07): reservas, parte 1

Base do módulo de reservas, ainda sem endpoints. Decisões do módulo:
- **Quem reserva:** `MORADOR` para a própria unidade; `SINDICO` e `ADMINISTRADOR` para qualquer unidade, e nesse caso a reserva já nasce `CONFIRMADA`. A `PORTARIA` não reserva.
- **Ocupação:** reserva `PENDENTE` bloqueia o horário até o síndico decidir; `RECUSADA` e `CANCELADA` liberam.
- **Período:** a reserva começa e termina no mesmo dia, com fim exclusivo (08:00–09:00 e 09:00–10:00 não conflitam).
- **Limite:** o limite por unidade conta as reservas futuras `PENDENTE` e `CONFIRMADA`.
- **Cancelamento:** o morador cancela só até a antecedência mínima do espaço.
- **Agenda:** mostra os horários ocupados sem identificar a unidade (LGPD).
- **Fuso:** `America/Manaus`.

Feito nesta parte:
- **Fuso e relógio:** propriedade `vicino.fuso-horario=America/Manaus` e `ClockConfig` com um bean `Clock`. Todo "agora" do módulo usa `LocalDateTime.now(clock)`, para não depender do fuso da máquina (o banco local está em `America/La_Paz` e o Railway roda em UTC).
- **`StatusReservaEnum`:** `PENDENTE`, `CONFIRMADA`, `RECUSADA` e `CANCELADA`.
- **Migration V7** (`V7__criar_reserva.sql`):
  - extensão `btree_gist`;
  - tabela `reserva` com espaço, unidade, `criado_por`, `inicio` e `fim` (`TIMESTAMP` sem fuso, em hora do condomínio), status e `criado_em`;
  - `CHECK` de período e de status;
  - constraint **`EXCLUDE`** `ex_reserva_sem_conflito`, que faz o próprio PostgreSQL impedir reservas ativas sobrepostas no mesmo espaço, mesmo com requisições simultâneas.
- **Entidade `Reserva`:** três `@ManyToOne` `LAZY` (espaço, unidade e `criadoPor`), `LocalDateTime` para o período, status como texto e `criadoEm` com `@PrePersist`.
- **`ReservaRepository`:**
  - consultas derivadas para conflito de horário, limite por unidade, "minhas reservas" e agenda do dia;
  - `buscar` com `@Query` em JPQL e filtros opcionais, para a listagem do síndico. É a primeira consulta escrita à mão no projeto.
- **DTOs:** `ReservaRequest`, `ReservaResponse` (com a unidade como texto, por exemplo "Bloco A - 101") e `AgendaItemResponse` (sem dados da unidade).
- **Esqueletos vazios:** `ReservaService` e `ReservaController` (preenchidos na parte 2).

### `aaab907` (2026-10-06): hash do commit de espaços no progresso

- Registro do hash `7a660cb` neste arquivo.

### `7a660cb` (2026-10-06): espaços comuns

- **Migration V6** (`V6__criar_espacos.sql`): tabela `espaco`, com nome único e constraints `CHECK` para horário, duração, antecedência e limite por unidade.
- **Entidade `Espaco`** com horário de funcionamento (`LocalTime`), duração mínima e máxima, antecedência mínima e máxima, limite de reservas por unidade, `exigeAprovacao` e `ativo`.
- **`EspacoRepository`** com `existsByNome` e `existsByNomeAndIdNot` (usado na edição).
- **`EspacoRequest` e `EspacoResponse`**.
- **`EspacoService`:**
  - criar e atualizar com nome único (409);
  - `validarRegras`: abertura antes do fechamento, duração mínima ≤ máxima, duração máxima dentro do horário e antecedência mínima < máxima (400);
  - atualização via *dirty checking*, sem `save()`;
  - listagem em ordem alfabética.
- **`EspacoController`:** `POST /espacos`, `PUT /espacos/{id}` (primeiro endpoint de edição do projeto), `GET /espacos` e `GET /espacos/{id}`.
- **`SecurityConfig`:** `POST /espacos` e `PUT /espacos/*` só para `SINDICO` e `ADMINISTRADOR`.
- **`application.properties`:** `spring.web.error.include-message=always`, para que os erros tragam o motivo no campo `message`.
- **Documentação:** ADR 0006 (espaços e janela de reserva), revisão do ADR 0001 (pacotes por camada), `api.md` e README.
- Testado no Postman: criação (201), nome repetido (409), as quatro regras do `validarRegras` (400), edição sem trocar o nome (200), edição com nome de outro espaço (409), espaço inexistente (404), POST e PUT por morador (403) e consultas por morador (200).

**Incidente durante o desenvolvimento:** a V6 foi salva vazia com a aplicação rodando, e o devtools reiniciou a aplicação, registrando a V6 com checksum 0 sem criar a tabela. Corrigido localmente removendo a linha da V6 do `flyway_schema_history` e rodando a migration de novo. A V3 está vazia pelo mesmo motivo. **Regra:** escrever migrations com a aplicação parada.

### `9ce95c0` (2026-10-05): hash do commit de autenticação no progresso

- Registro do hash `a1963a8` neste arquivo.

### `a1963a8` (2026-10-05): autenticação com JWT

- **Dependência:** `spring-security-crypto` trocado por `spring-boot-starter-security-oauth2-resource-server`.
- **`SecurityConfig`:**
  - API sem sessão (`STATELESS`) e sem CSRF.
  - Rotas públicas: `POST /auth/login`, `/actuator/health` e `/error`.
  - `POST` em `/blocos`, `/unidades` e `/usuarios` só para `SINDICO` e `ADMINISTRADOR`; o resto exige autenticação.
  - `JwtEncoder` e `JwtDecoder` com HS256, usando a chave do `JWT_SECRET`.
  - `JwtAuthenticationConverter` que transforma o claim `perfil` em `ROLE_<PERFIL>`.
- **`TokenService`:** gera o token com `iss`, `sub` (id do usuário), `iat`, `exp` (60 min) e `perfil`.
- **Login:** `LoginRequest`, `LoginResponse`, `AuthService` e `AuthController` (`POST /auth/login`). E-mail inexistente, usuário inativo e senha errada devolvem o mesmo 401.
- **`AdminInicial`:** cria o primeiro `ADMINISTRADOR` na inicialização a partir de `ADMIN_EMAIL` e `ADMIN_SENHA`, se ainda não existir nenhum. Novo método `existsByPerfil` no `UsuarioRepository`.
- **Configuração:** novas propriedades `vicino.jwt.secret`, `vicino.jwt.expiracao-minutos`, `vicino.admin.email` e `vicino.admin.senha`; variáveis `JWT_SECRET`, `ADMIN_EMAIL` e `ADMIN_SENHA` no `.env.example`.
- **Documentação:** ADR 0005, `api.md` com autenticação e permissões por rota, README e ADRs 0003 e 0004 atualizados.
- Testado no Postman: login (200 e 401), acesso sem token (401), cadastro por síndico e administrador (201), cadastro por morador e portaria (403), token adulterado (401) e criação única do administrador inicial.

### `2f864f6` (2026-10-04): documentação atualizada

- README com status, perfis, estrutura de pacotes, uso do `.env`, resumo da API e índice da documentação.
- Novos documentos: `progresso.md` (este arquivo) e `api.md`.
- ADR 0003 (BCrypt antes da autenticação completa) e ADR 0004 (perfis e permissões).
- Visão e escopo: mapeamento dos atores para os perfis e perguntas já respondidas.

### `6510edd` (2026-10-04): controllers e services de Bloco, Unidade e Usuário com BCrypt

- **Bloco:** `BlocoController` com `POST /blocos`, `GET /blocos` e `GET /blocos/{id}`.
- **Unidade:** `UnidadeRequest`, `UnidadeResponse`, `UnidadeService` e `UnidadeController`. O bloco precisa existir (404) e o número não pode repetir dentro do mesmo bloco (409). `GET /unidades` aceita o filtro opcional `?blocoId=`.
- **Usuário:** `UsuarioRequest`, `UsuarioResponse`, `UsuarioService` e `UsuarioController`.
  - E-mail único, normalizado com `trim()` e minúsculas (409).
  - A unidade informada precisa existir (404).
  - O perfil `MORADOR` exige unidade (400).
  - A senha é salva como hash BCrypt, e a resposta nunca devolve senha nem hash.
- **Configuração:** dependência `spring-security-crypto` no `pom.xml` e `PasswordConfig` com o bean `PasswordEncoder`.

### `9c7bb8a` (2026-10-04): DTOs e regras de negócio de Bloco

- `BlocoRequest` e `BlocoResponse`.
- `BlocoService`: nome de bloco não pode repetir (409); `buscarPorId` devolve 404 quando não encontra.
- Novos métodos de busca no `UsuarioRepository`.

### `911f056` (2026-10-04): estrutura inicial do back-end

- Projeto Spring Boot 4.1.1 com Java 25, Maven, Spring Data JPA, Flyway e PostgreSQL.
- Entidades `Bloco`, `Unidade` e `Usuario`, além do `PerfilEnum`.
- Migrations V1 a V5 (tabelas `bloco`, `unidade` e `usuario`, e ajuste do enum de perfis).
- Repositories das três entidades.
- ADRs 0001 (monolito modular) e 0002 (condomínio único no MVP), e o documento de visão e escopo.

## Pendências conhecidas

- [ ] **Tamanho do nome do bloco desalinhado:** a coluna tem 255 caracteres (V1), a entidade `Bloco` declara 50 e o `BlocoRequest` aceita até 100. Escolher um valor único e alinhar os três (a mudança no banco exige uma nova migration).
- [ ] **Mensagem de 409 do `BlocoService`:** falta um espaço entre o texto e o nome do bloco (`"...nome." + nome`).
- [ ] **`BlocoService` sem `readOnly = true`** nas consultas, ao contrário de Unidade e Usuário.
- [ ] **Erros de validação (400)** saem no formato padrão do Spring. Avaliar um tratamento global com `@RestControllerAdvice` para padronizar as mensagens.
- [ ] **Sem testes automatizados** além do teste de contexto gerado pelo Spring. O teste de contexto agora também depende do `JWT_SECRET` configurado.
- [ ] **Respostas 401 e 403 sem corpo:** padronizar junto com o tratamento global de erros.
- [ ] **`GET /usuarios` aberto a qualquer usuário autenticado:** morador e portaria conseguem ver nome e e-mail de todos os usuários. Avaliar restrição por causa da LGPD (ponto em aberto do [ADR 0004](0004-perfis-e-permissoes.md)).
- [ ] **Usuário desativado mantém acesso** até o token expirar (no máximo 60 minutos). Aceitável no MVP; não existe endpoint para desativar usuários ainda. O módulo de reservas já confere o `ativo` no banco a cada requisição.
- [ ] **Sem refresh token:** depois de 60 minutos é preciso logar de novo.
- [ ] **Sem endpoint para o usuário consultar os próprios dados** (por exemplo, `GET /usuarios/me`), útil para o front-end saber quem está logado.
- [ ] **`GET /espacos` lista também os inativos.** Para o morador, o ideal é ver só os ativos. A criação de reservas já recusa espaço inativo (400).
- [ ] **Janela de reserva não atravessa a meia-noite** e vale igual para todos os dias da semana ([ADR 0006](0006-espacos-comuns-e-janela-de-reserva.md)).
- [ ] **Listagens de reservas fazem uma consulta extra por reserva** (espaço, unidade e bloco são `LAZY`). Aceitável no volume de um condomínio; usar `@EntityGraph` se for preciso.
- [ ] **Extensão `btree_gist` no Railway:** confirmar antes do deploy que o PostgreSQL de produção permite instalá-la (a V7 depende dela).
- [ ] **Testes de reservas só pelo Postman:** a coleção "Vicino - Reservas (transições e consultas)" cobre o módulo, mas não roda no build. Um teste automatizado do `ReservaService` com `Clock.fixed` seria o primeiro do projeto.
- [ ] **Autorizações de teste no banco local:** as autorizações 8 ("LGPD Teste 89 dias") e 9 ("LGPD Teste terminada") ficaram `ATIVA`, mas com o período já encerrado, então não aparecem para a portaria. A 8 vai ser anonimizada sozinha pela tarefa diária. Podem ser removidas quando for conveniente.
- [ ] **Anonimização com mais de uma instância:** a tarefa rodaria em todas as instâncias. É inofensivo, porque a segunda execução não encontra nada, mas, se o deploy usar várias instâncias, vale um bloqueio distribuído (por exemplo, ShedLock).
- [ ] **`buscarUsuario` e `resolverUnidade` repetidos** no `ReservaService` e no `VisitanteService`. Extrair para um componente compartilhado se um terceiro módulo precisar da mesma regra.

## Próximos passos

1. **Front-end, etapas 1 a 10** ([ADR 0010](0010-arquitetura-do-front-end.md)):
   - setup e design tokens;
   - componentes base;
   - camada de API, mocks e sessão, incluindo o **`GET /usuarios/me`** no back-end;
   - autenticação;
   - telas: Home, reservas, visitantes, extras (só mock) e perfil;
   - README.
2. **Pendências pequenas do back-end** desta lista (bloco, `readOnly`, `GET /espacos` só ativos), que podem entrar junto de outro commit.
3. **Deploy no Railway**, decidindo junto a origem do front-end (mesma origem ou CORS).
