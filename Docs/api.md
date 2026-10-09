# API REST

Referência dos endpoints disponíveis. Base local: `http://localhost:8080`.

## Convenções

- Corpo das requisições e respostas em JSON (`Content-Type: application/json`).
- Criação devolve **201 Created** com o header `Location` apontando para o recurso criado.
- Erros de regra de negócio usam `ResponseStatusException`:
  - **400** para dados inválidos (validação do DTO ou regra de negócio)
  - **404** para recurso não encontrado
  - **409** para conflito, como duplicidade
- O corpo desses erros traz o motivo no campo `message` (propriedade `spring.web.error.include-message=always`). Em desenvolvimento, o devtools também inclui o campo `trace` com o stack trace; isso não acontece na aplicação empacotada.
- Edição usa **PUT** com o recurso completo e devolve **200** com o recurso atualizado.
- Erros de autenticação e autorização (sem corpo na resposta):
  - **401** sem token, ou com token inválido, adulterado ou expirado
  - **403** com token válido, mas sem o perfil exigido

## Autenticação

Todas as rotas, exceto `POST /auth/login` e `GET /actuator/health`, exigem um token JWT no header:

```
Authorization: Bearer <token>
```

O token é obtido no login e vale **60 minutos**. Detalhes da decisão no [ADR 0005](0005-autenticacao-jwt.md).

| Método | Rota | Acesso | Descrição | Respostas |
|---|---|---|---|---|
| POST | `/auth/login` | Público | Autentica e devolve o token | 200, 400, 401 |

**Requisição**
```json
{ "email": "ana@exemplo.com", "senha": "senha1234" }
```
- `email`: obrigatório, formato de e-mail. Maiúsculas e minúsculas não importam.
- `senha`: obrigatória.
- Não envie o header `Authorization` no login. Um token inválido nesse header causa 401 mesmo aqui.

**Resposta (200)**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEm": "2026-10-05T05:46:20Z"
}
```

**401** para e-mail inexistente, usuário inativo ou senha errada, sempre com a mesma mensagem ("E-mail ou senha inválidos.").

**Conteúdo do token** (o payload é legível por qualquer um; não contém dados sensíveis)

| Claim | Valor |
|---|---|
| `iss` | `vicino-api` |
| `sub` | id do usuário |
| `iat` / `exp` | emissão e expiração |
| `perfil` | `MORADOR`, `PORTARIA`, `SINDICO` ou `ADMINISTRADOR` |

## Permissões

Conforme o [ADR 0004](0004-perfis-e-permissoes.md):

| Operação | Perfis |
|---|---|
| `POST` em `/blocos`, `/unidades`, `/usuarios` e `/espacos` | `SINDICO`, `ADMINISTRADOR` |
| `PUT` em `/espacos/{id}` | `SINDICO`, `ADMINISTRADOR` |
| `POST` em `/reservas` | `MORADOR`, `SINDICO`, `ADMINISTRADOR` |
| `POST` em `/reservas/{id}/aprovar` e `/reservas/{id}/recusar` | `SINDICO`, `ADMINISTRADOR` |
| `POST` em `/reservas/{id}/cancelar` | Qualquer usuário autenticado; o service confere o dono e o prazo |
| `GET` em `/reservas` (listagem geral) | `SINDICO`, `ADMINISTRADOR` |
| `GET` em `/reservas/{id}`, `/reservas/minhas` e `/reservas/agenda` | Qualquer usuário autenticado; o service aplica as regras de visibilidade |
| `GET` nas demais rotas | Qualquer usuário autenticado |

## Blocos

| Método | Rota | Acesso | Descrição | Respostas |
|---|---|---|---|---|
| POST | `/blocos` | `SINDICO`, `ADMINISTRADOR` | Cria um bloco | 201, 400, 401, 403, 409 (nome repetido) |
| GET | `/blocos` | Autenticado | Lista todos os blocos | 200, 401 |
| GET | `/blocos/{id}` | Autenticado | Busca um bloco | 200, 401, 404 |

**Requisição (POST)**
```json
{ "nome": "Bloco A" }
```
- `nome`: obrigatório, até 100 caracteres (ver pendência de tamanho em [progresso.md](progresso.md)).

**Resposta**
```json
{ "id": 1, "nome": "Bloco A" }
```

## Unidades

| Método | Rota | Acesso | Descrição | Respostas |
|---|---|---|---|---|
| POST | `/unidades` | `SINDICO`, `ADMINISTRADOR` | Cria uma unidade | 201, 400, 401, 403, 404 (bloco inexistente), 409 (número repetido no bloco) |
| GET | `/unidades` | Autenticado | Lista as unidades; aceita `?blocoId=` para filtrar | 200, 401 |
| GET | `/unidades/{id}` | Autenticado | Busca uma unidade | 200, 401, 404 |

**Requisição (POST)**
```json
{ "blocoId": 1, "numero": "101" }
```
- `blocoId`: obrigatório.
- `numero`: obrigatório, até 20 caracteres. O mesmo número pode existir em blocos diferentes.

**Resposta**
```json
{ "id": 1, "blocoId": 1, "blocoNome": "Bloco A", "numero": "101" }
```

## Usuários

| Método | Rota | Acesso | Descrição | Respostas |
|---|---|---|---|---|
| POST | `/usuarios` | `SINDICO`, `ADMINISTRADOR` | Cria um usuário | 201, 400, 401, 403, 404 (unidade inexistente), 409 (e-mail repetido) |
| GET | `/usuarios` | Autenticado | Lista todos os usuários | 200, 401 |
| GET | `/usuarios/{id}` | Autenticado | Busca um usuário | 200, 401, 404 |

**Requisição (POST)**
```json
{
  "nome": "Ana",
  "email": "ana@exemplo.com",
  "senha": "senha1234",
  "perfil": "MORADOR",
  "unidadeId": 1
}
```
- `nome`: obrigatório, até 150 caracteres.
- `email`: obrigatório, formato de e-mail, até 150 caracteres. É gravado em minúsculas, então `ANA@EXEMPLO.COM` conflita com `ana@exemplo.com`.
- `senha`: obrigatória, de 8 a 72 caracteres. É gravada apenas como hash BCrypt.
- `perfil`: obrigatório. Valores: `MORADOR`, `PORTARIA`, `SINDICO`, `ADMINISTRADOR`.
- `unidadeId`: obrigatório para `MORADOR` (senão 400); opcional para os demais perfis.

**Resposta** (nunca contém senha nem hash)
```json
{
  "id": 1,
  "nome": "Ana",
  "email": "ana@exemplo.com",
  "perfil": "MORADOR",
  "unidadeId": 1,
  "ativo": true
}
```

## Espaços

Regras e motivação no [ADR 0006](0006-espacos-comuns-e-janela-de-reserva.md).

| Método | Rota | Acesso | Descrição | Respostas |
|---|---|---|---|---|
| POST | `/espacos` | `SINDICO`, `ADMINISTRADOR` | Cria um espaço | 201, 400, 401, 403, 409 (nome repetido) |
| PUT | `/espacos/{id}` | `SINDICO`, `ADMINISTRADOR` | Atualiza um espaço (corpo completo) | 200, 400, 401, 403, 404, 409 (nome de outro espaço) |
| GET | `/espacos` | Autenticado | Lista os espaços em ordem alfabética, inclusive os inativos | 200, 401 |
| GET | `/espacos/{id}` | Autenticado | Busca um espaço | 200, 401, 404 |

**Requisição (POST e PUT)**
```json
{
  "nome": "Quadra",
  "descricao": "Quadra poliesportiva",
  "horaAbertura": "08:00",
  "horaFechamento": "22:00",
  "duracaoMinimaMinutos": 60,
  "duracaoMaximaMinutos": 60,
  "antecedenciaMinimaHoras": 2,
  "antecedenciaMaximaDias": 15,
  "limiteReservasPorUnidade": 2,
  "exigeAprovacao": false,
  "ativo": true
}
```

| Campo | Regra |
|---|---|
| `nome` | Obrigatório, até 100 caracteres, único |
| `descricao` | Opcional, até 500 caracteres |
| `horaAbertura`, `horaFechamento` | Obrigatórios, formato `"HH:mm"`. A abertura deve ser anterior ao fechamento (a janela não atravessa a meia-noite). |
| `duracaoMinimaMinutos` | Obrigatório, maior que zero, menor ou igual à máxima |
| `duracaoMaximaMinutos` | Obrigatório, maior que zero, e precisa caber no horário de funcionamento |
| `antecedenciaMinimaHoras` | Obrigatório, zero ou mais, e menor que a antecedência máxima |
| `antecedenciaMaximaDias` | Obrigatório, maior que zero |
| `limiteReservasPorUnidade` | Obrigatório, maior que zero. Conta as reservas futuras da unidade naquele espaço. |
| `exigeAprovacao` | Obrigatório. Se `true`, as reservas ficam pendentes até o síndico aprovar. |
| `ativo` | Obrigatório. `false` desativa o espaço sem apagá-lo. |

**Exemplos de 400 por regra de negócio**

| Situação | Mensagem |
|---|---|
| Abertura `22:00`, fechamento `08:00` | O horário de abertura deve ser anterior ao de fechamento. |
| Duração mínima 90, máxima 60 | A duração mínima não pode ser maior que a duração máxima. |
| Horário `08:00`–`10:00`, duração máxima 180 | A duração máxima (180 min) não cabe no horário de funcionamento (120 min). |
| Antecedência mínima 48 h, máxima 1 dia | A antecedência mínima deve ser menor que a antecedência máxima. |

**Resposta**: o mesmo formato da requisição, com o `id`.
```json
{
  "id": 1,
  "nome": "Quadra",
  "descricao": "Quadra poliesportiva",
  "horaAbertura": "08:00:00",
  "horaFechamento": "22:00:00",
  "duracaoMinimaMinutos": 60,
  "duracaoMaximaMinutos": 60,
  "antecedenciaMinimaHoras": 2,
  "antecedenciaMaximaDias": 15,
  "limiteReservasPorUnidade": 2,
  "exigeAprovacao": false,
  "ativo": true
}
```

## Reservas

Regras e motivação no [ADR 0007](0007-reservas.md).

Datas e horas são **locais do condomínio** (`America/Manaus`), sem fuso, no formato `"2026-10-10T18:00:00"`.

| Método | Rota | Acesso | Descrição | Respostas |
|---|---|---|---|---|
| POST | `/reservas` | `MORADOR`, `SINDICO`, `ADMINISTRADOR` | Cria uma reserva | 201, 400, 401, 403, 404, 409 |
| POST | `/reservas/{id}/aprovar` | `SINDICO`, `ADMINISTRADOR` | `PENDENTE` → `CONFIRMADA` | 200, 400, 401, 403, 404, 409 |
| POST | `/reservas/{id}/recusar` | `SINDICO`, `ADMINISTRADOR` | `PENDENTE` → `RECUSADA` | 200, 400, 401, 403, 404, 409 |
| POST | `/reservas/{id}/cancelar` | Autenticado (veja as regras) | `PENDENTE` ou `CONFIRMADA` → `CANCELADA` | 200, 400, 401, 403, 404, 409 |
| GET | `/reservas/{id}` | Autenticado (veja as regras) | Detalhe de uma reserva | 200, 401, 403, 404 |
| GET | `/reservas/minhas` | Autenticado | Reservas da unidade do usuário logado, da mais recente para a mais antiga | 200, 401 |
| GET | `/reservas` | `SINDICO`, `ADMINISTRADOR` | Todas as reservas, com filtros opcionais `?espacoId=` e `?status=` | 200, 400, 401, 403 |
| GET | `/reservas/agenda?espacoId=&data=` | Autenticado | Horários ocupados de um espaço em um dia, sem dados da unidade | 200, 400, 401, 404 |

### Criação

**Requisição**
```json
{
  "espacoId": 1,
  "unidadeId": 2,
  "inicio": "2026-10-10T18:00:00",
  "fim": "2026-10-10T19:00:00"
}
```
- `espacoId`, `inicio` e `fim`: obrigatórios.
- `unidadeId`: o morador não precisa informar, porque a reserva vai para a unidade dele (se informar outra, 403). Para síndico e administrador é obrigatório.

**Regras**

| Regra | Resposta se violada |
|---|---|
| O espaço existe e está ativo | 404 / 400 |
| O início é anterior ao fim, e os dois estão no mesmo dia | 400 |
| O período fica dentro do horário de funcionamento do espaço | 400 |
| A duração fica entre a mínima e a máxima do espaço | 400 |
| O início respeita a antecedência mínima (em horas) e a máxima (em dias) | 400 |
| A unidade não passou do limite de reservas futuras ativas naquele espaço | 409 |
| Não há outra reserva ativa (`PENDENTE` ou `CONFIRMADA`) sobreposta no espaço | 409 |
| Quem reserva não é da portaria, e o usuário está ativo | 403 |

O fim é exclusivo: 18:00–19:00 e 19:00–20:00 não conflitam. Mesmo com duas requisições simultâneas, o banco impede a sobreposição (constraint `EXCLUDE`), e a segunda recebe 409.

**Status inicial:** `PENDENTE` quando o espaço exige aprovação e quem reserva é morador; `CONFIRMADA` nos demais casos, inclusive quando o síndico ou o administrador reserva.

### Transições

As três rotas não têm corpo e devolvem **200** com a reserva atualizada, no mesmo formato da resposta da criação.

| Operação | Regras | Erros |
|---|---|---|
| Aprovar e recusar | A reserva precisa estar `PENDENTE` e ainda não pode ter começado. Como uma `PENDENTE` já ocupa o horário, aprovar não gera conflito. | 404 (não existe), 409 (status não é `PENDENTE`), 400 (já começou) |
| Cancelar, pelo morador | Só reservas da própria unidade, `PENDENTE` ou `CONFIRMADA`, até `inicio − antecedenciaMinimaHoras` do espaço | 403 (outra unidade), 409 (status), 400 (prazo encerrado) |
| Cancelar, pelo síndico ou administrador | Qualquer reserva `PENDENTE` ou `CONFIRMADA` que ainda não começou | 409 (status), 400 (já começou) |
| Cancelar, pela portaria | Não permitido | 403 |

Recusar ou cancelar libera o horário para novas reservas.

### Consultas

| Rota | Regras |
|---|---|
| `GET /reservas/{id}` | O morador só vê reservas da própria unidade (403 para as outras); a portaria não vê detalhes (403); síndico e administrador veem todas. |
| `GET /reservas/minhas` | Reservas da unidade do usuário logado, em qualquer status. Para quem não tem unidade (síndico, administrador, portaria), devolve uma lista vazia. |
| `GET /reservas` | Só síndico e administrador. `espacoId` e `status` são opcionais e podem ser combinados; um `status` inválido dá 400. |
| `GET /reservas/agenda` | `espacoId` e `data` (formato `2026-10-10`) são obrigatórios (400 se faltarem); espaço inexistente dá 404. Mostra só reservas `PENDENTE` e `CONFIRMADA` que ocupam algum horário do dia, em ordem de início. |

**Resposta da agenda** (sem identificar a unidade, por causa da LGPD)
```json
[
  { "inicio": "2026-10-10T18:00:00", "fim": "2026-10-10T19:00:00", "status": "CONFIRMADA" },
  { "inicio": "2026-10-10T20:00:00", "fim": "2026-10-10T21:00:00", "status": "PENDENTE" }
]
```

### Formato da reserva

Usado na resposta da criação (201), das transições (200), do detalhe e das listas.
```json
{
  "id": 1,
  "espacoId": 1,
  "espacoNome": "Quadra",
  "unidadeId": 1,
  "unidade": "Bloco A - 101",
  "inicio": "2026-10-10T18:00:00",
  "fim": "2026-10-10T19:00:00",
  "status": "CONFIRMADA",
  "criadoEm": "2026-10-07T22:15:03.123Z"
}
```

## Saúde da aplicação

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/actuator/health` | Público | Indica se a aplicação está no ar |
