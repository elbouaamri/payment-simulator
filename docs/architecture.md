# Architecture du système

## Vue d'ensemble

```
┌──────────────────────────────────────────────────────────┐
│                    React Frontend (5173)                   │
│  Dashboard │ Send TX │ Transactions │ Profiles & Rules    │
└─────────────────────────┬────────────────────────────────┘
                          │ HTTP / REST
┌─────────────────────────▼────────────────────────────────┐
│                Spring Boot API (8080)                      │
│  ┌──────────┐  ┌──────────────┐  ┌────────────────────┐  │
│  │  REST     │  │ Transaction  │  │   Rules Engine     │  │
│  │  Controllers│ │  Service     │  │   (priority-based) │  │
│  └──────────┘  └──────────────┘  └────────────────────┘  │
│  ┌──────────────────┐  ┌──────────────────────────────┐  │
│  │  ISO Builder/     │  │   Visa-Like Service          │  │
│  │  Parser (jPOS)    │  │   (MTI mapping + fake crypto)│  │
│  └──────────────────┘  └──────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐│
│  │              JPA Repositories → H2 Database          ││
│  └──────────────────────────────────────────────────────┘│
└──────────────────────────────────────────────────────────┘
                          │
              TCP ISO 8583 Server (5000)
              (accepts incoming ISO messages)
```

## Packages Java

| Package | Responsabilité |
|---------|----------------|
| `com.simulator.api.controller` | REST endpoints (Transaction, Profile, Rule, Dashboard, Terminal) |
| `com.simulator.api.dto` | Request/Response DTOs avec Bean Validation |
| `com.simulator.config` | Configuration Spring (CORS, Web) |
| `com.simulator.engine` | TransactionService, RulesEngineService, VisaLikeService, Scenario |
| `com.simulator.iso` | jPOS – IsoMessageBuilder, IsoMessageParser, TcpIsoServer, Constants |
| `com.simulator.logging` | CorrelationIdFilter, PanMaskingUtil |
| `com.simulator.persistence.entity` | Entités JPA (TransactionLog, SimulationProfile, SimulationRule, Terminal) |
| `com.simulator.persistence.repository` | Spring Data JPA repositories |

## Flux de traitement d'une transaction

1. L'API REST reçoit un `TransactionRequest`
2. Le `TransactionService` charge le profil de simulation
3. L'`IsoMessageBuilder` construit le message ISO 8583 (ISOMsg jPOS)
4. Si Visa-like, `VisaLikeService` ajoute les données EMV simulées
5. Le `RulesEngineService` évalue les règles par priorité
6. Simulation de la latence (configurée dans le profil)
7. Construction du message ISO de réponse avec le code 39
8. Persistance du `TransactionLog` en base H2
9. Retour du `TransactionResponse` à l'appelant

## Choix d'architecture

| Choix | Justification |
|-------|--------------|
| **Annulation via MTI 0200 + PC 020000** | Plus standard que 0400 pour une annulation commerciale. Le 0400 est réservé aux reversals techniques. |
| **Rules Engine en DB** | Permet la modification des règles sans redéploiement. |
| **jPOS GenericPackager** | Standard de l'industrie, extensible via XML. |
| **H2 file mode** | Persistance entre redémarrages, pas de serveur externe. |
| **CORS permissif** | Environnement de développement uniquement. |
| **Fake crypto** | Aucune cryptographie réelle – le projet est un simulateur de test. |
