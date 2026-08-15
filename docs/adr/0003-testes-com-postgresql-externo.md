# 0003 — Testes com PostgreSQL externo, em vez de Testcontainers

## Status

Aceita — 2026-08-15. **Provisória**: reverter para Testcontainers quando o
bloqueio for resolvido.

## Context

Os testes de integração precisam de um banco. As migrações usam `BIGSERIAL`,
`TIMESTAMPTZ` e `SEQUENCE` — tipos específicos do PostgreSQL. Testar contra H2
validaria um schema diferente do que roda de verdade: o teste passaria e a
aplicação quebraria.

Testcontainers é o padrão documentado da fábrica para Java
(`_platform/documentation/development-workflow.md`) e foi a primeira escolha.
Foi implementado com `@ServiceConnection` e **falhou**.

### Diagnóstico do bloqueio

O ambiente local é WSL2 com Docker Desktop 29.6.1. O CLI funciona, mas o
Testcontainers não encontra ambiente Docker válido:

```text
UnixSocketClientProviderStrategy: BadRequestException (Status 400)
DockerDesktopClientProviderStrategy: NullPointerException (getSocketPath() is null)
```

Investigando o socket diretamente:

```bash
curl --unix-socket /var/run/docker.sock http://localhost/v1.32/info   # 400
curl --unix-socket /var/run/docker.sock http://localhost/v1.44/info   # 200
docker version --format '{{.Server.MinAPIVersion}}'                   # 1.40
```

**Causa:** o `docker-java` que acompanha o Testcontainers negocia a API 1.32, e
o Docker 29 exige no mínimo 1.40. O daemon responde `400` ao handshake.

Tentativas que **não** resolveram:

- `DOCKER_HOST` apontando para o socket real do engine
  (`/mnt/wsl/docker-desktop-bind-mounts/Ubuntu/docker.sock`);
- `DOCKER_API_VERSION=1.44` — é variável do CLI, o `docker-java` não a lê;
- `~/.testcontainers.properties` com `api.version=1.44`.

## Decision

Usar um **PostgreSQL real provisionado fora do build**:

| Ambiente | Como |
|---|---|
| Local | `docker compose up -d db` |
| CI | bloco `services:` do workflow |

A fidelidade é a mesma — PostgreSQL de verdade nos dois casos. O que muda é
quem provisiona.

## Alternatives Considered

**H2 em memória.** Autocontido e rápido. Rejeitada porque validaria um schema
diferente do de produção, que é exatamente o erro que testar com banco real
evita.

**Esperar o Testcontainers.** Rejeitada porque bloquearia o objetivo do
laboratório, que é percorrer o ciclo até o CI.

**Trocar para Docker Engine nativo no WSL.** Provavelmente resolveria — o socket
seria o engine de verdade, sem proxy. Rejeitada porque contraria o
[ADR 0004 da fábrica](../../../_platform/documentation/decisions/0004-docker-via-integracao-docker-desktop.md)
e é uma decisão da fábrica, não deste projeto.

## Consequences

**Negativas.** Os testes **não são autocontidos**: `mvn test` falha se o banco
não estiver no ar, com um erro de conexão que não explica o que fazer. É
fricção real para quem clonar o projeto, mitigada apenas pelo README.

**Positivas.** Funciona hoje, igual em local e CI, sem dependência de
Testcontainers. O CI fica até mais rápido, porque o serviço sobe em paralelo ao
checkout.

## Como reverter

Quando o Testcontainers suportar a API do Docker 29 — ou se a fábrica migrar
para Docker Engine nativo —, reintroduzir `spring-boot-testcontainers` e a
classe `ContainersConfig` com `@ServiceConnection`, e remover o `services:` do
workflow. O restante dos testes não muda.
