# API Guide

## Base URL

```
http://localhost:8080/api/v1
```

## Authentication

Aucune authentification requise (simulateur de test).

## Transaction Endpoints

### POST /transactions/authorize

Envoie une demande d'autorisation ISO 8583.

**Request Body:**
```json
{
  "pan": "4111111111111111",
  "expiry": "2812",
  "amount": 10000,
  "currency": "504",
  "terminalId": "TERM0001",
  "merchantId": "MERCH001",
  "stan": "",
  "rrn": "",
  "profileId": 1,
  "visaLike": false
}
```

| Champ | Type | Requis | Description |
|-------|------|--------|-------------|
| pan | string | ✅ | Numéro de carte (13-19 chiffres) |
| expiry | string | non | Date expiration YYMM |
| amount | long | ✅ | Montant en centimes |
| currency | string | non | Code devise (défaut: 504) |
| terminalId | string | ✅ | Identifiant terminal |
| merchantId | string | non | Identifiant commerçant |
| stan | string | non | Auto-généré si vide |
| rrn | string | non | Auto-généré si vide |
| profileId | long | non | ID profil simulation |
| visaLike | boolean | non | Utiliser MTI Visa (0100) |

**Response:**
```json
{
  "transactionId": "uuid",
  "type": "AUTHORIZE",
  "requestMti": "0200",
  "responseMti": "0210",
  "responseCode39": "00",
  "scenario": "ACCEPT",
  "scenarioDescription": "Transaction Approved",
  "panMasked": "411111******1111",
  "amount": 10000,
  "currency": "504",
  "stan": "123456",
  "rrn": "000000000001",
  "terminalId": "TERM0001",
  "visaLike": false,
  "fakeCryptogram": null,
  "requestIsoFields": { "MTI": "0200", "F2": "4111111111111111", ... },
  "responseIsoFields": { "MTI": "0210", "F39": "00", ... },
  "durationMs": 15,
  "createdAt": "2026-02-23T14:00:00",
  "correlationId": "uuid"
}
```

### POST /transactions/refund
Même format que /authorize, le type est automatiquement défini à `REFUND` (PC 200000).

### POST /transactions/cancel
Même format, type `CANCEL` (PC 020000 = void/annulation commerciale).

### POST /transactions/reversal
Même format, type `REVERSAL` (MTI 0400).

### GET /transactions/{id}
Retourne le `TransactionLog` complet par UUID.

### GET /transactions
Paramètres query optionnels :
- `from` : ISO DateTime (ex: `2026-01-01T00:00:00`)
- `to` : ISO DateTime
- `status` : Response code (ex: `00`, `51`)
- `terminalId` : Filtre par terminal

## Profile Endpoints

### GET /profiles
Liste tous les profils.

### POST /profiles
Crée un profil. Body : `{ name, description, defaultScenario, latencyMs, amountLimit, active }`

### PUT /profiles/{id}
Met à jour un profil existant.

### DELETE /profiles/{id}
Supprime un profil.

## Rule Endpoints

### GET /rules
Liste toutes les règles (triées par priorité).

### POST /rules
Crée une règle. Body : `{ name, priority, enabled, conditionType, conditionValue, outcomeScenario, responseCode39 }`

### PUT /rules/{id}
Met à jour une règle.

### DELETE /rules/{id}
Supprime une règle.

## Dashboard

### GET /dashboard
```json
{
  "totalTransactions": 42,
  "successCount": 35,
  "refusedCount": 5,
  "successRate": 83.3,
  "refusedRate": 11.9,
  "avgLatencyMs": 125.5
}
```

## Error Responses

```json
{
  "timestamp": "2026-02-23T14:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": { "pan": "PAN is required" }
}
```
