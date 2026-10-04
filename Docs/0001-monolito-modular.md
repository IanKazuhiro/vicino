# 0001 - Monolito modular

- **Status:** aceita
- **Data:** 2026-10-03

## Contexto

O Vicino está em fase inicial, é desenvolvido por uma pessoa e tem um escopo bem definido (reservas e visitantes) para um único condomínio. A prioridade é entregar valor com simplicidade, mantendo o código organizado para crescer.

## Decisão

O back-end será um **monolito modular**: uma única aplicação Spring Boot, dividida internamente em módulos por domínio (por exemplo `usuarios`, `unidades`, `espacos`, `reservas`, `visitantes`), com o front-end React em um projeto separado que consome a API REST.

## Alternativas consideradas

- **Microserviços**: permitem escalar e implantar partes de forma independente, mas trazem custo alto de infraestrutura, comunicação entre serviços e observabilidade, sem benefício real para o tamanho atual do projeto.
- **Monolito em camadas, sem módulos**: mais rápido no começo, mas tende a misturar regras de domínios diferentes e dificulta a evolução.

## Consequências

- Um único deploy e uma única base de código, o que simplifica o desenvolvimento e a operação
- Os módulos devem se comunicar por interfaces claras, sem acessar diretamente as tabelas uns dos outros, para facilitar uma eventual extração futura
- Se algum módulo exigir escala independente no futuro, ele poderá ser extraído para um serviço próprio
