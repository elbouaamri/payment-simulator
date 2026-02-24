# 💳 Payment Simulator ISO 8583

Simulateur monétique ISO 8583 complet avec jPOS, Spring Boot et React.

## 🏗 Architecture

```
payment-simulator/          ← Backend Spring Boot + jPOS
├── src/main/java/com/simulator/
│   ├── api/                ← REST controllers + DTOs
│   ├── config/             ← CORS, WebMvc config
│   ├── engine/             ← Rules Engine + TransactionService + VisaLikeService
│   ├── iso/                ← jPOS ISO 8583 (builder, parser, TCP server, constants)
│   ├── logging/            ← CorrelationIdFilter, PanMasking
│   └── persistence/        ← JPA entities + repositories
├── src/main/resources/
│   ├── application.yml     ← Configuration
│   ├── iso8583.xml         ← jPOS GenericPackager
│   └── data.sql            ← Seed data (profiles, rules, terminals)
└── src/test/               ← Unit + integration tests

payment-simulator-ui/       ← Frontend React + Vite
├── src/
│   ├── pages/              ← Dashboard, SendTransaction, TransactionsList, etc.
│   ├── api.js              ← API client
│   ├── App.jsx             ← Router + sidebar
│   └── index.css           ← Design system
```

## 🚀 Quick Start

### Prérequis
- **Java 17+** (JDK)
- **Maven 3.8+**
- **Node.js 18+** + npm

### Backend
```bash
cd payment-simulator
mvn clean install -DskipTests
mvn spring-boot:run
```
- API: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:file:./data/payment-simulator-db`)
- Swagger: http://localhost:8080/swagger-ui.html
- TCP ISO Server: port 5000

### Frontend
```bash
cd payment-simulator-ui
npm install
npm run dev
```
- UI: http://localhost:5173

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/transactions/authorize` | Authorization (0200/0100) |
| POST | `/api/v1/transactions/refund` | Refund (PC 200000) |
| POST | `/api/v1/transactions/cancel` | Cancellation/Void (PC 020000) |
| POST | `/api/v1/transactions/reversal` | Reversal (0400) |
| GET | `/api/v1/transactions/{id}` | Get transaction by ID |
| GET | `/api/v1/transactions?status=&terminalId=` | Filtered list |
| GET | `/api/v1/dashboard` | KPIs |
| GET/POST/PUT/DELETE | `/api/v1/profiles` | Profile CRUD |
| GET/POST/PUT/DELETE | `/api/v1/rules` | Rule CRUD |
| GET/POST/DELETE | `/api/v1/terminals` | Terminal CRUD |

## 🎯 Scénarios de simulation

| Scénario | Response Code (F39) | Condition |
|----------|-------------------|-----------|
| ACCEPT | 00 | Default |
| REFUSE | 05 | Terminal non reconnu |
| EXPIRED_CARD | 54 | Date expiration < aujourd'hui |
| INSUFFICIENT_FUNDS | 51 | Montant > limite profil |
| TIMEOUT | — | Pas de réponse envoyée |
| TECH_ERROR | 96 | Erreur système simulée |

## 🔧 Configuration (application.yml)

| Propriété | Défaut | Description |
|-----------|--------|-------------|
| `server.port` | 8080 | Port API REST |
| `simulator.tcp.port` | 5000 | Port TCP ISO 8583 |
| `simulator.tcp.timeout-ms` | 30000 | Timeout TCP |
| `spring.h2.console.enabled` | true | Console H2 |

## 🧪 Tests

```bash
cd payment-simulator
mvn test
```

Tests inclus :
- `RulesEngineServiceTest` – 7 scénarios
- `IsoMessageBuilderTest` – 5 tests pack/unpack
- `TransactionControllerIT` – 10 tests intégration REST
- `PanMaskingUtilTest` – masquage PAN

## 📋 Exemples curl

```bash
# Authorization
curl -X POST http://localhost:8080/api/v1/transactions/authorize \
  -H "Content-Type: application/json" \
  -d '{"pan":"4111111111111111","expiry":"2812","amount":10000,"currency":"504","terminalId":"TERM0001","profileId":1}'

# Reversal
curl -X POST http://localhost:8080/api/v1/transactions/reversal \
  -H "Content-Type: application/json" \
  -d '{"pan":"4111111111111111","expiry":"2812","amount":10000,"currency":"504","terminalId":"TERM0001"}'

# Dashboard
curl http://localhost:8080/api/v1/dashboard

# Profiles
curl http://localhost:8080/api/v1/profiles

# Rules
curl http://localhost:8080/api/v1/rules
```

## 📝 Licence

Projet académique (PFE) – Usage éducatif uniquement. Aucune cryptographie réelle n'est implémentée.
