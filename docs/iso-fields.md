# ISO 8583 Fields Reference

## Champs utilisés par le simulateur

| Field | Nom | Format | Longueur | Description |
|-------|-----|--------|----------|-------------|
| 0 | MTI | N | 4 | Message Type Indicator (0100, 0200, 0400...) |
| 2 | PAN | LLNUM | 19 | Primary Account Number |
| 3 | Processing Code | N | 6 | Type d'opération (000000=achat, 200000=remboursement, 020000=annulation) |
| 4 | Amount | N | 12 | Montant en unités mineures (centimes) |
| 5 | Amount Settlement | N | 12 | Montant de règlement en unités mineures |
| 6 | Amount Cardholder Billing | N | 12 | Montant facturation porteur en unités mineures |
| 7 | Transmission Date/Time | N | 10 | MMDDHHmmss |
| 11 | STAN | N | 6 | System Trace Audit Number |
| 12 | Local Time | N | 6 | HHmmss |
| 13 | Local Date | N | 4 | MMDD |
| 14 | Expiration Date | N | 4 | YYMM – date d'expiration carte |
| 18 | Merchant Type | N | 4 | Code type commerçant (MCC) – **obligatoire** |
| 22 | POS Entry Mode | N | 3 | Mode saisie (051 = chip) |
| 25 | POS Condition Code | N | 2 | Condition (00 = normal) |
| 32 | Acquiring Institution Code | LLNUM | 11 | Code institution acquéreur |
| 35 | Track 2 Data | LLNUM | 37 | Données piste 2 |
| 37 | RRN | AN | 12 | Retrieval Reference Number |
| 38 | Auth Code | AN | 6 | Code d'autorisation (en réponse) |
| 39 | Response Code | AN | 2 | Code réponse (00=accepté, 05=refusé, etc.) |
| 41 | Terminal ID | AN | 8 | Identifiant terminal |
| 42 | Merchant ID | AN | 15 | Identifiant commerçant |
| 43 | Card Acceptor Name | AN | 40 | Nom du commerçant |
| 49 | Currency Code | AN | 3 | Code devise (504=MAD, 840=USD, 978=EUR) |
| 52 | PIN Block | B | 16 | PIN chiffré (simulé) |
| 55 | EMV Data | LLLCHAR | 999 | Données EMV (simulées – fake TLV) |

## Message Types (MTI)

| MTI | Type | Description |
|-----|------|-------------|
| 0100 | Authorization Request (Visa) | Demande d'autorisation standard Visa |
| 0110 | Authorization Response (Visa) | Réponse à 0100 |
| 0200 | Financial Request | Demande financière (achat, remboursement, annulation) |
| 0210 | Financial Response | Réponse à 0200 |
| 0400 | Reversal Request | Demande d'annulation technique |
| 0410 | Reversal Response | Réponse à 0400 |

## Processing Codes

| Code | Signification |
|------|--------------|
| 000000 | Achat (Purchase) |
| 200000 | Remboursement (Refund) |
| 020000 | Annulation / Void (Cancel) |
| 310000 | Consultation de solde (Balance Inquiry) |

## Response Codes (Field 39)

| Code | Signification | Scénario |
|------|--------------|----------|
| 00 | Approuvé | ACCEPT |
| 05 | Ne pas honorer | REFUSE |
| 51 | Fonds insuffisants | INSUFFICIENT_FUNDS |
| 54 | Carte expirée | EXPIRED_CARD |
| 91 | Émetteur indisponible | TECH_ERROR |
| 96 | Dysfonctionnement système | TECH_ERROR |

## Notes sur la simulation

> ⚠️ **Aucune cryptographie réelle n'est implémentée.**

- Le champ 52 (PIN Block) est simulé avec des données factices
- Le champ 55 (EMV Data) contient des TLV simulés (tags 9F26, 9F27, 9F10)
- Le "cryptogramme" Visa-like est un hash SHA-256 tronqué – ce n'est PAS un vrai ARQC/TC
