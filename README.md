# Ford SpecPulse Twin — Backend

API REST para **inteligência competitiva automotiva**: recebe uma entrada simples (marca + modelo + versão + lista livre de atributos) e devolve uma ficha técnica padronizada. Também expõe análise comparativa Ford vs concorrentes com snapshot imutável.

---

## Stack

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 3.3.5 |
| Spring Security | OAuth2 Resource Server (JWT HS256) |
| Spring Data JPA | Hibernate 6 |
| Flyway | migrations V1–V7 |
| H2 | arquivo `./data/specpulse` |
| springdoc-openapi | Swagger UI |

---

## Como rodar

**Pré-requisitos:** JDK 21 e Maven 3.9+.

```powershell
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080`. As migrations Flyway aplicam schema e dados de seed automaticamente.

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| H2 Console | http://localhost:8080/h2-console |

Console H2 → JDBC URL: `jdbc:h2:file:./data/specpulse;DB_CLOSE_DELAY=-1;MODE=LEGACY` · usuário `sa` sem senha.

---

## Autenticação (JWT)

### Fluxo básico

```
POST /api/auth/register   →  cria conta (padrão: read_only)
POST /api/auth/login      →  { accessToken, refreshToken, expiresIn }
POST /api/auth/refresh    →  renova par de tokens (refresh token rotacionado)
POST /api/auth/logout     →  revoga todos os refresh tokens da sessão
GET  /api/usuarios/me     →  perfil e permissões do usuário autenticado
```

O `accessToken` expira em **15 minutos**. Use o `refreshToken` para renovar sem novo login. Inclua em todas as requisições protegidas:

```
Authorization: Bearer <accessToken>
```

### Usuários de teste (seed)

| Email | Senha | Perfil | Role | Permissões |
|---|---|---|---|---|
| `leitor@ford.internal` | `leitor123` | SOMENTE_LEITURA | read_only | leitura geral |
| `analista@ford.internal` | `analista123` | ANALISTA | analyst | leitura + comparações |
| `gerente@ford.internal` | `gerente123` | GERENTE | manager | leitura + comparações |
| `validador@ford.internal` | `validador123` | VALIDADOR_DADOS | data_validator | leitura + qualidade de dados |
| `admin@ford.internal` | `admin123` | ADMINISTRADOR | admin | acesso total |

---

## Endpoints

Todos os endpoints estão sob `/api`. As URLs de usuário aceitam também o alias `/api/users/*` para compatibilidade com o frontend.

### Autenticação — público

| Método | Path | Descrição |
|---|---|---|
| POST | `/api/auth/register` | Registra conta (padrão: read_only; admin pode definir outro perfil) |
| POST | `/api/auth/login` | Autentica com email + senha → access token (15 min) + refresh token (7 dias) |
| POST | `/api/auth/refresh` | Renova par de tokens usando refresh token válido |
| POST | `/api/auth/logout` | Revoga todos os refresh tokens do usuário autenticado |

### Usuários

| Método | Path | Perfil mínimo | Descrição |
|---|---|---|---|
| GET | `/api/usuarios/me` | qualquer autenticado | Perfil e roles do usuário logado |
| GET | `/api/usuarios` | admin | Lista todos os usuários (paginada) |
| PATCH | `/api/usuarios/{id}` | admin | Altera role e/ou status ativo do usuário |

### Marcas

| Método | Path | Perfil mínimo | Descrição |
|---|---|---|---|
| GET | `/api/marcas` | qualquer autenticado | Lista marcas; filtros: `q`, `isFord` |
| GET | `/api/marcas/{id}` | qualquer autenticado | Marca por UUID ou slug (ex: `brand-ford`) |

### Veículos

| Método | Path | Perfil mínimo | Descrição |
|---|---|---|---|
| GET | `/api/veiculos` | qualquer autenticado | Lista veículos; filtros: `q`, `brandId`, `isFord`, `segment`, `market`, `year` |
| GET | `/api/veiculos/{id}` | qualquer autenticado | Veículo por UUID ou slug |
| GET | `/api/veiculos/{id}/versoes` | qualquer autenticado | Versões do veículo; filtros: `powertrain`, `drivetrain` |

### Versões

| Método | Path | Perfil mínimo | Descrição |
|---|---|---|---|
| GET | `/api/versoes/{id}` | qualquer autenticado | Versão por UUID ou slug |
| GET | `/api/versoes/{id}/especificacoes` | qualquer autenticado | Specs paginadas; filtros: `category`, `status`, `confidenceLevel` |

### Atributos

| Método | Path | Perfil mínimo | Descrição |
|---|---|---|---|
| GET | `/api/atributos/taxonomia` | qualquer autenticado | Taxonomia canônica com sinônimos; filtros: `q`, `category` |

### Ficha técnica

| Método | Path | Perfil mínimo | Descrição |
|---|---|---|---|
| POST | `/api/fichas-tecnicas/consultar` | qualquer autenticado | Resolve marca + modelo + versão + lista de atributos em texto livre |

Body:

```json
{
  "marca": "Ford",
  "modelo": "Ranger Raptor",
  "versao": "Raptor 3.0 V6 Biturbo Gasolina 4x4 Cabine Dupla",
  "atributos": ["potencia", "torque", "pneus", "consumo urbano"]
}
```

Termos sinônimos (`"potencia"`, `"cv"`, `"cavalos"`) resolvem para o mesmo atributo canônico. Termos desconhecidos retornam `status: ATRIBUTO_DESCONHECIDO`. Atributos sem dado publicado retornam `status: NAO_INFORMADO`.

### Comparações

| Método | Path | Perfil mínimo | Descrição |
|---|---|---|---|
| POST | `/api/comparacoes` | analyst, manager, admin | Cria comparação (snapshot imutável) Ford vs concorrentes |
| GET | `/api/comparacoes` | qualquer autenticado | Lista comparações paginada |
| GET | `/api/comparacoes/{id}` | qualquer autenticado | Resultado completo da comparação |
| GET | `/api/comparacoes/{id}/matriz` | qualquer autenticado | Matriz de atributos; filtros: `category`, `status`, `difference`, `q` |

Body de criação:

```json
{
  "referenceVersionId": "version-ford-ranger-xlt-3-0-v6-diesel-4x4-cabine-dupla-2024",
  "competitorVersionIds": [
    "version-toyota-hilux-srx-2-8-diesel-4x4-cabine-dupla-2024",
    "version-chevrolet-s10-high-country-2-8-diesel-4x4-2024"
  ],
  "customerProfileId": "fleet",
  "title": "Ranger XLT vs Concorrentes — Frotista"
}
```

Aceita UUID ou slug como `referenceVersionId` e `competitorVersionIds`.

### Admin

| Método | Path | Perfil mínimo | Descrição |
|---|---|---|---|
| GET | `/api/admin/registro-auditoria` | admin | Log de auditoria paginado, mais recente primeiro |

### Stubs (contrato disponível, lógica na próxima sprint)

| Método | Path | Perfil mínimo |
|---|---|---|
| GET | `/api/lacunas` | qualquer autenticado |
| GET | `/api/comparacoes/{id}/lacunas` | qualquer autenticado |
| GET | `/api/recomendacoes` | qualquer autenticado |
| GET | `/api/alertas-mercado` | qualquer autenticado |
| GET | `/api/qualidade-dados/itens` | data_validator, admin |
| GET | `/api/uploads` | data_validator, admin |
| GET | `/api/perfis-clientes` | qualquer autenticado |
| GET | `/api/historico-analises` | qualquer autenticado |
| GET | `/api/versoes/{id}/fontes` | qualquer autenticado |
| POST | `/api/relatorios/exportar` | qualquer autenticado |
| GET | `/api/relatorios/{id}` | qualquer autenticado |

---

## IDs estáveis (slugs)

Os endpoints aceitam **UUID ou slug** — tentam parse UUID primeiro, depois scan por slug.

| Tipo | Padrão | Exemplo |
|---|---|---|
| Marca | `brand-{nome}` | `brand-ford` |
| Veículo | `vehicle-{marca}-{modelo}-{ano}` | `vehicle-ford-ranger-2024` |
| Versão | `version-{marca}-{modelo}-{nome}-{ano}` | `version-ford-ranger-xlt-3-0-v6-diesel-4x4-cabine-dupla-2024` |
| Atributo | `attr_{codigo}` | `attr_potencia_cv` |

---

## Categorias de atributos

| Valor na API | Descrição |
|---|---|
| `engine_transmission` | Motor e transmissão |
| `capacity_use` | Capacidade e utilidade |
| `safety` | Segurança |
| `traction_offroad` | Tração e off-road |
| `connectivity` | Conectividade |
| `comfort` | Conforto |
| `adas` | Assistentes de direção (ADAS) |
| `digital_cockpit` | Painel digital |
| `visual_finish` | Acabamento |

---

## Dados de seed

| Migration | Conteúdo |
|---|---|
| V1 | Schema completo (marcas, veículos, versões, atributos, especificações, comparações, auditoria) |
| V2 | Ford Ranger XLT + Toyota Hilux SRX + Chevrolet S10 High Country + VW Amarok Highline (7 atributos cada) |
| V3 | +16 atributos canônicos + sinônimos + Ford Ranger Raptor 2024 com 23 specs reais |
| V4 | Tabela `usuarios` + `refresh_tokens` (JWT) |
| V5 | Tabela `auditoria` |
| V6 | +16 atributos ampliados para todas as versões de entrada (23 specs cada) |
| V7 | Reclassificação de atributos para categorias mais precisas (ex: painel digital) |

---

## Arquitetura

### Estrutura de pacotes

```
com.ford.specpulse
├── SpecPulseApplication.java
├── config/              SecurityConfig, OpenApiConfig
├── compartilhado/       RespostaLista, RespostaErro, RateLimitFilter,
│                        RequestIdFilter, ManipuladorGlobalExcecoes,
│                        SlugUtil, EntidadeBase
├── seguranca/           Perfil (enum dos 5 perfis)
├── autenticacao/        JWT, login, register, refresh, logout
│   ├── api/             AutenticacaoControlador + DTOs
│   └── dominio/         AutenticacaoServico, TokenServico, Usuario, RefreshToken
├── auditoria/           AuditoriaServico, Auditoria (entity)
├── facade/api/          Controladores /api + DTOs de resposta
├── ficha/               FichaTecnicaServico (resolução de fichas técnicas)
├── marca/               Marca
├── veiculo/             Veiculo, Segmento
├── versao/              Versao, Powertrain, Tracao, Cabine
├── especificacao/       AtributoDefinicao, Especificacao, Fonte, NivelConfianca
└── comparacao/          Comparacao (snapshot imutável), ComparacaoCelula
```

### Modelo de dados

```
marcas (1) ─── (n) veiculos (1) ─── (n) versoes
                                       │
                                       └── (n) especificacoes ─── (1) atributos_definicao
                                                              │                │
                                                              │         (n) atributo_sinonimos
                                                              └── (n..1) fontes

versoes ── participa de ──> comparacao_versoes ──> comparacoes ──> comparacao_celulas
                                                                   (snapshot dos valores)

usuarios ──> refresh_tokens
         └──> auditoria
```

### Segurança

- **JWT HS256** — access token (15 min) + refresh token rotacionado (7 dias, revogável)
- **RBAC** — 5 perfis: `SOMENTE_LEITURA < ANALISTA < GERENTE < VALIDADOR_DADOS < ADMINISTRADOR`
- **Rate limiting** — 60 req/min geral, 10 req/min em `/api/auth/*`
- **CORS** — origens configuráveis via `specpulse.cors.origens`
- **Auditoria assíncrona** — login, registro, comparações, alterações de usuário e acessos negados (403) gravados na tabela `auditoria`
- **Erros sem stack trace** — respostas de erro nunca expõem detalhes internos

O segredo JWT deve ser sobrescrito em produção via variável de ambiente:

```
SPECPULSE_JWT_SEGREDO=<segredo-com-no-minimo-32-caracteres>
```

### Envelopes de resposta

**Lista paginada:**

```json
{
  "data": [...],
  "page": 1,
  "pageSize": 25,
  "total": 42
}
```

**Erro:**

```json
{
  "timestamp": "2025-01-01T12:00:00Z",
  "status": 422,
  "code": "REGRA_NEGOCIO",
  "message": "Descrição do problema.",
  "path": "/api/comparacoes",
  "requestId": "abc-123",
  "details": []
}
```

---

## Exemplos curl

### Login e ficha técnica

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"gerente@ford.internal","senha":"gerente123"}' \
  | python -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")

curl -s -X POST http://localhost:8080/api/fichas-tecnicas/consultar \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "marca": "Ford",
    "modelo": "Ranger Raptor",
    "versao": "Raptor 3.0 V6 Biturbo Gasolina 4x4 Cabine Dupla",
    "atributos": ["potencia","torque","pneus","consumo urbano"]
  }'
```

### Sem token → 401

```bash
curl -s http://localhost:8080/api/marcas
# {"timestamp":"...","status":401,"code":"UNAUTHORIZED",...}
```

### Rate limit em /api/auth (> 10/min → 429)

```bash
for i in $(seq 1 12); do
  curl -s -o /dev/null -w "%{http_code} " -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"x","senha":"y"}'; done
# 422 422 422 ... 429 429
```
