# Installation Guide

## Prérequis

- **Java 17+** : [Download JDK](https://adoptium.net/)
- **Maven 3.8+** : [Download Maven](https://maven.apache.org/download.cgi)
- **Node.js 18+** : [Download Node.js](https://nodejs.org/)

## Build & Run

### Backend (Spring Boot + jPOS)

```bash
cd payment-simulator

# Build (sans tests pour aller plus vite)
mvn clean package -DskipTests

# Run
mvn spring-boot:run
# ou
java -jar target/payment-simulator-1.0.0-SNAPSHOT.jar
```

Le serveur démarre sur :
- **Port 8080** : API REST + H2 Console + Swagger
- **Port 5000** : TCP ISO 8583 (Host Simulator)

### Frontend (React + Vite)

```bash
cd payment-simulator-ui

# Installer les dépendances
npm install

# Lancer en mode dev
npm run dev
```

Le frontend est accessible sur **http://localhost:5173**.

Le proxy Vite redirige `/api/*` vers `http://localhost:8080`.

### H2 Console

- URL : http://localhost:8080/h2-console
- JDBC URL : `jdbc:h2:file:./data/payment-simulator-db`
- User : `sa`
- Password : *(vide)*

### Swagger / OpenAPI

- Swagger UI : http://localhost:8080/swagger-ui.html
- API Docs JSON : http://localhost:8080/api-docs

### Actuator

- Health : http://localhost:8080/actuator/health
- Info : http://localhost:8080/actuator/info
- Metrics : http://localhost:8080/actuator/metrics
