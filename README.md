# url-shortener

API REST que encurta URLs. **Primeiro produto-piloto da Indie Software Factory.**

> Este projeto é um **laboratório**. Ele existe para validar o ciclo completo da
> fábrica — ideia → template → código → testes → Docker → CI —, não para ser um
> encurtador de URLs sério. O domínio foi escolhido por ser trivial: um domínio
> complexo esconderia a fricção da fábrica atrás da fricção do problema.

---

## Stack

| | |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4 (Web, Validation, Actuator, Data JPA) |
| Banco | PostgreSQL 16 |
| Migração | Flyway |
| Build | Maven |
| Testes | JUnit 5 + MockMvc — 16 testes |
| Container | Docker (multi-stage) + Compose |

A versão do Java está fixada em `.tool-versions`. Sem isso, um Java 17 ativo
globalmente faz o build falhar.

---

## API

### `POST /api/urls`

```bash
curl -X POST http://localhost:8080/api/urls \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com/pagina-bem-longa"}'
```

```json
{
  "code": "4c99",
  "url": "https://example.com/pagina-bem-longa",
  "shortUrl": "http://localhost:8080/4c99"
}
```

Responde `201` com header `Location`. URL precisa começar com `http://` ou
`https://` — outros esquemas recebem `400`.

### `GET /api/urls/{code}`

Devolve o mesmo objeto. Código inexistente recebe `404`.

### `GET /{code}`

Redireciona (`302`) para a URL original.

### `GET /health` · `GET /actuator/health`

Health check próprio e o do Actuator.

---

## Rodar localmente

Os testes e a aplicação precisam de um PostgreSQL. O `compose.yaml` fornece:

```bash
docker compose up -d db          # só o banco
mvn spring-boot:run              # aplicação a partir do código
```

Ou tudo em containers:

```bash
docker compose up --build
```

## Testes

```bash
docker compose up -d db          # obrigatório: os testes usam PostgreSQL real
mvn test
```

**Os testes não são autocontidos** — exigem o banco no ar. Isso é uma dívida
conhecida, registrada em [ADR 0003](docs/adr/0003-testes-com-postgresql-externo.md):
Testcontainers resolveria, mas está bloqueado neste ambiente.

Testar contra H2 não é alternativa: as migrações usam `BIGSERIAL`, `TIMESTAMPTZ`
e `SEQUENCE`, então o schema validado seria diferente do que roda de verdade.

---

## Configuração

Toda configuração vem do ambiente. O `application.yml` usa interpolação
(`${VAR:padrão}`) — nunca credencial literal.

| Variável | Padrão | Uso |
|---|---|---|
| `APP_BASE_URL` | `http://localhost:8080` | base da `shortUrl` devolvida |
| `ENVIRONMENT` | `development` | ambiente, exibido no health |
| `DB_HOST` · `DB_PORT` | `localhost` · `5432` | PostgreSQL |
| `DB_NAME` · `DB_USER` · `DB_PASSWORD` | `urlshortener` | credenciais |

Os padrões coincidem com o `compose.yaml` de propósito — são valores de
desenvolvimento local, não secrets.

---

## Estrutura

```text
src/main/java/dev/gustavosa/urlshortener/
├── Application.java
├── config/AppProperties.java       configuração tipada
├── controller/                     camada HTTP, sem regra de negócio
├── domain/                         entidade e repositório
└── service/                        Base62, regra de geração de código

src/main/resources/db/migration/    migrações Flyway
docs/adr/                           decisões arquiteturais
```

---

## Decisões

| ADR | Decisão |
|---|---|
| [0002](docs/adr/0002-gerar-codigo-com-sequence-base62.md) | Código curto via sequence do PostgreSQL em Base62 |
| [0003](docs/adr/0003-testes-com-postgresql-externo.md) | PostgreSQL externo nos testes, em vez de Testcontainers |

---

## Escopo — o que deliberadamente não existe

```text
autenticação · usuários · frontend · cache · Redis · mensageria
observabilidade · rate limiting · métricas de clique · expiração
URL customizada · deduplicação · Kubernetes · deploy em nuvem
```

Não é backlog: é fronteira. O objetivo era validar a fábrica, e cada item
acrescentado tornaria a medição menos limpa.

---

## Armadilhas

- **`GET /{code}` captura qualquer caminho de primeiro nível.** O Spring dá
  precedência ao literal, então `/health` continua funcionando — há um teste que
  trava esse comportamento. Mas uma rota nova de primeiro nível colide.
- **Códigos são sequenciais e adivinháveis.** Consequência aceita do ADR 0002.
  Para um encurtador público importaria; para um laboratório, não.
- **Testes precisam do banco no ar** — ver acima.
