# url-shortener

API REST que encurta URLs — primeiro produto-piloto da fabrica

---

## Stack

| | |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4 (Web, Validation, Actuator) |
| Build | Maven |
| Testes | JUnit 5, MockMvc |
| Container | Docker (build multi-stage) |

A versão do Java está fixada em `.tool-versions` (asdf). Sem isso, um Java 17
ativo globalmente faria o build falhar.

## Rodar localmente

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

| Rota | O que faz |
|---|---|
| `GET /health` | health check próprio da aplicação — status, nome e ambiente |
| `GET /actuator/health` | health check do Actuator |

Os dois existem de propósito: o formato do Actuator pode mudar entre versões do
Spring Boot, e quem consome o health check (proxy, CI, orquestrador) não
deveria quebrar por causa disso. `/health` é o contrato estável.

## Testes

```bash
mvn test              # testes
mvn verify            # testes + empacotamento
```

## Build

```bash
mvn clean package
java -jar target/url-shortener-0.1.0-SNAPSHOT.jar
```

## Docker

```bash
docker build -t url-shortener .
docker run -p 8080:8080 url-shortener

# ou, com compose
docker compose up --build
```

O `Dockerfile` usa build multi-stage: a imagem final leva só o JRE e o jar, sem
Maven nem código-fonte. Roda como usuário sem privilégio e traz `HEALTHCHECK`.

## Configuração

Toda configuração vem de variável de ambiente. O `application.yml` usa
interpolação (`${VAR:padrão}`) — **nunca colocar credencial nele**.

```bash
cp .env.example .env
```

| Variável | Padrão | Uso |
|---|---|---|
| `ENVIRONMENT` | `development` | ambiente atual |
| `SERVER_PORT` | `8080` | porta HTTP |

## Estrutura

```text
src/main/java/dev/gustavosa/urlshortener/
├── Application.java        ponto de entrada
├── controller/             camada HTTP — sem lógica de negócio
└── config/                 configuração do Spring

src/main/resources/
└── application.yml

src/test/java/…             espelha a estrutura de main
docs/adr/                   Architecture Decision Records
```

Ao crescer, acrescentar: `service/`, `repository/`, `entity/`, `dto/`,
`mapper/`, `exception/`. **Criar só quando houver conteúdo.**

Regra que vale desde o primeiro dia: **nada de lógica de negócio no
Controller.** Controller recebe, valida e delega.

## Banco de dados

Este template **não** traz persistência de propósito — nem toda API precisa de
banco, e remover é mais trabalhoso que adicionar.

Para incluir: descomentar o bloco `spring-boot-starter-data-jpa` no `pom.xml`,
acrescentar o driver e o Flyway, e descomentar o serviço `db` no `compose.yaml`.

## Documentação

- `docs/adr/` — decisões arquiteturais
- `catalog-info.yaml` — metadado do Backstage. A aplicação **não depende** dele.

---

## Ao criar um projeto a partir deste template

1. Substituir os placeholders: `url-shortener`, `API REST que encurta URLs — primeiro produto-piloto da fabrica`,
   `gsbad`.
2. Renomear o pacote `dev.gustavosa.urlshortener` e os diretórios correspondentes.
3. Revisar o `LICENSE`.
4. `git init -b main` e primeiro commit.
5. Apagar esta seção do README.

O script `_templates/new-project.sh` faz os passos 1, 2 e 4 automaticamente.
