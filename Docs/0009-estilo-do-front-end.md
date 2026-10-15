# 0009 - Estilo do front-end

- **Status:** aceita (revisada no mesmo dia, ver "Revisão" no fim)
- **Data:** 2026-10-14

## Contexto

Os dois módulos do MVP, reservas ([ADR 0007](0007-reservas.md)) e visitantes ([ADR 0008](0008-visitantes-e-lgpd.md)), estão prontos no back-end. Antes de criar o front-end, faltava decidir a cara do produto e como os estilos seriam escritos. As premissas do [Visão e escopo](Visao&Escopo.md) pesam nessa escolha:
- a aplicação é web e responsiva, usada principalmente no **celular**;
- o morador usa o app de vez em quando, para reservar e autorizar visitantes;
- a portaria precisa de uma tela rápida e simples.

Um protótipo de duas telas, Login e Home, foi aprovado e está em [`prototipo/`](prototipo/). **Ele é a fonte da verdade visual:** todas as outras telas devem parecer da mesma família. O guia completo, com as receitas de cada componente, está em [`prototipo/PROMPT-FRONTEND-VICINO.md`](prototipo/PROMPT-FRONTEND-VICINO.md) (seção 4). As adaptações desse guia ao back-end real estão no [ADR 0010](0010-arquitetura-do-front-end.md).

## Decisão

### Tecnologia

| Item | Escolha |
|---|---|
| Biblioteca | React, com Vite |
| Linguagem | TypeScript em modo `strict` |
| Estilos | **CSS Modules** (um `.module.css` por componente) e **variáveis CSS** como design tokens, em `src/styles/tokens.css`. Sem Tailwind e sem biblioteca de componentes prontos |
| Ícones | lucide-react, com traço 1.8 e tamanho de 20 a 22 px, no mesmo desenho dos SVGs do protótipo. Sem emoji |
| Fontes | Fraunces (500 e 600) para títulos e Manrope (400 a 800) para o resto, do Google Fonts |

- **Componentes próprios:** com uma biblioteca pronta, seria difícil chegar ao vidro e aos raios do protótipo.
- **CSS Modules com tokens:** as cores, os raios e as receitas de vidro ficam definidos **uma vez**. Os componentes usam `var(--color-accent)`, nunca um código de cor solto.

### Direção visual: "Vizinhança calorosa" com glassmorphism

O Vicino é usado por vizinhos, não por uma equipe de escritório. O visual mistura o tom de casa (off-white quente, marrom-café, verde-oliva e terracota) com superfícies de **vidro fosco** sobre manchas de cor desfocadas.

**Paleta**

| Token | Valor | Uso |
|---|---|---|
| `--color-bg` | `#F7F3EC` | Fundo off-white |
| `--color-olive` | `#2E4034` | Verde-oliva: avatar, card escuro, status "liberado" e "confirmada" |
| `--color-terracotta` | `#C4703F` | **Só decoração** (manchas e tintas). Nunca como fundo de texto branco |
| `--color-accent` | `#AD5D2F` | Botão principal, badges, pontos de notificação. É a terracota escurecida para dar contraste AA |
| `--color-accent-text` | `#8F4A22` | Links e textos de destaque. Hover em `#6E3815` |
| `--color-sand` | `#E8DFC8` | Areia: avatares neutros e texto secundário sobre o card escuro |
| `--color-text` | `#3D3229` | Marrom-café, texto principal |
| `--color-text-2` | `#5E4E41` | Texto secundário |
| `--color-text-3` | `#6B5A4C` | Metadados (datas, autor) |
| `--tint-accent` | `rgba(196,112,63,0.16)` | Fundo de ícones e selos de "aguardando" e "pendente" |
| `--tint-olive` | `rgba(46,64,52,0.12)` | Fundo de ícones e selos de "liberado" e "confirmada" |
| `--color-danger` | `#A3362B` | Erros (texto e borda), sempre com ícone e texto, não só pela cor |

**Vidro: as receitas** (detalhes na seção 4.3 do guia)

| Receita | Onde | Base |
|---|---|---|
| `glass` | Cartões, blocos de acesso rápido, listas | Branco a 45%, `blur(20px) saturate(140%)`, borda branca a 75%, sombra suave |
| `glass-strong` | Cartão de login e destaques | Branco a 42%, `blur(24px)`, sombra maior |
| `glass-dark` | Cartão "Próxima reserva" | Oliva a 82%, texto `#F7F3EC` e secundário `#E8DFC8` |
| `glass-nav` | Barra de navegação | Branco a 55%, `blur(26px)` |
| `input` | Campos | Branco a 60%, raio 16, altura 52. Foco com borda `--color-accent` e anel |

**Fundo:** de 3 a 4 manchas desfocadas (`BackgroundBlobs`), em terracota, oliva claro, pêssego e, só no login, oliva. Elas ficam atrás de todo o conteúdo. Uma animação lenta de "flutuar" é opcional e fica desligada com `prefers-reduced-motion`.

**Formas e tipografia**
- **Raios:**
  - cartão de login 30, cartões 24 a 28;
  - blocos de acesso rápido 22, botões 18, campos 16;
  - chips de ícone 14, selos em pílula (999).
- **Espaçamento:** escala de 4 (4, 8, 12, 16, 20, 24 e 32) e margem lateral de 20 a 24 px no celular.
- **Títulos:** Fraunces 600 com `letter-spacing: -0.01em`, nos tamanhos 44 (logo), 26 (h1) e 22 (saudação e título de cartão).
- **Corpo:** Manrope. Corpo em 15, rótulos em 13/700, seções em 16/800. Overlines em 11/800, com espaçamento e em maiúsculas ("PRÓXIMA RESERVA").
- **Logo:** "vicino" em minúsculas, em Fraunces e na cor oliva, com o ícone de casa com coração.

**Regras de uso**
- **Uma ação principal por tela:** botão terracota cheio, de 56 px. As outras ações são de vidro.
- **O status aparece em texto, nunca só pela cor:** "Confirmada", "Pendente", "Liberado" e "Aguardando" ficam escritos no selo.
- **Contraste mínimo de 4,5:1** para texto normal e 3:1 para texto a partir de 24 px. Todo par de cores novo precisa ser conferido.
- **Menos transparência e fallback do vidro:**
  - **Com `prefers-reduced-transparency`:** o vidro continua, com o desfoque, mas o fundo fica mais opaco (branco a 70%, barra a 78%, oliva a 90%).
  - **Sem suporte a `backdrop-filter`:** as superfícies ficam quase opacas (branco a 92%, oliva a 97%), porque não há desfoque para dar contraste.
- **Toque e movimento:**
  - alvos de toque de pelo menos 44×44 px;
  - transições de 150 a 200 ms;
  - nada de animação chamativa.

### Layout

- **Celular (até 1023 px):** conteúdo em coluna e uma barra de navegação inferior flutuante (Início, Reservas, Visitantes e Perfil).
- **A partir de 1024 px:** a barra vira uma barra lateral de vidro com o logo, e o conteúdo fica centralizado com no máximo 1100 px.
- **Tamanhos:** as telas são desenhadas primeiro para 390 px de largura, a do protótipo, e precisam funcionar de 360 a 1440 px sem rolagem horizontal.

## Alternativas consideradas

- **Visual acolhedor plano, sem vidro e sem sombras (primeira versão deste ADR):** foi substituído pelo protótipo, que tem mais identidade.
- **Visual neutro e institucional:** tem cara de sistema administrativo e pouca personalidade para um produto usado por moradores.
- **Visual escuro de alto contraste:** é bom para a portaria à noite, mas pesado para o morador.
- **Tailwind CSS (primeira versão deste ADR):** troca o CSS escrito à mão por classes no JSX. Com as receitas de vidro já definidas como blocos de CSS no guia, CSS Modules mantém essas receitas legíveis num lugar só e ensina CSS de verdade.
- **Biblioteca de componentes (Material UI, Mantine, shadcn):** seria difícil fugir da cara da biblioteca.

## Consequências

- **O guia pedia o fallback quase opaco também para `prefers-reduced-transparency`.** Mas o Windows com "Efeitos de transparência" desligado, uma configuração comum, ativa essa preferência, e esses usuários nunca veriam o vidro. Por isso a regra ficou mais suave: o vidro continua, só com mais opacidade. O quase opaco ficou só para navegadores sem `backdrop-filter`.
- **O vidro custa desempenho:** `backdrop-filter` é pesado em celulares mais simples. Por isso o fallback quase opaco e o desfoque só em superfícies, não em listas longas item por item.
- **`tokens.css` é a única fonte das cores:** uma cor solta num `.module.css` deve virar token numa revisão.
- **Sem modo escuro no MVP.**
- **A interface da portaria e a do síndico** vão reaproveitar os mesmos tokens e componentes quando forem criadas.

## Revisão (2026-10-14)

A primeira versão deste ADR, do mesmo dia, escolhia:
- **Tailwind CSS e Tabler Icons;**
- **um visual acolhedor plano:** fundo areia `#F1EFE8`, primária verde-água `#0F6E56`, sem sombras e com a fonte Nunito Sans.

Depois o Ian trouxe o protótipo e o guia do front-end, e a decisão mudou para **CSS Modules**, **lucide-react** e o visual **"Vizinhança calorosa" com glassmorphism**, porque o protótipo foi aprovado e já define as telas em detalhe. Nenhum código tinha sido escrito com a versão anterior, que nem chegou a ser commitada.
