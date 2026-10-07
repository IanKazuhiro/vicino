# Vicino

Sistema de gestão de condomínios. O MVP cobre o **agendamento de espaços comuns** (quadra, churrasqueiras, salão de festas) e a **liberação de visitantes**, inicialmente para um único condomínio.

## Status

Back-end em desenvolvimento. Os cadastros de **blocos, unidades, usuários e espaços comuns** estão prontos e testados, protegidos por **autenticação JWT** com permissões por perfil, e as senhas são guardadas em hash BCrypt. Cada espaço tem a própria janela de reserva, configurada pelo síndico ou administrador. O módulo de **reservas** está em andamento: banco, entidade e consultas prontos, endpoints na próxima etapa. O front-end ainda não foi iniciado.

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
| Segurança | Spring Security (OAuth2 Resource Server), JWT com HS256, senhas em BCrypt |
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
config/       # SecurityConfig (regras de acesso e JWT), PasswordConfig e AdminInicial
controller/   # endpoints REST
dto/          # records de entrada (Request) e saída (Response)
enums/        # PerfilEnum
model/        # entidades JPA (Bloco, Unidade, Usuario, Espaco, Reserva)
repository/   # interfaces Spring Data JPA
service/      # regras de negócio
```

A organização por camada foi uma decisão revista no [ADR 0001](Docs/0001-monolito-modular.md).

As migrations do banco ficam em `backend/src/main/resources/db/migration` e são aplicadas pelo Flyway ao subir a aplicação. Migrations já aplicadas não devem ser editadas; qualquer mudança no banco entra em uma nova migration.

> **Escreva migrations com a aplicação parada.** O devtools reinicia a aplicação a cada arquivo salvo; se uma migration for salva vazia ou incompleta nesse momento, o Flyway a registra como aplicada, e corrigi-la depois exige mexer no histórico do banco.

## Como rodar o back-end

Pré-requisitos: JDK 25 e um PostgreSQL acessível, com a extensão `btree_gist` disponível. Ela vem nas instalações padrão do PostgreSQL, e a migration V7 a instala; o usuário do banco precisa ter permissão para isso (por exemplo, ser dono do banco).

1. Copie `backend/.env.example` para `backend/.env` e preencha as variáveis. O arquivo `.env` não vai para o Git.

| Variável | Obrigatória | Uso |
|---|---|---|
| `DB_URL` | Não (padrão: `jdbc:postgresql://localhost:5432/vicino`) | URL do banco |
| `DB_USER` | Sim | Usuário do banco |
| `DB_PASSWORD` | Sim | Senha do banco |
| `JWT_SECRET` | Sim | Chave que assina os tokens. Mínimo de 32 bytes; trocá-la invalida todos os tokens emitidos. |
| `ADMIN_EMAIL` | Só na primeira execução | E-mail do administrador inicial |
| `ADMIN_SENHA` | Só na primeira execução | Senha do administrador inicial (mínimo 8 caracteres) |

Para gerar um `JWT_SECRET` no PowerShell:

```powershell
$b = New-Object byte[] 48; [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)
```

2. Na pasta `backend/`, execute:

```bash
./mvnw spring-boot:run
```

No Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

Com a aplicação no ar, o endpoint de saúde fica em `/actuator/health`.

Na primeira execução com um banco vazio, a aplicação cria um usuário `ADMINISTRADOR` com `ADMIN_EMAIL` e `ADMIN_SENHA`, e mostra no console `Administrador inicial criado`. Nas execuções seguintes, como o administrador já existe, nada é criado.

Para só compilar (útil depois de renomear classes ou records):

```powershell
.\mvnw.cmd -q -DskipTests clean compile
```

## API

| Recurso | Rotas |
|---|---|
| Autenticação | `POST /auth/login` (público) |
| Blocos | `POST /blocos`, `GET /blocos`, `GET /blocos/{id}` |
| Unidades | `POST /unidades`, `GET /unidades?blocoId=`, `GET /unidades/{id}` |
| Usuários | `POST /usuarios`, `GET /usuarios`, `GET /usuarios/{id}` |
| Espaços | `POST /espacos`, `PUT /espacos/{id}`, `GET /espacos`, `GET /espacos/{id}` |

Exceto o login e o `/actuator/health`, todas as rotas exigem o header `Authorization: Bearer <token>`, com o token obtido no login (válido por 60 minutos). Os `POST` de cadastro e o `PUT` de espaços exigem o perfil `SINDICO` ou `ADMINISTRADOR`.

Corpos, validações, permissões e códigos de resposta: [Docs/api.md](Docs/api.md).

## Documentação

| Documento | Conteúdo |
|---|---|
| [Visão e escopo](Docs/Visao&Escopo.md) | Problema, objetivo, escopo do MVP e perguntas em aberto |
| [Progresso](Docs/progresso.md) | Histórico por commit, pendências e próximos passos |
| [API](Docs/api.md) | Referência dos endpoints |
| [ADR 0001](Docs/0001-monolito-modular.md) | Monolito único, com pacotes por camada (revisão de 2026-10-06) |
| [ADR 0002](Docs/0002-condominio-unico-no-mvp.md) | Condomínio único no MVP |
| [ADR 0003](Docs/0003-bcrypt-sem-spring-security-completo.md) | Hash de senha com BCrypt antes da autenticação completa |
| [ADR 0004](Docs/0004-perfis-e-permissoes.md) | Perfis de acesso e permissões |
| [ADR 0005](Docs/0005-autenticacao-jwt.md) | Autenticação com JWT (HS256) e OAuth2 Resource Server |
| [ADR 0006](Docs/0006-espacos-comuns-e-janela-de-reserva.md) | Espaços comuns e janela de reserva configurável |

As decisões de arquitetura (ADRs) ficam em `Docs/`, um arquivo por decisão, numerados em sequência.

## Licença

A definir.
