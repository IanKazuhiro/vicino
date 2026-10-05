# 0003 - Hash de senha com BCrypt antes da autenticação completa

- **Status:** aceita
- **Data:** 2026-10-04

## Contexto

O cadastro de usuários precisa guardar senhas de forma segura desde o primeiro registro. A autenticação (login, emissão de token e controle de acesso) é uma etapa posterior. Adicionar o `spring-boot-starter-security` agora protegeria automaticamente todos os endpoints, o que impediria testar os cadastros de blocos, unidades e usuários antes de existir um fluxo de login.

## Decisão

- As senhas são gravadas apenas como **hash BCrypt**, na coluna `senha_hash`. A senha em texto puro nunca é salva nem devolvida pela API.
- Nesta fase, o projeto usa só a biblioteca `spring-security-crypto`, que fornece o `BCryptPasswordEncoder` sem ativar nenhuma proteção de rotas.
- O encoder é exposto como bean `PasswordEncoder` em `config/PasswordConfig`, e os services dependem da interface, não da implementação.
- A senha aceita de 8 a 72 caracteres, porque o BCrypt ignora o que passa de 72 bytes.

## Alternativas consideradas

- **Adicionar o `spring-boot-starter-security` já agora:** traria o encoder, mas bloquearia todos os endpoints até a autenticação estar pronta, ou exigiria uma configuração provisória liberando tudo.
- **Outro algoritmo (Argon2, PBKDF2):** também são seguros, mas o BCrypt é o padrão do Spring Security e atende bem ao projeto.

## Consequências

- Os endpoints continuam **abertos** até a etapa de autenticação. A aplicação não deve ser publicada nesse estado.
- Na etapa de autenticação, a dependência será trocada pelo `spring-boot-starter-security` (que já inclui o `spring-security-crypto`), e o bean `PasswordEncoder` continuará sendo usado sem mudanças.
- Os hashes já gravados continuam válidos depois da troca.
