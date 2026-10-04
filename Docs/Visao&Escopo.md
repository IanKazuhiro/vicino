# 01 - Visão e escopo

## Problema

A gestão do dia a dia de um condomínio costuma depender de planilhas, cadernos de portaria e mensagens em grupos de aplicativos. Isso gera conflitos de reserva de áreas comuns, falta de controle sobre quem entra e sai e pouca transparência para moradores e administração.

## Objetivo

O Vicino centraliza o **agendamento de espaços comuns** e a **liberação de visitantes**, dando a moradores, porteiros e síndico uma fonte única e confiável de informação.

## Atores

| Ator | O que precisa fazer |
|---|---|
| Morador | Reservar espaços, consultar e cancelar reservas, autorizar visitantes |
| Porteiro | Consultar visitantes autorizados e registrar entrada e saída |
| Síndico / administração | Cadastrar espaços e regras, aprovar reservas quando exigido, gerenciar moradores e unidades |

## Escopo do MVP

**Entra:**
- Autenticação e perfis de acesso (morador, porteiro, síndico)
- Cadastro de blocos, unidades e moradores
- Cadastro de espaços comuns (quadra, churrasqueiras, salão de festas) com suas regras
- Reserva de espaços, com bloqueio de conflitos de horário
- Autorização de visitantes pelo morador e validação pelo porteiro
- Atendimento a **um único condomínio**

**Fica de fora (por enquanto):**
- Financeiro (boletos, taxas, prestação de contas)
- Assembleias e votações
- Ocorrências e chamados de manutenção
- Controle de encomendas
- Mural de avisos e notificações push
- Múltiplos condomínios (multi-tenancy)

## Premissas

- Aplicação web responsiva, usada em celular e desktop
- O porteiro precisa de uma tela rápida e simples
- O sistema guarda dados pessoais de visitantes, então precisa respeitar a LGPD

## Perguntas em aberto

- Quais regras valem para cada espaço (antecedência mínima e máxima, duração, limite de reservas por unidade)?
- Quais espaços exigem aprovação do síndico?
- Morador inadimplente pode reservar?
- Visitante tem validade por data e hora, ou por tempo de permanência?
- Quais dados do visitante são obrigatórios (nome, documento, placa)?
- Por quanto tempo os dados de visitantes são mantidos?