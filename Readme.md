# PerifaFlow — Módulo Bem‑Estar & Insights (Java/Spring)

Serviço que registra **Ritmo** (opt‑in e anônimo), expõe **insights agregados** por bairro/turno e sugere **missões** adaptadas (curta/offline) quando o “ritmo” está baixo.

## ✨ Stack
- Java 17 • Spring Boot 3.5.x
- Web, Validation, Data JPA, Security, Actuator, Flyway, Lombok
- **H2 (dev)** e **Oracle (prod)**

## 🚀 Como rodar (dev)
```bash
./mvnw spring-boot:run
# health
curl http://localhost:8080/actuator/health
```
> Perfil ativo por padrão: `dev` (H2 em memória).

## 🔧 Perfis & Configuração
- `dev`: H2 em memória (`jdbc:h2:mem:bemestar;MODE=Oracle`), rotas abertas.
- `prod`: Oracle + validação JWT (resource server).

Variáveis para produção:
```
ORACLE_URL=jdbc:oracle:thin:@localhost:1521/XEPDB1
ORACLE_USER=SYSTEM
ORACLE_PASS=oracle
SPRING_PROFILES_ACTIVE=prod
```

## 🗃️ Migrações (Flyway)
- `V1__create_ritmo_event.sql`: cria a tabela `RITMO_EVENT` e índice por bairro/turno.
- `V2__seed_ritmo_event.sql` (opcional): insere registros de exemplo.

## 📦 Build (jar)
```bash
mvn -U clean package
java -jar target/bemestar-0.0.1-SNAPSHOT.jar
```

## 🔌 Endpoints (v1)
### 1) Registrar Ritmo (opt‑in)
`POST /v1/ritmo/registro`
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
**Curl**
```bash
curl -X POST http://localhost:8080/v1/ritmo/registro  -H "Content-Type: application/json"  -d '{"bairro":"Vila Nova","turno":"NOITE","energia":1,"ambiente":2,"condicao":1,"optIn":true}'
```

### 2) Insights agregados
`GET /v1/ritmo/insights?bairro={bairro}&de={yyyy-MM-dd}&ate={yyyy-MM-dd}`
- Janela **meia‑aberta** `[de, ate+1)`, compatível H2/Oracle.

**Curl**
```bash
curl "http://localhost:8080/v1/ritmo/insights?bairro=Vila%20Nova&de=2025-11-01&ate=2025-11-10"
```

**Resposta (exemplo)**
```json
{
  "bairro": "Vila Nova",
  "periodo": { "de": "2025-11-01", "ate": "2025-11-10" },
  "amostras": 3,
  "barreiras": { "barulho_alto": 0.66 }
}
```

### 3) Sugestão de Missão
`POST /v1/sugestoes/missao`
```json
{
  "perfil": "suporte",
  "ultimaEnergia": 0,
  "ultimoAmbiente": 1,
  "ultimaCondicao": 1
}
```
**Curl**
```bash
curl -X POST http://localhost:8080/v1/sugestoes/missao  -H "Content-Type: application/json"  -d '{"perfil":"suporte","ultimaEnergia":0,"ultimoAmbiente":1,"ultimaCondicao":1}'
```
**Resposta (exemplo)**
```json
{
  "missaoId": "SUG-suporte-CURTA",
  "complexidade": "CURTA",
  "offline": true,
  "mensagem": "Dia pesado? Vamos numa missão curtinha/offline pra manter o ritmo."
}
```

## 🛡️ Segurança
- `dev`: `permitAll` (sem JWT; facilita desenvolvimento).
- `prod`: **Resource Server** com JWT para autenticar requisições (ex.: emitido pelo gateway/.NET).

## ❗ Tratamento de erros
`GlobalExceptionHandler` retorna:
- **400** `VALIDATION_ERROR` (lista de `{field, message}`)
- **500** `INTERNAL_ERROR` (mensagem genérica)

## 🐳 Docker (dev, opcional)
`Dockerfile` básico para rodar o jar com perfil `dev`:
```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/bemestar-0.0.1-SNAPSHOT.jar app.jar
ENV SPRING_PROFILES_ACTIVE=dev
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
```
Build & run:
```bash
mvn -U -DskipTests package
docker build -t perifaflow-bemestar:dev .
docker run --rm -p 8080:8080 perifaflow-bemestar:dev
```

## ✅ Checklist de commits (sugestão)
- `feat(db): V1 create RITMO_EVENT + index`
- `feat(api): /ritmo/registro, /ritmo/insights, /sugestoes/missao`
- `feat(service): regras de negócio mínimas (registro/insights/sugestão)`
- `feat(security): permitAll no dev; JWT no prod`
- `feat(error): ProblemDetail handler (400/500)`
- `feat(db): V2 seed inicial`
- `docs(readme): instruções e curls`
