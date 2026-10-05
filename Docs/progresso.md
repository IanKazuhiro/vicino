# Progresso do projeto

Registro do que foi feito a cada commit, do que está pendente e dos próximos passos. **Atualize este arquivo junto com cada commit.**

## Estado atual

- **Back-end:** cadastro de blocos, unidades e usuários funcionando (criar, listar e buscar por id), testado manualmente no Postman.
- **Segurança:** senhas salvas com hash BCrypt. Os endpoints ainda **não exigem autenticação** (ver [ADR 0003](0003-bcrypt-sem-spring-security-completo.md)).
- **Front-end:** não iniciado.

## Histórico de commits

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
- [ ] **Estrutura de pacotes vs. [ADR 0001](0001-monolito-modular.md):** o ADR prevê módulos por domínio, mas o código está organizado por camada (`controller`, `service`, `model`, ...). Decidir se o ADR será revisto ou se o código será reorganizado.
- [ ] **Erros de validação (400)** saem no formato padrão do Spring. Avaliar um tratamento global com `@RestControllerAdvice` para padronizar as mensagens.
- [ ] **Sem testes automatizados** além do teste de contexto gerado pelo Spring.

## Próximos passos

1. **Autenticação e autorização** (ver [ADR 0004](0004-perfis-e-permissoes.md)): login com JWT e controle de acesso por perfil, trocando o `spring-security-crypto` pelo `spring-boot-starter-security`.
2. Cadastro de **espaços comuns** e suas regras.
3. **Reservas**, com bloqueio de conflito de horário.
4. **Visitantes**: autorização pelo morador e validação pela portaria.
