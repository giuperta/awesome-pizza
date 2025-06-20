# 🍕 Awesome Pizza

Awesome Pizza è un sistema di gestione ordini per una pizzeria, progettato come esercizio tecnico per un colloquio. Consente agli utenti non registrati di ordinare le pizze e al pizzaiolo di gestire la coda di preparazione tramite API REST, senza interfaccia grafica.

## ✅ Obiettivi

- Ordinare pizze senza autenticazione.
- Gestione della coda da parte del pizzaiolo.
- Tracciamento dello stato dell'ordine.
- Progetto sviluppato con Java e Spring in ottica iterativa.
- Copertura di test unitari.

---

## 🛠️ Tecnologie usate

- Java 21
- Spring Boot 3.5.0
- Spring Web / Spring Validation / Spring Data JPA
- H2 Database
- Lombok
- Swagger (Springdoc OpenAPI)
- JUnit 5
- Maven

---

## 🚀 Avvio del progetto

1. **Clonare il repository**
   ```bash
   git clone https://github.com/<tuo-username>/awesome-pizza.git
   cd awesome-pizza
   ```

2. **Avviare l'applicazione**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Interfacce disponibili**
   - Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) [http://localhost:8080/v3/api-docs] (http://localhost:8080/v3/api-docs)
   - H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
     - JDBC URL: `jdbc:h2:mem:pizza-db`
     - Username: `sa`, Password: *(vuota)*

---

## 📦 API disponibili

### 🍕 Creazione di un ordine
```http
POST /orders
```

### 🔍 Stato ordine
```http
GET /orders/{id}
```

### 📋 Menu disponibile
```http
GET /orders/menu
```

### 👨‍🍳 Pizzaiolo - Visualizza coda ordini
```http
GET /kitchen/orders
```

### 🔄 Pizzaiolo - Passa all'ordine successivo
```http
POST /kitchen/orders/next
```

### ✅ Pizzaiolo - Completa ordine
```http
POST /kitchen/orders/{id}/complete
```

---

## 🧪 Test

Esegui i test con:

```bash
./mvnw test
```

## 🐳 Esecuzione con Docker
Per costruire ed eseguire il progetto in un container Docker:

## 🔨 Build dell'immagine
```bash
docker build -t awesome-pizza .
```

## 🚀 Avvio del container
```bash
docker run -p 8080:8080 awesome-pizza
```

L'applicazione sarà accessibile su http://localhost:8080
