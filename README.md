# Vicino

Sistema de gestão de condomínios. O MVP cobre o **agendamento de espaços comuns** (quadra, churrasqueiras, salão de festas) e a **liberação de visitantes**, inicialmente para um único condomínio.

## Status

Back-end em desenvolvimento. Os cadastros de **blocos, unidades e usuários** estão prontos e testados, com senhas guardadas em hash BCrypt. A próxima etapa é a **autenticação** (login com JWT e permissões por perfil). O front-end ainda não foi iniciado.

Detalhes do que foi feito, pendências e próximos passos estão em [Docs/progresso.md](Docs/progresso.md).

## Atores e perfis

| Perfil (`PerfilEnum`) | Papel |
|---|---|
| `MORADOR` | Reserva espaços e autoriza visitantes |
| `PORTARIA` | Valida a entrada de visitantes |
| `SINDICO` | Gerencia espaços, regras, aprovações e cadastros |
| `ADMINISTRADOR` | Gerencia o sistema e os cadastros |

As permissões de cada perfil estão no [ADR 0004](Docs/0004-perfis-e-permissoes.md).

## Stack

| Camada | Tecnologia |
|---|---|
| Back-end | Java 25, Spring Boot 4.1.1, Maven, Lombok |
| Persistência | Spring Data JPA, PostgreSQL, Flyway |
| Segurança | BCrypt (`spring-security-crypto`); Spring Security com JWT na próxima etapa |
| Front-end | React.js |
| Deploy | Railway |

## Estrutura do repositório

```
vicino/
├── README.md
├── .gitignore
├── Docs/        # documentação, decisões de arquitetura (ADRs) e progresso
├── backend/     # API REST (Spring Boot)
└── frontend/    # aplicação web (React), ainda não criada
```

### Back-end (`backend/src/main/java/br/com/vicino/api`)

```
config/       # configurações (ex.: PasswordConfig com o PasswordEncoder)
controller/   # endpoints REST
dto/          # records de entrada (Request) e saída (Response)
enums/        # PerfilEnum
model/        # entidades JPA (Bloco, Unidade, Usuario)
repository/   # interfaces Spring Data JPA
service/      # regras de negócio
```

As migrations do banco ficam em `backend/src/main/resources/db/migration` e são aplicadas pelo Flyway ao subir a aplicação. Migrations já aplicadas não devem ser editadas; qualquer mudança no banco entra em uma nova migration.

## Como rodar o back-end

Pré-requisitos: JDK 25 e um PostgreSQL acessível.

1. Copie `backend/.env.example` para `backend/.env` e preencha `DB_URL`, `DB_USER` e `DB_PASSWORD`. O arquivo `.env` não vai para o Git.
2. Na pasta `backend/`, execute:

```bash
./mvnw spring-boot:run
```

No Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

Com a aplicação no ar, o endpoint de saúde fica em `/actuator/health`.

Para só compilar (útil depois de renomear classes ou records):

```powershell
.\mvnw.cmd -q -DskipTests clean compile
```

## API

| Recurso | Rotas |
|---|---|
| Blocos | `POST /blocos`, `GET /blocos`, `GET /blocos/{id}` |
| Unidades | `POST /unidades`, `GET /unidades?blocoId=`, `GET /unidades/{id}` |
| Usuários | `POST /usuarios`, `GET /usuarios`, `GET /usuarios/{id}` |

Corpos, validações e códigos de resposta: [Docs/api.md](Docs/api.md).

> Os endpoints ainda **não exigem autenticação**. Não publique a aplicação neste estado.

## Documentação

| Documento | Conteúdo |
|---|---|
| [Visão e escopo](Docs/Visao&Escopo.md) | Problema, objetivo, escopo do MVP e perguntas em aberto |
| [Progresso](Docs/progresso.md) | Histórico por commit, pendências e próximos passos |
| [API](Docs/api.md) | Referência dos endpoints |
| [ADR 0001](Docs/0001-monolito-modular.md) | Monolito modular |
| [ADR 0002](Docs/0002-condominio-unico-no-mvp.md) | Condomínio único no MVP |
| [ADR 0003](Docs/0003-bcrypt-sem-spring-security-completo.md) | Hash de senha com BCrypt antes da autenticação completa |
| [ADR 0004](Docs/0004-perfis-e-permissoes.md) | Perfis de acesso e permissões |

As decisões de arquitetura (ADRs) ficam em `Docs/`, um arquivo por decisão, numerados em sequência.

## Licença

A definir.
