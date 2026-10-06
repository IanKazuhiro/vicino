# 0001 - Monolito modular

- **Status:** aceita; a divisão interna em módulos por domínio foi revista em 2026-10-06 (ver [Revisão](#revisão-2026-10-06-pacotes-por-camada))
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

## Revisão (2026-10-06): pacotes por camada

**Mudança:** o monolito único continua valendo, mas o código **não** será dividido em módulos por domínio. Os pacotes ficam organizados por camada, como já estavam desde o início:

```
config/  controller/  dto/  enums/  model/  repository/  service/
```

**Motivo:** quando a decisão foi revista, os cadastros de blocos, unidades e usuários e a autenticação já tinham sido escritos por camada. O projeto é desenvolvido por uma pessoa e ainda é pequeno, então a organização por camada é mais simples de navegar, e reorganizar tudo antes do módulo de espaços não traria ganho concreto.

**Consequências:**
- Cada domínio novo (espaços, reservas, visitantes) adiciona seus arquivos nas pastas de camada existentes.
- Extrair um domínio para um serviço próprio fica mais trabalhoso, porque os arquivos de um mesmo domínio estão espalhados. Essa extração não está nos planos.
- Os services podem usar repositories de outros domínios (por exemplo, o `UsuarioService` usa o `UnidadeRepository`). Para conter o acoplamento, as regras de negócio de cada domínio continuam concentradas no service daquele domínio.
- Se o número de domínios crescer bastante, esta decisão deve ser reavaliada.
