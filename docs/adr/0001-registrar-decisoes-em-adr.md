# 0001 — Registrar decisões arquiteturais em ADR

## Status

Aceita.

## Context

Decisões arquiteturais tomadas e não registradas são reabertas meses depois,
sem que ninguém lembre por que a alternativa foi descartada. Num projeto de um
operador só, "ninguém" é você mesmo daqui a seis meses.

## Decision

Registrar decisões arquiteturais como ADRs neste diretório, numeradas
sequencialmente, no formato: Status, Context, Decision, Alternatives
Considered, Consequences.

Merecem ADR: escolha de linguagem, banco, framework de peso, estratégia de
deploy — qualquer decisão cara de reverter. Não merecem: biblioteca pequena,
formatação, organização interna de módulo.

## Alternatives Considered

**Não documentar.** Zero esforço. Rejeitada porque o custo aparece depois, na
forma de rediscussão.

**Documentar no vault do Obsidian.** Rejeitada porque a decisão precisa evoluir
junto com o código: quem clona o repositório precisa enxergá-la.

## Consequences

Cada decisão relevante custa alguns minutos de escrita. Em troca, o histórico
de "por que isto é assim" fica junto do código, versionado e legível por quem
chegar depois — inclusive você.

Este ADR é o exemplo do formato. Pode ser mantido ou apagado.
