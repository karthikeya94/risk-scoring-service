# Risk Scoring Service

<div align="center">

Real-time fraud risk assessment for high-volume transaction platforms

</div>

## Table of Contents

1. [Project Overview](#project-overview)
2. [Solution Architecture](#solution-architecture)
3. [Domain Concepts](#domain-concepts)
4. [Service Capabilities](#service-capabilities)
5. [Project Structure](#project-structure)
6. [Local Development](#local-development)
7. [Configuration](#configuration)
8. [Running with Containers](#running-with-containers)
9. [Event Streaming](#event-streaming)
10. [REST APIs](#rest-apis)
11. [Testing & Quality](#testing--quality)
12. [Observability & Operations](#observability--operations)
13. [Data & Persistence](#data--persistence)
14. [Makefile & Scripts](#makefile--scripts)
15. [Troubleshooting](#troubleshooting)
16. [Contributing](#contributing)
17. [Reference Documents](#reference-documents)

---

## Project Overview

The **Risk Scoring Service** is a Spring Boot microservice that evaluates the fraud risk of incoming financial transactions. It combines weighted rule-based scoring, anomaly detection, customer profiling, and streaming event processing to support automated decisioning in real time. The service enables:

- Consistent and explainable risk scores and decisions.
- Longitudinal customer risk profiles for trend analysis.
- Event emission for downstream fraud operations, compliance, and analytics platforms.
- Rapid experimentation with configurable rules and thresholds.

For a business-oriented summary of functionality, see [BUSINESS_FUNCTIONALITY.md](BUSINESS_FUNCTIONALITY.md).

---

## Solution Architecture

The service follows a layered architecture with clear separation of concerns, as detailed in [TECHNICAL_DOCUMENTATION.md](TECHNICAL_DOCUMENTATION.md):

- **Ingress**: Kafka consumer for validated transactions and REST controllers for on-demand scoring.
- **Domain Services**: Risk factor calculators, customer profile management, anomaly detection, and decision logic.
- **Persistence**: MongoDB repositories for profiles, assessments, anomalies, and event history.
- **Egress**: Kafka producer publishing assessments, profile updates, and high-risk alerts.
- **Cross-cutting**: Configuration, validation, logging, metrics, and service discovery via Netflix Eureka.

### Core Spring Modules

- Spring Boot 3.5.8
- Spring Web & Validation
- Spring Data MongoDB
- Spring Kafka
- Spring Cloud Netflix Eureka Client
- Springdoc OpenAPI for Swagger UI

### Runtime Characteristics

- Java 17 baseline with async processing enabled for non-blocking tasks.
- Kafka listener concurrency configurable (default: 3 consumers).
- Application port derived from the `PORT` environment variable (random fallback for local runs).

---

## Domain Concepts

- **RiskAssessment**: Aggregated outcome containing factor scores, total score, risk level, decision, and audit metadata.
- **RiskFactors**: Weighted components (Transaction, Behavior, Velocity, Geographic, Merchant) whose scores sum to 100.
- **CustomerRiskProfile**: Persisted customer snapshot capturing score history, velocity data, and profile metadata.
- **Anomalies**: Signals generated when pattern-based rules detect unusual behaviour.
- **EventStoreEntry**: Immutable event log capturing risk assessments for auditability.

Risk levels are derived from total score thresholds:

| Score | Risk Level | Decision |
|-------|------------|----------|
| 0 – 20 | LOW | APPROVE |
| 21 – 50 | MEDIUM | APPROVE (monitor) |
| 51 – 75 | HIGH | MANUAL_REVIEW |
| 76 – 100 | CRITICAL | REJECT/BLOCK |

---

## Service Capabilities

- Deterministic, configurable risk scoring algorithm.
- Customer risk profile maintenance with significance thresholds.
- Velocity, geospatial, and merchant risk analytics.
- Anomaly detection hooks for suspicious activity.
- Kafka-based event streaming for assessments and alerts.
- REST APIs for synchronous risk evaluation and insights.
- Actuator endpoints for health, metrics, and Prometheus scraping.

---

## Project Structure

```text
src/main/java/com/risk/scoring
├── RiskScoringServiceApplication.java   # Spring Boot entry point
├── config/                              # Configuration beans (Kafka, Mongo, etc.)
├── controller/                          # REST controllers
├── messaging/                           # Kafka consumer & producer services
├── model/                               # Domain entities, DTOs, enums
├── repository/                          # MongoDB repositories & implementations
└── service/                             # Domain services and implementations
    └── impl/

src/main/resources
├── application.yaml                     # Default configuration
└── ...
```

Extensive supporting documentation lives under `/docs` (API specs, architecture notes, configuration guides, etc.).

---

## Local Development

### Prerequisites

- Java 17 (Temurin or OpenJDK recommended)
- Maven ≥ 3.9
- Docker or Podman for optional container workflows
- Accessible MongoDB and Kafka instances

### Quick Start

```bash
git clone <repository-url>
cd risk-scoring-service

mvn clean verify
mvn spring-boot:run
```

The application binds to `:${PORT}` (random by default). Override with `SERVER_PORT` or `--server.port`.

### Executable JAR

```bash
mvn clean package -DskipTests
java -jar target/risk-scoring-service-0.0.1-SNAPSHOT.jar
```

Provide configuration overrides via `--spring.config.location` or environment variables.

---

## Configuration

Primary properties reside in [`src/main/resources/application.yaml`](src/main/resources/application.yaml). Override via environment variables, profile-specific YAML, or command-line properties.

| Property | Description | Default |
|----------|-------------|---------|
| `kafka.bootstrap-servers` | Kafka broker bootstrap servers | `localhost:9092` |
| `kafka.group-id` | Consumer group ID | `risk-scoring-group` |
| `kafka.topics.transaction-validated` | Inbound transaction topic | `transaction-validated` |
| `kafka.topics.risk-score-calculated` | Outbound assessment topic | `risk-score-calculated` |
| `kafka.topics.risk-profile-updated` | Customer profile updates | `risk-profile-updated` |
| `kafka.topics.risk-alert-high-score` | High-risk alert topic | `risk-alert-high-score` |
| `spring.data.mongodb.uri` | MongoDB connection URI | sample URI |
| `spring.data.mongodb.database` | Mongo database | `sample_mflix` |
| `risk.scoring.weights.*` | Risk factor weights | see YAML |
| `risk.scoring.profile.debounce-threshold` | Score delta required before persisting profiles | `5` |

Follow Spring Boot naming conventions for environment overrides (e.g., `KAFKA_BOOTSTRAP_SERVERS`, `RISK_SCORING_WEIGHTS_TRANSACTION`).

### Sensitive Configuration

- Replace the sample MongoDB URI with environment-specific credentials.
- Store secrets in `.env` (ignored by Git) or a secrets manager; Docker Compose consumes `.env` automatically.

---

## Running with Containers

### Docker Image

```bash
docker build -t risk-scoring-service .
docker run --rm -p 8080:8080 \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/riskdb \
  risk-scoring-service
```

### Docker Compose Stack

[`docker-compose.yml`](docker-compose.yml) provisions Kafka, Zookeeper, MongoDB, and the service.

```bash
docker compose up -d
docker compose logs -f app
docker compose down
```

Customize via `.env` or command-line overrides.

---

## Event Streaming

Kafka integration is central to service operations.

### Topics

- Consume: `transaction-validated` (validated transactions from upstream services).
- Produce: `risk-score-calculated`, `risk-profile-updated`, `risk-alert-high-score` for downstream consumers.

### Processing Flow

1. `KafkaConsumerService` receives events from `transaction-validated` and deserializes payloads.
2. Risk scoring executes; profiles, anomalies, and event store entries are updated as needed.
3. `KafkaProducerService` emits assessment, profile update, and alert events to configured topics.

Sample payloads are available in integration tests or can be generated via local runs.

---

## REST APIs

Swagger UI is exposed at `/swagger-ui.html`, with OpenAPI JSON at `/v3/api-docs`.

### Calculate Risk Score

- **POST** `/api/v1/risk/calculate`
- **Body**: `RiskCalculationRequest`

```jsonc
{
  "transactionId": "T-20250107-001",
  "customerId": "C-100045",
  "amount": 1250.75,
  "merchant": {
    "id": "M-0099",
    "category": "Wire_Transfer"
  },
  "location": {
    "country": "US",
    "latitude": 37.7749,
    "longitude": -122.4194
  },
  "timestamp": "2025-01-07T12:15:30Z"
}
```

Response excerpt:

```jsonc
{
  "riskAssessment": {
    "transactionId": "T-20250107-001",
    "riskScore": 62,
    "riskLevel": "HIGH",
    "decision": "MANUAL_REVIEW",
    "riskFactors": {
      "transactionRisk": 18,
      "behaviorRisk": 14,
      "velocityRisk": 12,
      "geographicRisk": 9,
      "merchantRisk": 9
    },
    "decisionDetails": [
      "High merchant category risk",
      "Velocity spike in the last hour"
    ]
  }
}
```

Additional endpoints:

- **GET** `/api/v1/risk/customer/{customerId}/score` – Retrieve latest customer profile snapshot.
- **GET** `/api/v1/risk/anomalies` – List detected anomalies with optional filters.
- **POST** `/api/v1/risk/rules` – Manage configurable risk rules (administrative access).

Refer to `/v3/api-docs` for the full OpenAPI contract.

---

## Testing & Quality

- Unit tests: `mvn test`
- Integration tests: `mvn verify -DskipUnitTests`
- Testcontainers: MongoDB integration tests (Docker required).
- Static analysis: Checkstyle and SpotBugs.

### Code Quality Commands

```bash
mvn checkstyle:check
mvn spotbugs:check
make code-quality
```

### Coverage

Jacoco instrumentation is enabled; run `mvn verify` and review `target/site/jacoco/index.html` for coverage insights.

---

## Observability & Operations

- Actuator endpoints: `/actuator/health`, `/actuator/info`, `/actuator/metrics`, `/actuator/prometheus`.
- Adjust exposed endpoints via `management.endpoints.web.exposure.include` in configuration.
- Logging levels configured under `logging.level.com.risk.scoring`; switch to `DEBUG` for detailed diagnostics.
- Eureka registration enabled for service discovery.

---

## Data & Persistence

MongoDB stores customer profiles, risk assessments, anomalies, and event history. Configure indexes on `customerId`, `transactionId`, and timestamp fields for optimal query performance. Initialization scripts (if present) reside in `/scripts` (e.g., `init-mongo.js`).

Velocity and customer profile fallbacks exist for environments without Redis; replace with production-grade integrations before launch.

---

## Makefile & Scripts

Common targets in the [`Makefile`](Makefile):

```bash
make build              # mvn clean package
make run                # mvn spring-boot:run
make test               # mvn test
make docker-build       # Build Docker image
make docker-compose-up  # Launch local stack (Kafka, Mongo, service)
make init-db            # Seed MongoDB with sample data
make clean              # Remove build artifacts
make help               # List target descriptions
```

Utility scripts in [`scripts/`](scripts/):

- `run-with-docker.sh`
- `run-integration-tests.sh`
- `run-code-quality.sh`
- `generate-api-docs.sh`

---

## Troubleshooting

| Symptom | Likely Cause | Suggested Action |
|---------|--------------|------------------|
| Kafka consumer not starting | Brokers unreachable | Verify `kafka.bootstrap-servers`, ensure connectivity. |
| MongoDB authentication failure | Invalid URI or credentials | Update `SPRING_DATA_MONGODB_URI`, confirm permissions. |
| Swagger UI 404 | Springdoc not scanning base package | Ensure `springdoc.packages-to-scan` matches `com.risk.scoring`. |
| Profiles not updating | Debounce threshold too high | Tune `risk.scoring.profile.debounce-threshold`. |
| High latency responses | Missing indexes or slow downstream dependencies | Add MongoDB indexes, review dependent services. |

Enable DEBUG logging or start with `--trace` for additional diagnostics.

---

## Contributing

1. Fork the repository and create a feature branch.
2. Run tests and static analysis before opening a PR.
3. Follow existing code style rules (Checkstyle enforced).
4. Provide context (logs, screenshots) for behavioural changes.

See [CHANGELOG.md](CHANGELOG.md) for release notes and follow semantic versioning conventions.

---

## Reference Documents

- [BUSINESS_FUNCTIONALITY.md](BUSINESS_FUNCTIONALITY.md)
- [TECHNICAL_DOCUMENTATION.md](TECHNICAL_DOCUMENTATION.md)
- [docs/api-docs.md](docs/api-docs.md)
- [docs/configuration.md](docs/configuration.md)
- [docs/messaging.md](docs/messaging.md)
- [docs/deployment.md](docs/deployment.md)
- [LICENSE](LICENSE)

---

For questions or support, contact the risk platform engineering team.
