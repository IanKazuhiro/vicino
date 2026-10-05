# API REST

Referência dos endpoints disponíveis. Base local: `http://localhost:8080`.

## Convenções

- Corpo das requisições e respostas em JSON (`Content-Type: application/json`).
- Criação devolve **201 Created** com o header `Location` apontando para o recurso criado.
- Erros de regra de negócio usam `ResponseStatusException`:
  - **400** para dados inválidos (validação do DTO ou regra de negócio)
  - **404** para recurso não encontrado
  - **409** para conflito, como duplicidade
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
| `POST` em `/blocos`, `/unidades` e `/usuarios` | `SINDICO`, `ADMINISTRADOR` |
| `GET` em qualquer rota | Qualquer usuário autenticado |

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

## Saúde da aplicação

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/actuator/health` | Público | Indica se a aplicação está no ar |
