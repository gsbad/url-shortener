# Deployment

Não existe ADR dedicada a esta decisão (o racional do primeiro deploy
mora no plano de consolidação da fábrica,
`_platform/documentation/plano-ciclo-catalogo-deploy-observabilidade.md`).
Este documento registra o que existiu, para o caso de o projeto voltar a
ser deployado no futuro — e o decommission, que é o estado atual.

---

## Arquitetura (histórica — decomissionada, ver seção abaixo)

```text
Internet
   │  porta 8002
OCI Compute oci-lab-vm-2 (VM.Standard.E2.1.Micro, Always Free —
   │                       compartilhada com dice-api :8000 e reforma-casa :8003)
   ▼
Docker container "url-shortener" (rede url-shortener-net)
   │
   ▼
Docker container "url-shortener-db" (Postgres 16, volume nomeado
                                       url-shortener-db-data)
```

```text
git push main
   │
   ▼
GitHub Actions (.github/workflows/ci.yml, job gerado pelo template
"Publicar lab na OCI" do Backstage Scaffolder):
   build → docker (build+smoke, com Postgres efêmero) → publish → deploy
                                                              │        │
                                                              ▼        ▼
                                                     ghcr.io/gsbad/   SSH → oci-lab-vm-2
                                                     url-shortener
```

Secrets usados pelo job `deploy` (ainda existem no repositório, sem uso
atual): `OCI_SSH_KEY`, `OCI_HOST`, `OCI_USER`, `URLSHORTENER_DB_PASSWORD`.
`GHCR_VISIBILITY_PAT` também existe, órfão desde antes do decommission
(gap de automação de visibilidade do GHCR, ver `development-workflow.md`
§Gap de autenticação GHCR — nunca foi usado de fato).

---

## Descomissionamento

**Status: decomissionado — 2026-09-04.** `oci-lab-vm-2` foi consolidada
pelo usuário para hospedar só o `reforma-casa` — não é nada que
`url-shortener` tenha feito de errado, é consolidação da fábrica (ver
`_platform/documentation/plano-consolidacao-labs-e-observabilidade.md`).
Dado do Postgres era só material de teste (URLs encurtadas de
laboratório, sem uso real — ver README) — removido junto, sem backup.

```bash
# parou e removeu os containers e o volume deste projeto
ssh -i ~/.ssh/reforma-casa_oci ubuntu@129.80.95.224 "
  docker stop url-shortener url-shortener-db
  docker rm url-shortener url-shortener-db
  docker volume rm url-shortener-db-data
  docker network rm url-shortener-net
"

# removeu a regra de porta 8002 na Security List dice-api-sl (oci CLI)
# removeu a regra de iptables 8002 no host (via SSH, mesma VM)
```

Job `deploy` removido de `.github/workflows/ci.yml` — `build`/`docker`/
`publish` continuam rodando normalmente a cada push (o smoke test do job
`docker` sobe seu próprio Postgres efêmero, não depende da VM). A imagem
`ghcr.io/gsbad/url-shortener` continua publicada no GHCR, sem remoção.

Secrets do GitHub (`OCI_SSH_KEY`, `OCI_HOST`, `OCI_USER`,
`URLSHORTENER_DB_PASSWORD`) **não** foram removidos — redeploy é
possível reabrindo a porta na Security List, recriando o job `deploy` e
reexecutando o pipeline; nada na aplicação mudou.
