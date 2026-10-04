# Vicino

Sistema de gestão de condomínios. O MVP cobre o **agendamento de espaços comuns** (quadra, churrasqueiras, salão de festas) e a **liberação de visitantes**, inicialmente para um único condomínio.

## Status

Em fase de arquitetura e estruturação inicial.

## Atores

- **Morador**: reserva espaços e autoriza visitantes
- **Porteiro**: valida a entrada de visitantes
- **Síndico/administração**: gerencia espaços, regras e aprovações

## Stack

| Camada | Tecnologia |
|---|---|
| Back-end | Java 25, Spring Boot 4.1.1, Maven |
| Persistência | Spring Data JPA, PostgreSQL, Flyway |
| Front-end | React.js |
| Deploy | Railway |

## Estrutura do repositório

```
Vicino/
├── README.md
├── .gitignore
├── docs/        # documentação e decisões de arquitetura (ADRs)
├── backend/     # API REST (Spring Boot)
└── frontend/    # aplicação web (React)
```

## Como rodar o back-end

Pré-requisitos: JDK 25 e um PostgreSQL acessível.

1. Configure as variáveis de ambiente do banco (URL, usuário e senha) usadas pelo `application.properties`.
2. Na pasta `backend/`, execute:

```bash
./mvnw spring-boot:run
```

No Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

Com a aplicação no ar, o endpoint de saúde fica em `/actuator/health`.

## Documentação

As decisões de arquitetura ficam em `docs/adr/`, um arquivo por decisão, numerados em sequência.

## Licença

A definir.