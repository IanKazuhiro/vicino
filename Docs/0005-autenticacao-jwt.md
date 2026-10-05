# 0005 - Autenticação com JWT (HS256) e OAuth2 Resource Server

- **Status:** aceita
- **Data:** 2026-10-05

## Contexto

Os cadastros precisam ser protegidos conforme os perfis do [ADR 0004](0004-perfis-e-permissoes.md). A API é consumida por um front-end React separado e será publicada no Railway, então a autenticação deve funcionar sem sessão no servidor e permitir mais de uma instância da aplicação.

## Decisão

- **Login:** `POST /auth/login` recebe e-mail e senha, confere a senha com o `PasswordEncoder` (BCrypt) e devolve um **JWT** com validade de 60 minutos.
- **Formato do token:** assinado com **HS256** (chave simétrica). A chave vem da variável de ambiente `JWT_SECRET` e deve ter pelo menos 32 bytes.
- **Claims:**
  - `iss`: `vicino-api`
  - `sub`: id do usuário (não o e-mail, que pode mudar)
  - `iat` e `exp`: emissão e expiração
  - `perfil`: perfil do usuário (`MORADOR`, `PORTARIA`, `SINDICO`, `ADMINISTRADOR`)
- **Validação:** feita pelo **OAuth2 Resource Server do Spring Security** (`spring-boot-starter-security-oauth2-resource-server`), com `NimbusJwtEncoder` e `NimbusJwtDecoder`. Não há filtro JWT escrito à mão nem biblioteca de terceiros.
- **Perfis:** um `JwtAuthenticationConverter` transforma o claim `perfil` em authority `ROLE_<PERFIL>`, usada pelas regras `hasAnyRole(...)` do `SecurityConfig`.
- **Sem sessão:** `SessionCreationPolicy.STATELESS` e CSRF desabilitado, já que a API não usa cookies.
- **Falha de login:** e-mail inexistente, usuário inativo e senha errada respondem igual (**401**, "E-mail ou senha inválidos."), para não revelar quais e-mails estão cadastrados.
- **Administrador inicial:** o `AdminInicial` (um `ApplicationRunner`) cria um usuário `ADMINISTRADOR` na inicialização a partir de `ADMIN_EMAIL` e `ADMIN_SENHA`, se ainda não existir nenhum. Sem isso, um banco novo não teria ninguém autorizado a cadastrar usuários.

## Alternativas consideradas

- **Filtro JWT próprio com a biblioteca `jjwt`:** muito usado em tutoriais, mas exige escrever e manter o filtro, a extração do header e o tratamento de erros, que o Resource Server já faz.
- **RS256 (par de chaves pública e privada):** permite que outros serviços validem tokens sem conhecer a chave de assinatura. Não há outros serviços no MVP, então a chave simétrica é mais simples.
- **Sessão no servidor com cookie:** exigiria proteção CSRF e sessão compartilhada entre instâncias.
- **Usuário inicial criado por migration:** deixaria um hash de senha fixo versionado no repositório.

## Consequências

- O `JWT_SECRET` precisa ser configurado em cada ambiente e **nunca** pode ir para o Git. Trocar o segredo invalida todos os tokens emitidos.
- O payload do token é apenas codificado em Base64, não criptografado. Nenhum dado sensível deve ser colocado nele.
- **Sem refresh token:** depois de 60 minutos, o usuário faz login de novo.
- **Sem revogação:** um usuário desativado continua com acesso até o token expirar (no máximo 60 minutos).
- As respostas 401 e 403 geradas pelo Spring Security não têm corpo.
- A rota `/error` precisa ficar liberada (`permitAll`); senão, erros em rotas públicas, como um 400 no login, seriam convertidos em 401.
- Uma requisição com header `Authorization: Bearer` inválido recebe 401 mesmo em rotas públicas. O login deve ser chamado sem esse header.
- Depois do primeiro deploy, a variável `ADMIN_SENHA` pode ser removida do ambiente: o `AdminInicial` não faz nada quando já existe um administrador.
