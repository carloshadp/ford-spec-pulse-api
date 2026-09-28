# Evidências reais de execução local

Saída bruta de ferramentas de segurança rodadas localmente via Docker contra
o código e a imagem deste projeto — não simuladas, não editadas (além de
cortar ruído de progresso do pull de imagem).

| Arquivo | Ferramenta | Alvo | Resultado |
|---|---|---|---|
| `gitleaks-api.txt` | Gitleaks v8 (imagem oficial) | Histórico git de `ford-spec-pulse-api` | 1 achado — falso positivo confirmado (nome de header numa string de exemplo em comentário, não segredo real) |
| `gitleaks-mobile.txt` | Gitleaks v8 | Histórico git de `FordSpecPulse_MobileAPP` | Nenhum segredo encontrado |
| `trivy-api.txt` | Trivy (imagem oficial) | `ford-spec-pulse-api:evidencia` com Spring Boot 3.3.5 | 34 vulnerabilidades (26 HIGH, 8 CRITICAL) nas dependências Java empacotadas no jar |
| `trivy-api-pos-correcao.txt` | Trivy | Mesma imagem, após bump para Spring Boot 3.3.13 | Ver comparação abaixo |
| `semgrep-api.txt` | Semgrep (`p/java`, `p/owasp-top-ten`, `p/secrets`) | `ford-spec-pulse-api` (código + os próprios workflows YAML criados nesta entrega) | 15 achados — todos nos arquivos `.github/**` que acabamos de criar, não no código Java. Ver correção abaixo |
| `semgrep-api-pos-correcao.txt` | Semgrep, mesma config | Mesmo repositório, após corrigir os 15 achados | 0 achados |
| `semgrep-mobile.txt` | Semgrep (`p/typescript`, `p/react`, `p/owasp-top-ten`, `p/secrets`) | `FordSpecPulse_MobileAPP` | 0 achados (workflow já criado com as correções aplicadas) |
| `swagger-ui.jpg` | Chrome (print real) | API rodando localmente (`localhost:8081/swagger-ui`) | Documentação OpenAPI real, gerada pelo mesmo código testado no pipeline |
| `dashboard-grafana.jpg` | Chrome (print real) | Grafana local (`localhost:3000`), dashboard provisionado em `../monitoramento/` | Métricas reais de uma sessão de tráfego real contra a API (login, 403, 429, 500 do bug B1) |
| `prometheus-targets.jpg` | Chrome (print real) | Prometheus local (`localhost:9090/targets`) | Confirma que o scrape contra `host.docker.internal:8081/actuator/prometheus` está `UP` de verdade |
| `logs-json-prod.txt` | Saída real da aplicação | API local com `SPRING_PROFILES_ACTIVE=prod,observabilidade` | Logs estruturados JSON reais (login falho, IP bloqueado, rate limit excedido) — não simulados |
| `backup-real.txt` | Saída real (curl) | `POST /api/admin/backup` na API local | 201 com o arquivo `.zip` gerado e listado em disco; 403 para perfil não-admin |
| `rate-limit-real.txt` | Saída real (curl) | 12 logins em rajada na API local | 200 até o limite, depois 429 `RATE_LIMIT_EXCEEDED` |
| `prometheus-alert-firing.jpg` | Chrome (print real) | Prometheus local (`/alerts`) | Alerta `PicoDeRespostasDeSeguranca` em `FIRING`, com query, tempo ativo e valor |
| `github-actions-run.jpg` | Chrome (print real) | [Execução real do pipeline no GitHub Actions — API](https://github.com/carloshadp/ford-spec-pulse-api/actions/runs/36355442796), commit `49e536f` | Os 5 jobs (`build-test`, `sast`, `secret-scanning`, `sca`, `container-scan`) com sucesso, 3m57s |
| `github-actions-run-mobile.jpg` | Chrome (print real) | [Execução real do pipeline no GitHub Actions — Mobile](https://github.com/BrnBastos/FordSpecPulse_MobileAPP/actions/runs/36357417397), commit `f7fb4fe` | Os 4 jobs (`build-test`, `sast`, `secret-scanning`, `sca`) com sucesso, 41s |

## O achado mais relevante: CVE-2026-22732 (CRITICAL)

O Trivy encontrou `CVE-2026-22732` em `spring-security-web 6.3.4` — bypass de
política de segurança / divulgação de informação, corrigido em 6.5.9/7.0.4 —
usando exatamente a mesma versão do Spring Security que este projeto
declarava (`spring-boot-starter-parent 3.3.5`). Isso é evidência real de que
o scanner funciona e de que a rotina de revisão de dependências (seção 4 do
documento de compliance) tem trabalho de verdade para fazer, não é só teoria.

**Ação tomada nesta entrega:** subimos `spring-boot-starter-parent` de `3.3.5`
para `3.3.13` — mesma linha minor, só patches — e reexecutamos a suíte de 69
testes (todos passando) antes e depois.

**Resultado real, sem maquiagem:** o total caiu de 34 para 28 vulnerabilidades
(HIGH: 26→21, CRITICAL: 8→7) — comparação em `trivy-api.txt` ×
`trivy-api-pos-correcao.txt`. **A CVE-2026-22732 continua presente**
(`spring-security-web` foi de `6.3.4` para `6.3.10`, ainda abaixo da versão
corrigida `6.5.9`). O motivo: o patch dela só saiu a partir do Spring Security
6.5.x, que acompanha Spring Boot 3.5.x — um patch dentro da linha 3.3.x não
alcança. Corrigi-la de fato exige subir de minor version (3.3 → 3.5 ou maior),
uma migração com risco de regressão real demais para o escopo mínimo desta
entrega. **Fica registrado como o item de maior prioridade da rotina de
revisão de dependências** (documento 4, seção 4) — não maquiado como
resolvido.

## O segundo achado relevante: nosso próprio pipeline tinha os problemas que ele deveria pegar

Rodar o Semgrep contra o repositório inteiro (não só contra o código Java)
pegou 15 achados — todos nos arquivos `.github/workflows/security.yml` e
`.github/dependabot.yml` que criamos nesta mesma entrega:

1. **`github-actions-mutable-action-tag`** (13 ocorrências): toda action
   (`actions/checkout@v4`, `semgrep/semgrep-action@v1`, etc.) estava referenciada
   por tag mutável, não por SHA de commit — o próprio Semgrep cita os
   incidentes reais do `trivy-action` e do `kics-github-action` como exemplo
   desse padrão de risco de supply-chain (tag reapontada pelo autor da action).
   Corrigido: todas as `uses:` agora referenciam um SHA
   de 40 caracteres (comentado com a versão, ex.:
   `actions/checkout@11d5960a326750d5838078e36cf38b85af677262 # v4.4.0`).
2. **`dependabot-missing-cooldown`** (2 ocorrências, uma por `dependabot.yml`):
   sem período de cooldown, o Dependabot pode propor a versão de um pacote
   recém-publicado antes de a comunidade detectar que ele foi comprometido.
   Corrigido: `cooldown: default-days: 7` em cada ecossistema.

Bônus descoberto ao resolver as SHAs: `aquasecurity/trivy-action@0.24.0`, como
escrito originalmente, **nem existia** como tag (faltava o prefixo `v`) — o
job `container-scan` teria falhado na primeira execução real no GitHub
Actions. Corrigido para `v0.36.0` (a mais recente), pinada por SHA.

Reexecutar o Semgrep depois das correções (`semgrep-api-pos-correcao.txt`,
`semgrep-mobile.txt`) confirma 0 achados nos dois repositórios.

## O pipeline rodou de verdade no GitHub Actions — nos dois repositórios

Depois de commitar, fizemos push dos dois repositórios para `origin/main`.
Isso disparou o `security.yml` de verdade no GitHub Actions em ambos, além
dos jobs iniciais do Dependabot em cada um, todos com sucesso:

- **API:** 5/5 jobs (`build-test`, `sast`, `secret-scanning`, `sca`,
  `container-scan`), 3m57s. Print real em `github-actions-run.jpg`, run
  completo em
  https://github.com/carloshadp/ford-spec-pulse-api/actions/runs/36355442796.
- **Mobile:** 4/4 jobs (`build-test`, `sast`, `secret-scanning`, `sca`),
  41s. Print real em `github-actions-run-mobile.jpg`, run completo em
  https://github.com/BrnBastos/FordSpecPulse_MobileAPP/actions/runs/36357417397.

## Como reproduzir

```bash
# Secret scanning
docker run --rm -v "<repo>:/repo" zricethezav/gitleaks:latest detect --source=/repo -v --redact

# SAST
docker run --rm -v "<repo>:/src" semgrep/semgrep:latest semgrep scan --config=p/java --config=p/owasp-top-ten --config=p/secrets /src

# Container scan
docker build -t ford-spec-pulse-api:evidencia ford-spec-pulse-api
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest \
  image --severity CRITICAL,HIGH --scanners vuln ford-spec-pulse-api:evidencia

# Dashboard (Prometheus + Grafana) — ver ../monitoramento/README embutido no docker-compose.yml
cd ford-spec-pulse-api && PORT=8081 SPRING_PROFILES_ACTIVE=observabilidade mvn spring-boot:run &
cd ../Entrega/monitoramento && docker compose up -d
# abrir http://localhost:3000 (admin/admin)
```
