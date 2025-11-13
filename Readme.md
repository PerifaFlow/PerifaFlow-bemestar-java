# PerifaFlow — Módulo Bem‑Estar & Insights (Java/Spring)

Serviço backend responsável por registrar o **Ritmo** dos participantes (opt‑in e anônimo), gerar **insights agregados** por bairro/período e sugerir **missões adaptadas** com IA generativa quando o “ritmo” está baixo.  
É o módulo Java da plataforma **PerifaFlow**, integrado à Global Solution FIAP 2025.

---

## ✨ Visão Geral

- **Contexto da GS**: bem‑estar, energia sustentável e inclusão produtiva na periferia.
- **Domínio**: Ritmo (energia, ambiente, condição) + Insights agregados por bairro.
- **Objetivo**:
    - Coletar sinais de bem‑estar de forma opcional (opt‑in, sem dados pessoais).
    - Geração de métricas agregadas (ex.: % de barulho alto por bairro/período).
    - Sugerir **missões de portfólio** (CURTA / NORMAL, offline/online) com IA generativa.
- **Fronts consumidores**:
    - App / web PerifaFlow (.NET + Mobile) chamando os endpoints `/v1/...`.
    - Painéis de insights para ONGs/empresas (via API REST).

---

## 🧱 Stack Técnica

- **Linguagem**: Java 17
- **Framework**: Spring Boot 3.3.x
- **Módulos Spring**:
    - `spring-boot-starter-web` (API REST)
    - `spring-boot-starter-data-jpa` (persistência)
    - `spring-boot-starter-validation` (Bean Validation)
    - `spring-boot-starter-security` (Security)
    - `spring-boot-starter-actuator` (health, info)
    - `spring-boot-starter-cache` + **Caffeine** (caching)
    - `spring-boot-starter-amqp` (RabbitMQ)
- **Banco de dados**:
    - **H2 em memória** (dev & testes)
    - **Oracle** (produção)
- **Migração de schema**: Flyway
- **IA Generativa**: Spring AI + Ollama (modelo `qwen3:4b`)
- **Mensageria**: RabbitMQ (publicação de eventos de Ritmo)
- **Documentação**: springdoc-openapi (`/swagger-ui.html`)
- **Teste**:
    - `@WebMvcTest` para controller
    - `@JdbcTest` para agregações de insights
    - `@DataJpaTest` para serviço/JPA
    - `SpringBootTest` carregando o contexto
- **Outros**:
    - Lombok (boilerplate)
    - git-commit-id-plugin (metadata de versão)

---

## 🔧 Perfis & Configuração

### Perfis ativos

- **`dev` (padrão)**
    - Banco H2 em memória (`jdbc:h2:mem:bemestar;MODE=Oracle;DB_CLOSE_DELAY=-1`)
    - Console H2 em `/h2-console`
    - Segurança relaxada (`permitAll`), ideal para desenvolvimento.
    - Mensageria **desligada** por padrão (`messaging.enabled=false`).

- **`prod`**
    - Banco Oracle via variáveis de ambiente.
    - Segurança configurada como **Resource Server (JWT)**.
    - Mensageria configurável via env (`MESSAGING_ENABLED=true/false`).

### `application.yml` (resumo)

- Mapeia:
    - Datasource H2 / Oracle
    - Flyway (`classpath:db/migration`)
    - Cache Caffeine (`maximumSize=1000,expireAfterWrite=5m`)
    - Spring AI (base URL do Ollama e modelo)
    - RabbitMQ (exchange, queue, routing key)
    - Flag `messaging.enabled`
- Para testes (`application-test.yml`):
    - H2 em memória + Flyway
    - Mensageria **sempre desabilitada**

### Variáveis de ambiente (produção)

```bash
# Banco Oracle
ORACLE_URL=jdbc:oracle:thin:@localhost:1521/XEPDB1
ORACLE_USER=SYSTEM
ORACLE_PASS=oracle

# Perfil
SPRING_PROFILES_ACTIVE=prod

# RabbitMQ (opcional)
RABBIT_HOST=...
RABBIT_PORT=5672
RABBIT_USER=...
RABBIT_PASS=...
MESSAGING_ENABLED=true

# AI (se usar Spring AI em prod)
SPRING_AI_OLLAMA_BASE_URL=http://ollama:11434/
```

---

## 🗃️ Migrações (Flyway)

Localizadas em `src/main/resources/db/migration`:

1. `V1__create_ritmo_event.sql`
    - Cria tabela `RITMO_EVENT` com colunas:
        - `ID`, `BAIRRO`, `TURNO`, `ENERGIA`, `AMBIENTE`, `CONDICAO`, `ENVIADO_EM`
    - Índice `IDX_RITMO_BAIRRO_TURNO` (bairro + turno).

2. `V2__seed_ritmo_event.sql`
    - Insere alguns registros de exemplo (diferentes bairros/turnos) para testes rápidos.

3. `V3__ajustes_turno_enum.sql`
    - Índice adicional por `ENVIADO_EM` e constraint de domínio para `TURNO` (`MANHA|TARDE|NOITE`).

> Observação: a entidade JPA `RitmoEvent` espelha essa estrutura e usa `@Enumerated(EnumType.STRING)` para o enum `Turno`.

---

## 🔌 Endpoints (v1)

Todos expostos no path base `/v1`.  
Documentação interativa em: **`/swagger-ui.html`** (dev e prod).

### 1) Registrar Ritmo (opt‑in)

`POST /v1/ritmo/registro`

Request body (`RitmoRegistroDTO`):

```json
{
  "bairro": "Vila Nova",
  "turno": "NOITE",
  "energia": 1,
  "ambiente": 2,
  "condicao": 1,
  "optIn": true
}
```

Regras:

- Se `optIn=false`, o registro **não é salvo** nem enviado para mensageria.
- `bairro` obrigatório, até 120 caracteres.
- `turno` deve ser `MANHA`, `TARDE` ou `NOITE` (validação + enum).
- `energia`, `ambiente`, `condicao` ∈ {0, 1, 2}.

**Exemplo cURL:**

```bash
curl -X POST http://localhost:8080/v1/ritmo/registro   -H "Content-Type: application/json"   -d '{"bairro":"Vila Nova","turno":"NOITE","energia":1,"ambiente":2,"condicao":1,"optIn":true}'
```

**Resposta:**

- `202 Accepted` em caso de sucesso.
- `400 Bad Request` se violar validação (corpo ProblemDetail).

---

### 2) Lista paginada de Registros

`GET /v1/ritmo/registros`

Parâmetros:

- `bairro` *(opcional)* — filtro parcial (`LIKE %bairro%`, ignorando maiúsculas/minúsculas)
- `turno` *(opcional)* — `MANHA|TARDE|NOITE`
- Parâmetros de paginação Spring (`page`, `size`, `sort`), com defaults:
    - `size = 10`
    - `sort = enviadoEm, DESC`

**Exemplo:**

```bash
curl "http://localhost:8080/v1/ritmo/registros?bairro=Vila&turno=NOITE&page=0&size=5"
```

Resposta: `Page<RitmoEvent>` com metadados de paginação (`content`, `totalElements`, `totalPages`, etc.).

---

### 3) Insights agregados por bairro/período

`GET /v1/ritmo/insights?bairro={bairro}&de={yyyy-MM-dd}&ate={yyyy-MM-dd}`

- Janela **meia‑aberta** `[de, ate+1)`, garantindo contagem correta da data final.
- Calcula:
    - `amostras`: total de registros no período.
    - `barreiras.barulho_alto`: proporção de eventos com `AMBIENTE=2`.

**Exemplo:**

```bash
curl "http://localhost:8080/v1/ritmo/insights?bairro=Vila%20Nova&de=2025-01-01&ate=2025-12-31"
```

**Resposta (modelo `InsightsDTO`):**

```json
{
  "bairro": "Vila Nova",
  "periodo": { "de": "2025-01-01", "ate": "2025-12-31" },
  "amostras": 3,
  "barreiras": { "barulho_alto": 0.66 }
}
```

Caching:

- Resultado é armazenado em cache (`@Cacheable("insights")`) com chave `"bairro|de|ate"`.
- Cache Caffeine com TTL configurado em `application.yml`.

---

### 4) Sugestão de Missão com IA

`POST /v1/sugestoes/missao`

Request body (`SugestaoMissaoRequest`):

```json
{
  "perfil": "suporte",
  "ultimaEnergia": 0,
  "ultimoAmbiente": 1,
  "ultimaCondicao": 1
}
```

Fluxo:

1. Monta um prompt em pt‑BR com regras claras:
    - Se soma dos sinais (`energia + ambiente + condicao`) ≤ 2 → missão `CURTA`, `offline = true`.
    - Caso contrário → missão `NORMAL`, `offline = false`.
2. Envia prompt para Spring AI + Ollama (`qwen3:4b`), exigindo resposta **apenas em JSON**.
3. Faz parsing seguro do JSON com `ObjectMapper`.
4. Se algo der errado (erro de rede, parsing, etc.), cai no **fallback** local com as mesmas regras.

**Resposta (`SugestaoMissaoResponse`):**

```json
{
  "missaoId": "SUG-suporte-CURTA",
  "complexidade": "CURTA",
  "offline": true,
  "mensagem": "Dia pesado? Vamos numa missão curtinha/offline pra manter o ritmo."
}
```

---

## 🌐 Internacionalização (i18n)

Configurações em `I18nConfig` + arquivos de mensagens:

- `messages_pt_BR.properties`
- `messages_en.properties`

Chaves importantes:

- `validation.turno`
- `validation.bairro.notblank`
- `validation.range02`
- `error.validation`
- `error.invalid_body`
- `error.internal`

A língua é resolvida via:

- Header `Accept-Language` (ex.: `pt-BR`, `en`)
- Default: `pt-BR`

`OpenApiConfig` adiciona automaticamente o header no Swagger para facilitar testes.

---

## ❗ Tratamento Global de Erros

`GlobalExceptionHandler` (anotado com `@RestControllerAdvice`) padroniza as respostas de erro em formato **ProblemDetail**:

- `400 BAD_REQUEST` — `MethodArgumentNotValidException`
    - `title`: `VALIDATION_ERROR`
    - `details`: lista de `{ field, message }`
- `400 BAD_REQUEST` — `HttpMessageNotReadableException` (body inválido)
- `400 BAD_REQUEST` — `DateTimeParseException` (datas inválidas)
- `400 BAD_REQUEST` — `IllegalArgumentException` (erros de regra simples)
- `500 INTERNAL_SERVER_ERROR` — fallback genérico
    - `title`: `INTERNAL_ERROR`
    - `detail`: mensagem amigável em pt‑BR/en.

---

## 🛡️ Segurança (Spring Security)

### Perfil `dev`

`SecurityConfig.devChain`:

- `csrf` desabilitado.
- Libera:
    - `/h2-console/**`
    - `/actuator/**`
    - `/v3/api-docs/**`
    - `/swagger-ui/**`
    - `/swagger-ui.html`
- Demais rotas: `permitAll`.

### Perfil `prod`

`SecurityConfig.prodChain`:

- `csrf` desabilitado.
- `/actuator/**` liberado (para health monitorado).
- Demais rotas: `authenticated()` via **JWT** (`oauth2ResourceServer().jwt()`).
- JWT será emitido/validado pelo gateway/.NET do PerifaFlow.

---

## 📬 Mensageria (RabbitMQ)

A interface `RitmoPublisher` possui duas implementações:

- **`NoopRitmoPublisher`** (default, `messaging.enabled=false`)
    - Apenas loga que a mensageria está desabilitada (evita erros em dev/test).

- **`RabbitRitmoPublisher`** (`messaging.enabled=true`)
    - Converte `RitmoEvent` em `RitmoEventMessage` e publica em:
        - `TopicExchange` configurado (`app.mq.ritmo.exchange`)
        - `routingKey` configurado (`app.mq.ritmo.routingKey`)

`RitmoListener`:

- `@RabbitListener(queues = "${app.mq.ritmo.queue}")`
- Recebe `RitmoEventMessage` e loga informações (ponto de extensão para futuros processamentos).

---

## 🧪 Testes Automatizados

Localizados em `src/test/java/com/perifaflow/bemestar`:

- **`BemEstarControllerTest`**
    - `@WebMvcTest(BemEstarController.class)`
    - Usa `MockMvc` para validar:
        - `POST /v1/ritmo/registro` (202 e 400 com `VALIDATION_ERROR`)
        - `GET /v1/ritmo/registros` (200 OK com mocks de serviço)
        - `POST /v1/sugestoes/missao` (200 OK + campos retornados)

- **`InsightsServiceTest`**
    - `@JdbcTest` + `@Import(InsightsService.class)`
    - Monta tabela `RITMO_EVENT` via SQL puro no `@BeforeEach`.
    - Garante que `barulho_alto` é calculado como `2/3` ≈ `0.66`.

- **`RitmoServiceTest`**
    - `@DataJpaTest` + `@Import(RitmoService.class)`
    - Usa H2 + Spring Data JPA para testar:
        - Registro com `optIn=true` persiste dados corretamente.
        - `optIn=false` não persiste nada.
        - Filtros de listagem por bairro & turno.
    - `RitmoPublisher` é `@MockBean` para não depender de RabbitMQ real.

- **`SugestoesServiceTest`**
    - Testa especificamente o **fallback** local da IA (sem chamar Ollama).
    - Garante:
        - Soma ≤ 2 → `CURTA`, `offline=true`.
        - Soma > 2 → `NORMAL`, `offline=false`.

- **`BemestarApplicationTests`**
    - `@SpringBootTest` para validar se o contexto Spring sobe com sucesso.

---

## 🐳 Docker (Build da Imagem)

`Dockerfile` multi‑stage (utilizado no Render):

```dockerfile
# 1) Stage de build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY ../../donwload/src ./src

# Gera o jar (sem rodar testes para agilizar)
RUN mvn -U -DskipTests package

# 2) Stage final: JRE + jar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/bemestar-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=dev
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
```

Build local:

```bash
mvn -U clean package
docker build -t perifaflow-bemestar:dev .
docker run --rm -p 8080:8080 perifaflow-bemestar:dev
```

---

## ☁️ Deploy em Nuvem (Render)

Aplicação deployada como **Web Service (Docker)** no Render:

- Build:
    - Render executa o `Dockerfile` na raiz do projeto (multi‑stage).
- Execução:
    - Container expõe a porta `8080` (mapeada para HTTPS público pelo Render).
    - Perfil padrão `dev`, com H2 em memória (independente de banco externo).

URL :

```text
https://perifaflow-bemestar-java.onrender.com
```

- `GET /` → Landing page estática (`index.html`) com resumo do serviço e botões para:
    - Swagger (`/swagger-ui.html`)
    - Health (`/actuator/health`)

---

## 🧪 Como Rodar Localmente (dev)

Requisitos:

- Java 17+
- Maven 3.9+
- (Opcional) RabbitMQ local / Docker se quiser testar mensageria real
- (Opcional) Ollama rodando localmente com o modelo `qwen3:4b`

Passos:

```bash
# 1. Clonar repositório
git clone https://github.com/PerifaFlow/PerifaFlow-bemestar-java.git
cd PerifaFlow-bemestar-java

# 2. Subir em perfil dev (H2, sem autenticação)
./mvnw spring-boot:run

# 3. Testar health
curl http://localhost:8080/actuator/health

# 4. Abrir Swagger no navegador
http://localhost:8080/swagger-ui.html
```

Para rodar **os testes**:

```bash
./mvnw test
```

---

## ✅ Checklist de Requisitos (Java Advanced FIAP)

- [x] **Anotações Spring** para beans, injeção de dependências e configuração.
- [x] **Camada model/DTO** com uso correto de acesso e validações.
- [x] **Persistência com Spring Data JPA** (entidade `RitmoEvent` + `RitmoEventRepo`).
- [x] **Validação com Bean Validation** (`RitmoRegistroDTO` com constraints).
- [x] **Caching** (`InsightsService` com Caffeine).
- [x] **Internacionalização** com suporte a pt‑BR e en (`I18nConfig` + messages).
- [x] **Paginação** em `/v1/ritmo/registros` (Spring Pageable).
- [x] **Spring Security** com perfis `dev` (aberto) e `prod` (JWT).
- [x] **Tratamento adequado de erros/exceptions** (`GlobalExceptionHandler` + ProblemDetail).
- [x] **Mensageria** com filas assíncronas (RabbitMQ + publishers/listener).
- [x] **Recurso de IA Generativa** (Spring AI + Ollama para `/v1/sugestoes/missao`).
- [x] **Deploy em nuvem** (Render, via Dockerfile).
- [x] **API REST** utilizando verbos HTTP/códigos de status adequados.

---

## 👥 Sobre o Módulo no PerifaFlow

Este módulo Java integra o ecossistema PerifaFlow:

- Alimenta o backend principal (.NET) com **Ritmo Score** e insights por bairro.
- Gera dados agregados que podem ser exportados/relacionados com o modelo relacional (Oracle) da disciplina de Banco de Dados.
- Serve de base para painéis de monitoramento de bem‑estar e para personalização das **missões/ trilhas** via IA.

> Qualquer contribuição ou ajuste pode ser registrado via issues e pull requests no repositório GitHub do módulo.
