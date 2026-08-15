# 0002 — Gerar o código curto com sequence do PostgreSQL em Base62

## Status

Aceita — 2026-08-15.

## Context

A API precisa produzir um código curto e único para cada URL. A escolha da
estratégia de geração determina se o código pode colidir, e colisão é o tipo de
problema que aparece só sob concorrência — difícil de reproduzir depois.

## Decision

Obter `nextval('short_code_seq')` do PostgreSQL e codificar o número em Base62.

`nextval` é atômico: dois pedidos concorrentes nunca recebem o mesmo valor.
Portanto **não há colisão e não há laço de repetição**.

A sequence começa em 1.000.000 para que os códigos tenham 4 caracteres desde o
primeiro registro. Começando em 1, os primeiros códigos teriam 1 caractere.

Base62 (dígitos + letras) evita caracteres que precisariam de escape em URL, ao
contrário de Base64.

## Alternatives Considered

**Código aleatório com constraint `UNIQUE` e retry.** Produz códigos não
adivinháveis. Rejeitada porque exige tratar colisão e repetir a inserção —
complexidade real, e caminho de código que quase nunca executa e por isso quase
nunca é testado.

**Hash da URL.** Daria deduplicação de graça. Rejeitada porque produz códigos
longos, e truncar o hash reintroduz colisão.

**UUID.** Sem colisão prática, mas 36 caracteres não é um código curto.

## Consequences

**Positivas.** Nenhuma colisão possível. Nenhum retry. A leitura quente
(resolver código) é atendida por um índice único.

**Negativas.** Os códigos são **sequenciais e portanto adivinháveis**: quem tem
um código pode enumerar os vizinhos. Para um encurtador público isso seria
inaceitável; para este laboratório é irrelevante, e está registrado como
consequência aceita, não como descuido.

Também acopla a geração ao PostgreSQL. Trocar de banco exigiria repensar isto —
o que é aceitável num projeto que já usa Flyway com tipos específicos do Postgres.
