# PDS – Booking Microservices (Docker-first)

Mala mikroservisna aplikacija za **rezervacije** koja demonstrira: razdvajanje na servise, **service discovery (Eureka)**, **API Gateway (Spring Cloud Gateway)**, **komunikaciju između servisa (OpenFeign)** i **otpornost (Resilience4j)**. Build i pokretanje su u potpunosti preko **Docker Compose**.

---

## 1) Pokretanje (Docker Compose, redosled podizanja)

> *depends_on + healthcheck* u `compose.yml` već obezbeđuju ispravan redosled (Eureka → Users → Bookings → Gateway).

```bash
# iz root foldera (gde je compose.yml)
docker compose up --build -d

# logovi po servisu (primer)
docker compose logs -f api-gateway

# gašenje i čišćenje volumena
docker compose down -v
```

### Provere (health i registracija)
- **Eureka dashboard:**  <http://localhost:8761>  
  (očekuje se da su registrovani: `users-service`, `bookings-service`, `api-gateway`)
- **Gateway health:** `curl -s http://localhost:8085/actuator/health`
- **Users health:**   `curl -s http://localhost:8081/actuator/health`
- **Bookings health:** `curl -s http://localhost:8082/actuator/health`

> **API ključ (gateway):** dodati header **`X-API-Key: sifra123`** na sve pozive preko gateway-a.

---

## 2) Kratak pregled arhitekture

```
[ client ] → [ api-gateway ] → [ users-service ] → [ H2 ]
                          ↘→  [ bookings-service ] → [ H2 ]
                [ discovery-service (Eureka) ]
```

- `bookings-service` koristi **Feign** ka `users-service` (npr. validacija `userId`).
- Otpornost: **Resilience4j** (Retry/Circuit Breaker) primenjen na Feign poziv.
- Persistencija: **H2 in-memory** (users/bookings) za lokalni rad i demonstraciju.

---

## 3) Endpoints (preko Gateway-a) – primeri poziva

> Svi primeri koriste header `X-API-Key: sifra123`.

**Users**
```bash
curl -X POST http://localhost:8085/api/users   -H "Content-Type: application/json" -H "X-API-Key: sifra123"   -d '{"name":"Ana","email":"ana@example.com"}'

curl http://localhost:8085/api/users -H "X-API-Key: sifra123"
```

**Bookings**
```bash
curl -X POST http://localhost:8085/api/bookings   -H "Content-Type: application/json" -H "X-API-Key: sifra123"   -d '{"userId":1,"startTime":"2026-01-01T10:00:00","endTime":"2026-01-01T11:00:00"}'
```

**H2 konzole**
- users-service: <http://localhost:8081/h2-console> (JDBC: `jdbc:h2:mem:usersdb`, user `sa`, pass prazno)
- bookings-service: <http://localhost:8082/h2-console> (JDBC: `jdbc:h2:mem:bookingsdb`, user `sa`, pass prazno)

---

# DOKUMENTACIJA

## Dijagram komponenti


## Tabela servisa (naziv, port, rute, odgovornosti)
| Naziv | Port (host) | Rute (preko gateway-a) | Odgovornosti |
|---|---:|---|---|
| discovery-service | 8761 | — | **Eureka** registry – registracija i lookup servisa |
| api-gateway | 8085 | `/api/users/**`, `/api/bookings/**` | Centralna ulazna tačka; rutiranje; **API key** filter |
| users-service | 8081 | (kroz gateway) `/api/users/**` | CRUD korisnika; **H2** `usersdb` |
| bookings-service | 8082 | (kroz gateway) `/api/bookings/**` | CRUD rezervacija; **Feign** ka users; **Resilience4j** |

## Šta je urađeno (obavezno) + Bonus delovi
**Obavezno (ispunjeno):**
- **Service Discovery (Eureka)** – centralni registry
- **API Gateway (Spring Cloud Gateway)** – rute ka users/bookings
- **Dva mikroservisa** – `users-service` (CRUD + H2), `bookings-service` (CRUD + Feign + H2)
- **Komunikacija servis–servis – OpenFeign** (bookings → users)
- **Otpornost – Resilience4j (Retry + Circuit Breaker)** na Feign pozivu
- **Persistencija – H2 (in-memory)** za oba servisa
- **Agregacioni endpoint** – *u sklopu bookings-service* (GET detalja rezervacije sa korisnikom)

**Bonus (u ovom projektu):**
- **Docker Compose** – build/run celog okruženja
- **Jednostavna autentikacija na gateway-u (API Key)** – header `X-API-Key`

## Tehnologije
- Java (Temurin 21 JRE u slikama), **Spring Boot 3**, Spring Web, Spring Data JPA  
- **Spring Cloud**: Eureka (discovery), Gateway (routing)  
- **OpenFeign** (servis–servis), **Resilience4j** (CB/Retry)  
- **H2** (users/bookings), **Maven**, **Docker/Compose**

---