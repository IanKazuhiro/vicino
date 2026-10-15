# 0010 - Arquitetura do front-end

- **Status:** aceita (front-end em construção)
- **Data:** 2026-10-14

## Contexto

O guia do front-end ([`prototipo/PROMPT-FRONTEND-VICINO.md`](prototipo/PROMPT-FRONTEND-VICINO.md)) foi escrito como se o back-end ainda não existisse. Por isso ele propõe contratos de API diferentes dos que já estão prontos e testados ([`api.md`](api.md)). Alguns exemplos:

| O guia propõe | O back-end tem |
|---|---|
| `DELETE /reservas/{id}` | `POST /reservas/{id}/cancelar` |
| `POST /reservas { espacoId, data, horaInicio, horaFim }` | `POST /reservas { espacoId, inicio, fim }` |
| `GET /espacos/{id}/disponibilidade?data=` | `GET /reservas/agenda?espacoId=&data=` (horários ocupados) |
| `POST /visitantes`, com tipo, código de acesso de 6 dígitos e documento opcional | `POST /visitantes/autorizacoes`, com início e fim e documento obrigatório ([ADR 0008](0008-visitantes-e-lgpd.md)) |
| `POST /auth/login` → `{ token, usuario }` | `POST /auth/login` → `{ token, tipo, expiraEm }` |
| Prefixo `/api` nas rotas | Sem prefixo |

O guia também prevê telas sem nenhum back-end: encomendas, mural de avisos, notificações, primeiro acesso, esqueci a senha, editar telefone e alterar senha. Encomendas, mural e notificações estão em "Fica de fora" no [Visão e escopo](Visao&Escopo.md).

O estilo visual está no [ADR 0009](0009-estilo-do-front-end.md).

## Decisão

### Stack

| Necessidade | Biblioteca |
|---|---|
| Rotas | React Router |
| HTTP | Axios, com um cliente único e interceptors |
| Dados do servidor (cache, carregando, erro) | TanStack Query |
| Formulários e validação | React Hook Form + Zod (`@hookform/resolvers`) |
| Datas | date-fns com locale `pt-BR` |
| Testes | Vitest + Testing Library (jsdom) |
| Qualidade | ESLint + Prettier, e `npm run build` sem erros de tipo |

### Contratos híbridos

- **Login, sessão, espaços, reservas e visitantes usam as rotas e os formatos reais do back-end.** Os tipos em `src/types/` espelham os DTOs (`EspacoResponse`, `ReservaResponse`, `AutorizacaoVisitanteResponse` e outros), e com `VITE_USE_MOCKS=false` essas telas falam com a API de verdade.
- **Encomendas, avisos, notificações, primeiro acesso, esqueci a senha, editar telefone e alterar senha existem só como mock.**
  - Com `VITE_USE_MOCKS=false`, essas telas mostram "Disponível em breve", sem misturar dados falsos com reais.
  - Na Home, o cartão do mural e o badge de encomendas somem.

**Adaptações das telas ao back-end real**

| Tela | Como se adapta |
|---|---|
| Espaços | Mostra só os `ativo`. As "regras" são montadas a partir dos campos do espaço (duração, antecedência, limite por unidade e aprovação). Não há capacidade nem foto, então entra um placeholder visual |
| Horários de um dia | Os slots vão da abertura ao fechamento, com passo igual à duração mínima. Os que cruzam a agenda do dia aparecem como ocupados, e os anteriores à antecedência mínima, como indisponíveis. As datas vão até a antecedência máxima |
| Reservas | Quatro status: Confirmada, Pendente, **Recusada** e Cancelada. Uma reserva pode voltar `PENDENTE` se o espaço exige aprovação. O cancelamento só aparece dentro do prazo |
| Visitantes | O formulário segue o back-end: nome, documento, telefone, placa, início e fim. Não há código de acesso nem tipo |
| Status do visitante | É calculado a partir do status e do período: **Aguardando** antes do início, **Liberado** durante, **Concluído** depois do fim e **Cancelado** |
| Detalhe do visitante | Vem da lista "minhas", em cache, porque o back-end não tem `GET` por id. Uma autorização anonimizada aparece como "Dados removidos (LGPD)" |

### Camada de serviços

- **Um módulo por recurso** em `src/services/api/`: `auth`, `users`, `spaces`, `reservations`, `visitors`, `packages`, `notices` e `notifications`. Cada um decide entre a API real e o mock pela variável `VITE_USE_MOCKS`. Os componentes **nunca** sabem qual dos dois está ativo.
- **Os mocks** ficam em `src/services/mocks/`, com latência de 300 a 600 ms e estado em memória, para que uma reserva criada apareça nas listas. Os dados vêm do protótipo (Marina, Bloco B · Apto 302, Churrasqueira 2, Carlos Mendes...).
- **Os erros da API** chegam no corpo `{ message }` do Spring (`include-message=always`), e é essa mensagem que aparece nos alertas e toasts.

### Sessão

- **O token** fica no `localStorage` quando "Lembrar de mim" está marcado, e no `sessionStorage` quando não. Isso inclui o `expiraEm` devolvido pelo login.
- **Na abertura do app,** um token já vencido é descartado sem chamar a API.
- **Interceptors:**
  - um coloca `Authorization: Bearer <token>` em toda requisição;
  - uma resposta **401** limpa a sessão e leva para `/login`.
- **Os dados de quem está logado** vêm de um endpoint novo no back-end, **`GET /usuarios/me`**. O login não devolve o usuário, e a Home precisa do nome, do perfil e da unidade como texto ("Bloco B · Apto 302").
- **`AuthContext`** expõe `usuario`, `login`, `logout` e `isAuthenticated`, e o `ProtectedRoute` protege as rotas privadas.

### Integração no desenvolvimento

- **`VITE_API_URL=/api`, com um proxy do Vite** que envia `/api/...` para `http://localhost:8080/...`, removendo o prefixo. Para o navegador, front e API estão na mesma origem, então **não é preciso CORS** no desenvolvimento. A configuração de produção (mesma origem ou CORS) é decidida no deploy.
- **Datas:** o back-end usa `LocalDateTime` sem fuso, em hora do condomínio, e o front trata essas datas como hora local (`parseISO`).

### Escopo

- **Só a interface do morador.** O tipo `Usuario.perfil` e a estrutura de rotas ficam prontos para a portaria e o síndico, mas essas interfaces não são implementadas agora.

### Organização do trabalho

- **O código do front-end é escrito pelo Claude, por etapas.** A cada etapa, `npm run lint`, `npm run build` e `npm test` precisam passar, e o Ian revisa e faz o commit.
- **As mudanças no back-end,** como o `GET /usuarios/me`, continuam escritas pelo Ian.

## Alternativas consideradas

- **Seguir os contratos do guia:**
  - o back-end teria de mudar rotas já testadas, como `DELETE` no lugar de `cancelar`, e o formato do visitante;
  - até lá, `VITE_USE_MOCKS=false` não funcionaria para nenhuma tela.
- **Construir só as telas que têm back-end:** é mais honesto com o escopo do MVP, mas deixaria a Home do protótipo incompleta (acesso rápido de encomendas e avisos, mural).
- **CORS no back-end desde já:** o proxy do Vite resolve o desenvolvimento sem mexer na segurança agora. O CORS só é necessário se front e API ficarem em origens diferentes em produção.
- **Guardar o token num cookie `HttpOnly`:** protege melhor contra XSS, mas exigiria mudar a autenticação do back-end, que hoje é um Bearer no header ([ADR 0005](0005-autenticacao-jwt.md)). Fica como evolução.

## Consequências

- **Os tipos do front-end precisam acompanhar os DTOs:** quando um DTO mudar, o tipo correspondente muda junto.
- **As telas só com mock são demonstração:** para virarem reais, cada uma precisa de um módulo no back-end, e encomendas, mural e notificações exigem antes rever o escopo do MVP.
- **O token no `localStorage` fica exposto se houver uma falha de XSS.** Por isso o front não usa `dangerouslySetInnerHTML` nem HTML vindo da API.
- **Ficam como pendências:** o código de acesso e o tipo do visitante, e o telefone do usuário.
