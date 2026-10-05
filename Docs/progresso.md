# Progresso do projeto

Registro do que foi feito a cada commit, do que está pendente e dos próximos passos. **Atualize este arquivo junto com cada commit.**

## Estado atual

- **Back-end:** cadastro de blocos, unidades e usuários funcionando (criar, listar e buscar por id), testado manualmente no Postman.
- **Segurança:** login com JWT (`POST /auth/login`) e controle de acesso por perfil. Cadastros só para `SINDICO` e `ADMINISTRADOR`; consultas para qualquer usuário autenticado (ver [ADR 0004](0004-perfis-e-permissoes.md) e [ADR 0005](0005-autenticacao-jwt.md)). Senhas salvas com hash BCrypt.
- **Front-end:** não iniciado.

## Histórico de commits

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
- [ ] **Estrutura de pacotes vs. [ADR 0001](0001-monolito-modular.md):** o ADR prevê módulos por domínio, mas o código está organizado por camada (`controller`, `service`, `model`, ...). Decidir se o ADR será revisto ou se o código será reorganizado.
- [ ] **Erros de validação (400)** saem no formato padrão do Spring. Avaliar um tratamento global com `@RestControllerAdvice` para padronizar as mensagens.
- [ ] **Sem testes automatizados** além do teste de contexto gerado pelo Spring. O teste de contexto agora também depende do `JWT_SECRET` configurado.
- [ ] **Respostas 401 e 403 sem corpo:** padronizar junto com o tratamento global de erros.
- [ ] **`GET /usuarios` aberto a qualquer usuário autenticado:** morador e portaria conseguem ver nome e e-mail de todos os usuários. Avaliar restrição por causa da LGPD (ponto em aberto do [ADR 0004](0004-perfis-e-permissoes.md)).
- [ ] **Usuário desativado mantém acesso** até o token expirar (no máximo 60 minutos). Aceitável no MVP; não existe endpoint para desativar usuários ainda.
- [ ] **Sem refresh token:** depois de 60 minutos é preciso logar de novo.
- [ ] **Sem endpoint para o usuário consultar os próprios dados** (por exemplo, `GET /usuarios/me`), útil para o front-end saber quem está logado.

## Próximos passos

1. Cadastro de **espaços comuns** e suas regras.
2. **Reservas**, com bloqueio de conflito de horário.
3. **Visitantes**: autorização pelo morador e validação pela portaria.
