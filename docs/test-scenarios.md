# Test Scenarios & curl Examples

## 1. Authorization – Approved (ACCEPT)

```bash
curl -s -X POST http://localhost:8080/api/v1/transactions/authorize \
  -H "Content-Type: application/json" \
  -d '{
    "pan": "4111111111111111",
    "expiry": "2812",
    "amount": 10000,
    "currency": "504",
    "terminalId": "TERM0001",
    "profileId": 1,
    "visaLike": false
  }' | jq .
```
**Expected**: RC 39 = `00`, Scenario = `ACCEPT`, MTI 0200→0210

## 2. Authorization – Expired Card

```bash
curl -s -X POST http://localhost:8080/api/v1/transactions/authorize \
  -H "Content-Type: application/json" \
  -d '{
    "pan": "4111111111111111",
    "expiry": "2301",
    "amount": 10000,
    "currency": "504",
    "terminalId": "TERM0001"
  }' | jq .
```
**Expected**: RC 39 = `54`, Scenario = `EXPIRED_CARD`

## 3. Authorization – Insufficient Funds

```bash
curl -s -X POST http://localhost:8080/api/v1/transactions/authorize \
  -H "Content-Type: application/json" \
  -d '{
    "pan": "4111111111111111",
    "expiry": "2812",
    "amount": 99999999,
    "currency": "504",
    "terminalId": "TERM0001",
    "profileId": 2
  }' | jq .
```
**Expected**: RC 39 = `51`, Scenario = `INSUFFICIENT_FUNDS` (profile SLOW_RANDOM has limit 50000)

## 4. Authorization – Unknown Terminal (Refused)

```bash
curl -s -X POST http://localhost:8080/api/v1/transactions/authorize \
  -H "Content-Type: application/json" \
  -d '{
    "pan": "4111111111111111",
    "expiry": "2812",
    "amount": 10000,
    "currency": "504",
    "terminalId": "UNKNOWN_TERM"
  }' | jq .
```
**Expected**: RC 39 = `05`, Scenario = `REFUSE`

## 5. Authorization – Visa-like (MTI 0100)

```bash
curl -s -X POST http://localhost:8080/api/v1/transactions/authorize \
  -H "Content-Type: application/json" \
  -d '{
    "pan": "4111111111111111",
    "expiry": "2812",
    "amount": 10000,
    "currency": "504",
    "terminalId": "TERM0001",
    "visaLike": true
  }' | jq .
```
**Expected**: MTI 0100→0110, `fakeCryptogram` populated, EMV data in field 55

## 6. Refund

```bash
curl -s -X POST http://localhost:8080/api/v1/transactions/refund \
  -H "Content-Type: application/json" \
  -d '{
    "pan": "4111111111111111",
    "expiry": "2812",
    "amount": 5000,
    "currency": "504",
    "terminalId": "TERM0001"
  }' | jq .
```
**Expected**: Processing code `200000`, Type = `REFUND`

## 7. Cancellation / Void

```bash
curl -s -X POST http://localhost:8080/api/v1/transactions/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "pan": "4111111111111111",
    "expiry": "2812",
    "amount": 10000,
    "currency": "504",
    "terminalId": "TERM0001"
  }' | jq .
```
**Expected**: Processing code `020000`, Type = `CANCEL`

## 8. Reversal (MTI 0400)

```bash
curl -s -X POST http://localhost:8080/api/v1/transactions/reversal \
  -H "Content-Type: application/json" \
  -d '{
    "pan": "4111111111111111",
    "expiry": "2812",
    "amount": 10000,
    "currency": "504",
    "terminalId": "TERM0001"
  }' | jq .
```
**Expected**: MTI 0400→0410

## 9. Timeout Scenario

```bash
# Create a profile with TIMEOUT scenario
curl -s -X POST http://localhost:8080/api/v1/profiles \
  -H "Content-Type: application/json" \
  -d '{
    "name": "TIMEOUT_TEST",
    "description": "Forces timeout scenario",
    "defaultScenario": "TIMEOUT",
    "latencyMs": 0,
    "active": true
  }' | jq .

# Use the timeout profile (replace profileId with the returned ID)
curl -s -X POST http://localhost:8080/api/v1/transactions/authorize \
  -H "Content-Type: application/json" \
  -d '{
    "pan": "4111111111111111",
    "expiry": "2812",
    "amount": 10000,
    "currency": "504",
    "terminalId": "TERM0001",
    "profileId": 3
  }' | jq .
```
**Expected**: Scenario = `TIMEOUT`, no response MTI, no RC39

## 10. Tech Error Scenario

```bash
curl -s -X POST http://localhost:8080/api/v1/profiles \
  -H "Content-Type: application/json" \
  -d '{
    "name": "TECH_ERROR_TEST",
    "description": "Forces tech error",
    "defaultScenario": "TECH_ERROR",
    "latencyMs": 0,
    "active": true
  }' | jq .

# Use the tech error profile
curl -s -X POST http://localhost:8080/api/v1/transactions/authorize \
  -H "Content-Type: application/json" \
  -d '{
    "pan": "4111111111111111",
    "expiry": "2812",
    "amount": 10000,
    "currency": "504",
    "terminalId": "TERM0001",
    "profileId": 4
  }' | jq .
```
**Expected**: RC 39 = `96`, Scenario = `TECH_ERROR`

## 11. Dashboard KPIs

```bash
curl -s http://localhost:8080/api/v1/dashboard | jq .
```

## 12. Transaction History (filtered)

```bash
# All transactions
curl -s http://localhost:8080/api/v1/transactions | jq .

# Filter by status
curl -s "http://localhost:8080/api/v1/transactions?status=00" | jq .

# Filter by terminal
curl -s "http://localhost:8080/api/v1/transactions?terminalId=TERM0001" | jq .
```

## 13. Profiles CRUD

```bash
# List
curl -s http://localhost:8080/api/v1/profiles | jq .

# Create
curl -s -X POST http://localhost:8080/api/v1/profiles \
  -H "Content-Type: application/json" \
  -d '{"name":"CUSTOM","description":"Custom profile","defaultScenario":"ACCEPT","latencyMs":500,"amountLimit":100000,"active":true}' | jq .

# Update
curl -s -X PUT http://localhost:8080/api/v1/profiles/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"FAST_ACCEPT","description":"Updated","defaultScenario":"ACCEPT","latencyMs":100,"amountLimit":999999999,"active":true}' | jq .
```

## 14. Rules CRUD

```bash
# List
curl -s http://localhost:8080/api/v1/rules | jq .

# Create a new rule
curl -s -X POST http://localhost:8080/api/v1/rules \
  -H "Content-Type: application/json" \
  -d '{"name":"Block MasterCard","priority":5,"enabled":true,"conditionType":"PAN_PREFIX","conditionValue":"5","outcomeScenario":"REFUSE","responseCode39":"05"}' | jq .
```
