# 0004 - Perfis de acesso e permissões

- **Status:** aceita (implementação pendente, na etapa de autenticação)
- **Data:** 2026-10-04

## Contexto

O Vicino tem quatro perfis de usuário, definidos no `PerfilEnum` e na constraint `ck_usuario_perfil` do banco: `MORADOR`, `PORTARIA`, `SINDICO` e `ADMINISTRADOR`. Antes de implementar a autenticação, é preciso definir o que cada perfil pode fazer nos cadastros já existentes.

## Decisão

| Perfil | Cadastros (blocos, unidades, usuários) |
|---|---|
| `SINDICO` | Cadastra e consulta |
| `ADMINISTRADOR` | Cadastra e consulta |
| `MORADOR` | Apenas consulta |
| `PORTARIA` | Apenas consulta |

- Endpoints de criação (`POST`) de blocos, unidades e usuários exigem `SINDICO` ou `ADMINISTRADOR`.
- Endpoints de consulta (`GET`) exigem apenas um usuário autenticado.
- As funções próprias de cada perfil (morador reservar espaços e autorizar visitantes, portaria validar a entrada de visitantes) serão definidas junto com esses módulos.

## Alternativas consideradas

- **Só o `ADMINISTRADOR` cadastra:** concentraria o trabalho em um único perfil, mas o síndico também gerencia o condomínio no dia a dia.
- **Permissões granulares por ação** (tabela de permissões configuráveis): flexível, mas desnecessário para quatro perfis fixos no MVP.

## Consequências

- O controle de acesso pode ser feito por perfil (role) com o Spring Security, sem tabela de permissões.
- Para cadastrar o primeiro `SINDICO` ou `ADMINISTRADOR` com os endpoints já protegidos, será preciso um usuário inicial criado fora da API (por exemplo, por migration ou por configuração na inicialização).
- **Ponto em aberto:** a consulta de usuários pelo `MORADOR` e pela `PORTARIA` expõe nome e e-mail de outros moradores. Avaliar, na etapa de autenticação, se esses perfis devem ver só os próprios dados, por causa da LGPD.
