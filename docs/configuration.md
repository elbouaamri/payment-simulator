# Configuration YAML

Toute la configuration est dans `src/main/resources/application.yml`.

## Propriétés principales

```yaml
server:
  port: 8080                    # Port API REST

simulator:
  tcp:
    port: 5000                  # Port TCP ISO 8583
    timeout-ms: 30000           # Timeout TCP (ms)
  default-currency: "504"       # Code devise (MAD)
  packager: classpath:iso8583.xml  # Fichier packager jPOS

spring:
  datasource:
    url: jdbc:h2:file:./data/payment-simulator-db  # DB H2 en mode fichier
  h2:
    console:
      enabled: true             # Activer la console H2
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: update          # Auto-création des tables
    defer-datasource-initialization: true  # Charger data.sql après JPA
```

## Variables d'environnement

Vous pouvez surcharger les propriétés via des variables d'environnement :

```bash
# Changer le port API
SERVER_PORT=9090 mvn spring-boot:run

# Changer le port TCP
SIMULATOR_TCP_PORT=6000 mvn spring-boot:run
```

## Profiles Spring

Le projet utilise le profil par défaut. Pour ajouter un profil `prod` :

```yaml
# application-prod.yml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
```

Lancement : `mvn spring-boot:run -Dspring-boot.run.profiles=prod`
