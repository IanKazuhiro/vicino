# Prompt — Frontend do Vicino

> Cole este prompt no Claude Code a partir da raiz do monorepo do Vicino.
> Antes, coloque os arquivos `prototipo-login.html` e `prototipo-home.html` em `docs/prototipo/`.

---

## 1. Contexto

Você vai construir o **frontend completo** do **Vicino**, um app web de gestão de condomínio para **moradores**. O MVP atende **um único condomínio**.

- O repositório é um **monorepo** com duas pastas: `backend/` (Spring Boot, Maven, Java 25, pacote `br.com.vicino.api`, PostgreSQL local) e `frontend/`. **Trabalhe só dentro de `frontend/`.** Não altere nada em `backend/`.
- O backend ainda está em construção. Por isso o frontend precisa funcionar **com dados mockados** e estar pronto para trocar para a API real só mudando uma variável de ambiente.
- Existe um protótipo aprovado de duas telas (Login e Home) em `docs/prototipo/`. Abra e leia os dois HTMLs antes de começar: eles são a **fonte da verdade visual**. Todas as outras telas devem parecer da mesma família.
- Estou aprendendo (Engenharia de Software, foco em back-end, básico de front-end). Escreva código limpo, bem organizado e legível. Comente só o que não for óbvio. No fim de cada etapa, explique em poucas linhas o que fez e por quê.

## 2. Stack e restrições

- **React 18 + Vite + TypeScript** (modo `strict`).
- **React Router** (v6 ou superior) para rotas.
- **CSS Modules + variáveis CSS** (design tokens em `src/styles/tokens.css`). Sem Tailwind, sem biblioteca de componentes pronta (MUI, Chakra, shadcn etc.). Os componentes visuais são nossos.
- **Axios** para HTTP, com um cliente único e interceptors.
- **TanStack Query** para buscar dados, guardar em cache e controlar estados de carregando e erro.
- **React Hook Form + Zod** para formulários e validação.
- **date-fns** com locale `pt-BR` para datas.
- Ícones: **lucide-react**, com traço 1.8 e tamanho 20–22 (mesmo estilo dos SVGs do protótipo). **Nada de emoji.**
- Fontes do Google Fonts: **Fraunces** (500, 600) para títulos e **Manrope** (400–800) para o resto.
- Qualidade: ESLint + Prettier configurados, e `npm run build` sem erros nem warnings de tipo.
- Testes: **Vitest + Testing Library**, cobrindo pelo menos os componentes base, o fluxo de login e a criação de reserva.
- Idioma da interface: **português do Brasil**. Nomes de código (variáveis, componentes, arquivos) em inglês. Campos dos contratos de API em português, como na seção 7.

## 3. Estrutura de pastas

```
frontend/
  src/
    app/            # App.tsx, router, providers (QueryClient, Auth)
    styles/         # tokens.css, globals.css, glass.css
    components/
      ui/           # GlassCard, Button, Input, PasswordInput, Checkbox, Pill, Badge,
                    # Avatar, IconTile, SectionHeader, EmptyState, Skeleton, Toast,
                    # BottomSheet, Modal, DatePickerStrip, TimeSlotGrid
      layout/       # AppShell, BottomNav, SideNav (desktop), PageHeader, BackgroundBlobs
    features/
      auth/         # páginas, hooks, schemas
      home/
      reservations/
      visitors/
      packages/     # encomendas
      notices/      # mural / avisos
      notifications/
      profile/
    services/
      api/          # client.ts (axios), endpoints por recurso
      mocks/        # dados e handlers mockados
    types/          # tipos dos contratos (seção 7)
    utils/
  .env.example
  README.md
```

## 4. Design system (siga à risca)

### 4.1 Tokens de cor (paleta "Vizinhança calorosa")

| Token | Valor | Uso |
|---|---|---|
| `--color-bg` | `#F7F3EC` | fundo off-white |
| `--color-olive` | `#2E4034` | verde oliva: avatar, card escuro, status "liberado" |
| `--color-terracotta` | `#C4703F` | só em decoração (blobs, tints). **Nunca** use como fundo de texto branco |
| `--color-accent` | `#AD5D2F` | botão primário, badges, pontos de notificação (terracota escurecido para contraste AA) |
| `--color-accent-text` | `#8F4A22` | links e textos de destaque; hover `#6E3815` |
| `--color-sand` | `#E8DFC8` | areia: avatares neutros, textos sobre o card escuro |
| `--color-text` | `#3D3229` | marrom-café: texto principal |
| `--color-text-2` | `#5E4E41` | texto secundário |
| `--color-text-3` | `#6B5A4C` | metadados (datas, autor) |
| `--tint-accent` | `rgba(196,112,63,0.16)` | fundo de ícones e pills de "aguardando" |
| `--tint-olive` | `rgba(46,64,52,0.12)` | fundo de ícones e pills de "liberado/ok" |
| `--color-danger` | `#A3362B` | erros (texto e borda), sempre com ícone e texto, não só a cor |

Contraste mínimo: 4.5:1 para texto normal e 3:1 para texto ≥24px. Verifique os pares novos que criar.

### 4.2 Tipografia

- Títulos de página, saudação, nome de espaço e logo: **Fraunces 600**, `letter-spacing: -0.01em`. Tamanhos: 44 (logo), 26 (h1), 22 (saudação e título de card).
- Corpo e UI: **Manrope**. Corpo 15, secundário 13–14, labels 13 em 700, rótulos de seção 16 em 800.
- *Overlines* (ex.: "PRÓXIMA RESERVA", "MURAL"): 11px, 800, `letter-spacing: 0.1–0.12em`, maiúsculas.
- Logo: a palavra **"vicino"** em minúsculas, Fraunces 600, cor oliva, com o ícone de casa com coração (ver protótipo). Transforme o ícone em componente `<Logo />`.

### 4.3 Glassmorphism: receitas

Crie classes utilitárias em `glass.css` (ou variantes de `GlassCard`):

- **glass (padrão)**: `background: rgba(255,255,255,0.45)`, `backdrop-filter: blur(20px) saturate(140%)` (com `-webkit-` também), `border: 1px solid rgba(255,255,255,0.75)`, `box-shadow: 0 6px 20px rgba(61,50,41,0.08)`.
- **glass-strong** (cards principais, como o de login): fundo `0.42`, blur 24, saturate 150%, sombra `0 12px 40px rgba(61,50,41,0.14)`.
- **glass-dark** (card de destaque, como "Próxima reserva"): `rgba(46,64,52,0.82)`, borda `rgba(255,255,255,0.22)`, sombra `0 14px 36px rgba(46,64,52,0.3)`, texto `#F7F3EC` e secundário `#E8DFC8`.
- **glass-nav** (barra inferior): fundo `0.55`, blur 26, saturate 160%, sombra `0 12px 32px rgba(61,50,41,0.16)`.
- **input**: fundo `rgba(255,255,255,0.6)`, borda `rgba(61,50,41,0.12)`, raio 16, altura 52, ícone à esquerda em `#7A685A`. Foco: borda `--color-accent` e anel `0 0 0 3px rgba(173,93,47,0.18)`.
- **Fallback obrigatório**: com `@supports not (backdrop-filter: blur(1px))` ou `@media (prefers-reduced-transparency: reduce)`, use fundos quase opacos (`rgba(255,255,255,0.92)` e `rgba(46,64,52,0.97)`).

### 4.4 Fundo com blobs

O componente `<BackgroundBlobs variant="login" | "app" />` desenha 3 ou 4 círculos `position: fixed`, `border-radius: 50%` e `filter: blur(60–70px)`, atrás de todo o conteúdo:
- terracota `#C4703F`, opacidade 0.45–0.55
- oliva claro `#7E8F63`, opacidade 0.45–0.5
- pêssego `#E3A878`, opacidade 0.55–0.6
- (só no login) oliva `#2E4034`, opacidade 0.18

Posições como no protótipo. Use `pointer-events: none` e `aria-hidden`. Animação de "flutuar" bem lenta (20–30 s) é opcional, e precisa ser desligada com `prefers-reduced-motion`.

### 4.5 Forma, espaçamento e movimento

- Raios: card de login 30, cards 24–28, tiles 22, botões 18, inputs 16, chips de ícone 14, pills 999.
- Espaçamento em escala de 4 (4, 8, 12, 16, 20, 24, 32). Margem lateral de página: 20–24px no mobile.
- Botão primário: altura 56, fundo `--color-accent`, texto branco 16/800, sombra `0 10px 24px rgba(173,93,47,0.32)`. Botão secundário: glass com texto `--color-text`. Botão claro sobre card escuro: fundo `#F7F3EC` com texto oliva.
- Alvos de toque de pelo menos 44×44px.
- Transições de 150–200ms em hover, press (`scale(0.98)`) e foco. Nada de animação chamativa.

## 5. Layout e navegação

- **Mobile-first.** No celular (< 768px): conteúdo em coluna e **barra de navegação inferior flutuante** (glass-nav, 16px das bordas, raio 26) com **Início, Reservas, Visitantes e Perfil**. O item ativo usa fundo `--tint-accent` e texto/ícone `--color-accent-text`. Reserve espaço inferior (cerca de 112px) para a barra não cobrir o conteúdo.
- **Tablet e desktop (≥ 1024px)**: a barra inferior vira uma **barra lateral** glass à esquerda, com o logo no topo. O conteúdo fica centralizado com `max-width: 1100px`. Na Home, use grid de 2 colunas (próxima reserva e acesso rápido à esquerda; visitantes e mural à direita).
- `PageHeader` nas telas internas: botão voltar (com `aria-label`), título em Fraunces e ação opcional à direita.
- Use `<nav>`, `<main>`, `<h1>` (um por página), `aria-current="page"` no item ativo, `<label>` em todos os inputs e foco visível em tudo.

## 6. Telas e fluxos

Rotas públicas: `/login`, `/primeiro-acesso`, `/esqueci-senha`. Todas as outras são protegidas e redirecionam para `/login` sem sessão.

### 6.1 Login (`/login`): igual ao protótipo
Logo, frase "Seu condomínio, mais perto de você.", card glass-strong com "Bem-vindo de volta", campos **E-mail** e **Senha** (com botão mostrar/ocultar), "Lembrar de mim", link "Esqueci a senha", botão **Entrar** com seta e, abaixo do card, "Primeiro acesso? **Ative sua conta**".
- Validação com Zod (e-mail válido, senha obrigatória). Mostre as mensagens de erro abaixo de cada campo.
- Enquanto envia, o botão mostra estado de carregando e fica desabilitado. Erro de credencial aparece como alerta dentro do card.
- Com "Lembrar de mim", o token vai para `localStorage`. Sem ele, vai para `sessionStorage`.

### 6.2 Primeiro acesso (`/primeiro-acesso`)
Mesmo visual do login. Passos: (1) e-mail e **código de convite** enviado pela administração → (2) criar senha, com confirmação e regras visíveis (mínimo de 8 caracteres, uma letra e um número) → (3) tela de sucesso com botão "Ir para o login".

### 6.3 Esqueci a senha (`/esqueci-senha`)
Informar o e-mail. Depois aparece a confirmação: "Se o e-mail estiver cadastrado, enviaremos as instruções".

### 6.4 Home (`/`): igual ao protótipo
1. Cabeçalho: avatar com iniciais (fundo oliva), saudação conforme o horário ("Bom dia / Boa tarde / Boa noite, {primeiro nome}"), "Bloco X · Apto Y" e botão de sino com ponto `--color-accent` quando há notificação não lida (leva a `/notificacoes`).
2. Card **glass-dark "Próxima reserva"**: overline, pill de status, nome do espaço em Fraunces, data e horário com ícones, botão "Detalhes". Sem reserva futura, mostre um estado vazio com o CTA "Reservar um espaço".
3. **Acesso rápido**: grid de 4 tiles glass (Reservar, Visitante, Encomendas com badge de quantidade pendente, Avisos), cada um com ícone em chip tingido. Os tints alternam entre terracota e oliva.
4. **Visitantes de hoje**: card glass com até 3 linhas (avatar com iniciais em areia, nome, tipo · horário, pill de status) e link "Ver todos".
5. **Mural**: o aviso mais recente (overline "MURAL", título, resumo, "Autor · há X tempo"), que leva ao detalhe.
- Mostre skeletons durante o carregamento, com o mesmo formato dos cards.

### 6.5 Reservas
- `/reservas`: duas abas (pills segmentadas glass): **Espaços** e **Minhas reservas**.
  - **Espaços**: cards com nome (Churrasqueira 1, Churrasqueira 2, Salão de festas, Quadra), capacidade, horário de funcionamento e uma regra curta. Use placeholder visual se não houver foto.
  - **Minhas reservas**: separadas em "Próximas" e "Anteriores", com pill de status (Confirmada, Pendente, Cancelada).
- `/reservas/espacos/:id`: detalhe do espaço, regras, **faixa de datas rolável** (`DatePickerStrip`, 14 dias a partir de hoje) e **grade de horários** (`TimeSlotGrid`) com slots disponível / ocupado / selecionado. Os ocupados ficam desabilitados e têm texto, não só cor. Botão fixo no rodapé: "Reservar · {data} · {faixa}".
- Confirmação em **BottomSheet** glass com o resumo e a caixa "Li e aceito as regras do espaço". Ao confirmar, mostre Toast de sucesso e vá para "Minhas reservas".
- `/reservas/:id`: detalhe da reserva, com "Cancelar reserva" (confirmação em modal) quando permitido.

### 6.6 Visitantes
- `/visitantes`: filtro por data (Hoje, Amanhã, escolher) e lista agrupada por status (Aguardando, Liberado, Concluído, Cancelado). Botão primário "Liberar visitante".
- `/visitantes/novo`: formulário com nome, documento (opcional), tipo (Visita, Prestador de serviço, Entrega), data, faixa de horário prevista, placa do veículo (opcional) e observação. Valide com Zod.
- `/visitantes/:id`: detalhe com **código de acesso** de 6 dígitos em destaque (Fraunces grande, glass-strong), botão "Compartilhar" (Web Share API, com fallback de copiar) e "Cancelar liberação".

### 6.7 Encomendas (`/encomendas`)
Lista com abas **Aguardando retirada** e **Retiradas**. Cada item mostra transportadora/remetente, data de chegada, local de retirada e código de retirada.

### 6.8 Mural / Avisos (`/avisos`, `/avisos/:id`)
Lista com filtros por categoria (Geral, Manutenção, Assembleia, Segurança). O detalhe mostra título em Fraunces, autor, data e corpo do texto.

### 6.9 Notificações (`/notificacoes`)
Lista agrupada por dia, com lidas e não lidas visualmente distintas (ponto `--color-accent` e peso de fonte). Ação "Marcar todas como lidas".

### 6.10 Perfil (`/perfil`)
Avatar, nome, e-mail, telefone, unidade (bloco e apto), editar telefone, alterar senha (modal) e **Sair** (limpa a sessão e vai para `/login`).

### 6.11 Estados globais
- **EmptyState** em toda lista vazia, com ícone, frase curta e CTA quando fizer sentido.
- **Erro de rede**: card glass com mensagem e botão "Tentar novamente".
- Página **404** no mesmo estilo.
- **Toasts** glass no topo para sucesso e erro.

## 7. Dados e integração com a API

- `VITE_API_URL=http://localhost:8080/api` e `VITE_USE_MOCKS=true` no `.env.example`.
- `services/api/client.ts`: instância axios com `baseURL`. Um interceptor injeta `Authorization: Bearer <token>`. Em resposta 401, limpa a sessão e redireciona para `/login`.
- Cada recurso tem um módulo (`auth.ts`, `reservations.ts`...) que chama a API real ou o mock conforme `VITE_USE_MOCKS`. Os componentes **nunca** sabem qual dos dois está ativo. O mock simula latência de 300–600ms e mantém estado em memória (criar reserva ou liberar visitante aparece nas listas).
- `AuthContext` com `usuario`, `login()`, `logout()` e `isAuthenticated`. `ProtectedRoute` envolve as rotas privadas.

**Contratos propostos.** Crie os tipos em `src/types/`. O backend ainda vai implementar esses endpoints, então deixe tudo centralizado para eu ajustar depois:

```
POST   /auth/login                 { email, senha } -> { token, usuario }
POST   /auth/primeiro-acesso       { email, codigoConvite, senha }
POST   /auth/esqueci-senha         { email }
GET    /usuarios/me                -> Usuario
PATCH  /usuarios/me                { telefone }
PATCH  /usuarios/me/senha          { senhaAtual, novaSenha }

GET    /espacos                    -> Espaco[]
GET    /espacos/{id}               -> Espaco
GET    /espacos/{id}/disponibilidade?data=YYYY-MM-DD -> Slot[]
GET    /reservas/minhas            -> Reserva[]
POST   /reservas                   { espacoId, data, horaInicio, horaFim } -> Reserva
DELETE /reservas/{id}

GET    /visitantes?data=YYYY-MM-DD -> Visitante[]
GET    /visitantes/{id}            -> Visitante
POST   /visitantes                 VisitanteInput -> Visitante
PATCH  /visitantes/{id}/cancelar

GET    /encomendas?status=AGUARDANDO|RETIRADA -> Encomenda[]
GET    /avisos?categoria=          -> Aviso[]
GET    /avisos/{id}                -> Aviso
GET    /notificacoes               -> Notificacao[]
PATCH  /notificacoes/{id}/lida
PATCH  /notificacoes/lidas
```

Tipos principais:
- `Usuario { id, nome, email, telefone, bloco, apartamento, perfil: 'MORADOR' }`
- `Espaco { id, nome, descricao, capacidade, horarioAbertura, horarioFechamento, regras: string[], fotoUrl? }`
- `Slot { horaInicio, horaFim, disponivel }`
- `Reserva { id, espaco: { id, nome }, data, horaInicio, horaFim, status: 'CONFIRMADA'|'PENDENTE'|'CANCELADA' }`
- `Visitante { id, nome, documento?, tipo: 'VISITA'|'PRESTADOR'|'ENTREGA', data, horaPrevista, placa?, observacao?, codigoAcesso, status: 'AGUARDANDO'|'LIBERADO'|'CONCLUIDO'|'CANCELADO' }`
- `Encomenda { id, remetente, chegadaEm, localRetirada, codigoRetirada, status: 'AGUARDANDO'|'RETIRADA', retiradaEm? }`
- `Aviso { id, titulo, resumo, corpo, categoria, autor, publicadoEm }`
- `Notificacao { id, titulo, mensagem, tipo, lida, criadaEm }`

Datas trafegam em ISO 8601 e são formatadas na interface com date-fns `pt-BR` ("Sáb, 18 out", "18h – 22h", "há 2h").

Dados de mock: use os do protótipo (moradora "Marina", Bloco B · Apto 302; Churrasqueira 2 no sábado das 18h às 22h; visitantes Carlos Mendes e Ana Paula; aviso "Manutenção da piscina") e complete com mais alguns registros coerentes.

## 8. Fora do escopo agora

Painel do síndico/administração e app da portaria (validar o código do visitante). Deixe o campo `perfil` no tipo `Usuario` e a estrutura de rotas prontos para isso no futuro, mas não implemente.

## 9. Como trabalhar

1. **Antes de escrever código**, leia `docs/prototipo/*.html` e me apresente um plano curto: etapas, dependências que vai instalar e qualquer dúvida. Espere meu ok.
2. Implemente **por etapas**, com **um commit por etapa** (mensagens no padrão Conventional Commits, em português, ex.: `feat(auth): tela de login com validação`):
   1. Setup (Vite, TS, ESLint/Prettier, estrutura, tokens, fontes, globals, glass, BackgroundBlobs)
   2. Componentes base de `ui/` e `layout/` (AppShell, BottomNav, SideNav)
   3. Camada de API, mocks, AuthContext e rotas protegidas
   4. Auth: Login, Primeiro acesso e Esqueci a senha
   5. Home
   6. Reservas
   7. Visitantes
   8. Encomendas, Avisos e Notificações
   9. Perfil, 404, estados globais e testes
   10. README (como rodar, variáveis de ambiente, como trocar mock por API, estrutura de pastas)
3. Ao final de cada etapa, rode `npm run lint`, `npm run build` e os testes, e corrija o que quebrar antes de seguir.
4. Não invente bibliotecas além das listadas sem me perguntar.

## 10. Critérios de aceite

- Login e Home ficam **visualmente fiéis ao protótipo** (cores, fontes, raios, vidro, blobs e espaçamentos) em 390px de largura.
- Todas as telas da seção 6 estão navegáveis com dados mockados, sem nenhum erro no console.
- O layout funciona de 360px até 1440px, com barra inferior no mobile e lateral no desktop, e sem rolagem horizontal.
- Navegação completa pelo teclado, foco visível, labels em todos os campos e contraste AA.
- O fallback sem `backdrop-filter` fica legível.
- Trocar `VITE_USE_MOCKS=false` faz o app chamar `VITE_API_URL` sem mudar nenhum componente.
- `npm run lint`, `npm run build` e `npm test` passam.
