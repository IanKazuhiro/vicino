# API REST

Referência dos endpoints disponíveis. Base local: `http://localhost:8080`.

> Por enquanto **nenhum endpoint exige autenticação**. As restrições por perfil estão definidas no [ADR 0004](0004-perfis-e-permissoes.md) e serão aplicadas na etapa de autenticação.

## Convenções

- Corpo das requisições e respostas em JSON (`Content-Type: application/json`).
- Criação devolve **201 Created** com o header `Location` apontando para o recurso criado.
- Erros de regra de negócio usam `ResponseStatusException`:
  - **400** para dados inválidos (validação do DTO ou regra de negócio)
  - **404** para recurso não encontrado
  - **409** para conflito, como duplicidade

## Blocos

| Método | Rota | Descrição | Respostas |
|---|---|---|---|
| POST | `/blocos` | Cria um bloco | 201, 400, 409 (nome repetido) |
| GET | `/blocos` | Lista todos os blocos | 200 |
| GET | `/blocos/{id}` | Busca um bloco | 200, 404 |

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

| Método | Rota | Descrição | Respostas |
|---|---|---|---|
| POST | `/unidades` | Cria uma unidade | 201, 400, 404 (bloco inexistente), 409 (número repetido no bloco) |
| GET | `/unidades` | Lista as unidades; aceita `?blocoId=` para filtrar | 200 |
| GET | `/unidades/{id}` | Busca uma unidade | 200, 404 |

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

| Método | Rota | Descrição | Respostas |
|---|---|---|---|
| POST | `/usuarios` | Cria um usuário | 201, 400, 404 (unidade inexistente), 409 (e-mail repetido) |
| GET | `/usuarios` | Lista todos os usuários | 200 |
| GET | `/usuarios/{id}` | Busca um usuário | 200, 404 |

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

| Método | Rota | Descrição |
|---|---|---|
| GET | `/actuator/health` | Indica se a aplicação está no ar |
