# 0002 - Condomínio único no MVP

- **Status:** aceita
- **Data:** 2026-10-03

## Contexto

O Vicino inicialmente atenderá apenas um condomínio. Suportar vários condomínios (multi-tenancy) exige isolar dados por condomínio em todas as tabelas e consultas, o que aumenta a complexidade do modelo e das regras de acesso.

## Decisão

O MVP **não terá multi-tenancy**. O sistema assume a existência de um único condomínio, sem coluna `condominio_id` nas tabelas.

## Alternativas consideradas

- **Multi-tenant desde o início**, com `condominio_id` em todas as tabelas e filtro obrigatório em todas as consultas: prepara o sistema para crescer, mas adiciona custo e complexidade que o MVP não exige.

## Consequências

- Modelo de dados e consultas mais simples agora
- Migrar para multi-tenancy no futuro exigirá alterar as tabelas (nova coluna e chaves), as consultas e as regras de autorização, com uma migration de dados
- Para reduzir esse custo, evitar no código suposições rígidas de que existe um único condomínio (por exemplo, não espalhar constantes ou configurações globais que seriam específicas de cada condomínio)
- Esta decisão deve ser revisitada se surgir demanda concreta por um segundo condomínio
