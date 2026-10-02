# bibliotek-api

Ett bibliotekssystem i Java med:

- trelagersarkitektur
- JDBC
- DTOs och mappers
- konsolbaserade menyer
- REST API med Spring Boot (för React-frontend)
- MySQL

## Struktur

```text
Presentation -> Business -> Data -> Database
               DTO + Mapper
```

## Krav

- Java 25
- MySQL med schemat i [bibliotek.sql](bibliotek.sql)
- konfigurerade miljövariabler

## Miljövariabler

Använd [.env.example](.env.example) som referens:

```env
DB_URL=jdbc:mysql://localhost:3306/bibliotek
DB_USER=root
DB_PASSWORD=your_password_here
```

## Köra projektet

Projektet byggs med Maven via wrappern (`./mvnw`), ingen egen Maven-installation behövs.

Ladda miljövariablerna från `.env` (bash/zsh):

```bash
set -a; source .env; set +a
```

### REST API (Spring Boot)

```bash
./mvnw spring-boot:run
```

API:t startar på `http://localhost:8090`. Byt port med `SERVER_PORT=xxxx`.
CORS tillåter `http://localhost:5173` (Vite) som standard; ändra med `CORS_ALLOWED_ORIGINS`.

| Metod | Endpoint | Beskrivning |
|-------|----------|-------------|
| GET | `/api/books` | Alla böcker (`?search=`, `?available=true`, `?sort=id\|title\|author`) |
| GET | `/api/books/{id}` | Bokdetaljer (404 om boken inte finns) |
| GET | `/api/books/most-borrowed` | Mest utlånade böcker (`?limit=10`) |
| GET | `/api/members` | Alla medlemmar |
| GET | `/api/members/{id}` | Medlemsprofil med lån och böter |
| POST | `/api/members` | Registrera medlem (`firstName`, `lastName`, `email`) |
| PUT | `/api/members/{id}` | Uppdatera medlem (`firstName`, `lastName`, `email`, `membershipType`) |
| POST | `/api/members/{id}/suspend` | Stäng av medlem |
| GET | `/api/members/{id}/loans` | Alla lån för en medlem |
| GET | `/api/loans` | Aktiva lån |
| GET | `/api/loans/overdue` | Register över förfallna lån (med boktitel och medlem) |
| GET | `/api/loans/{id}` | Ett lån |
| POST | `/api/loans` | Låna en bok (`memberId`, `bookId`) |
| POST | `/api/loans/{id}/return` | Lämna tillbaka; svaret innehåller `fineAmount` om boken var försenad |
| POST | `/api/loans/{id}/extend` | Förläng lån (`extraDays`) |
| GET | `/api/members/{id}/fines` | Böter för en medlem (med boktitel) |
| POST | `/api/members/{id}/fines/{fineId}/pay` | Betala böter |
| GET | `/api/books/{id}/reviews` | Recensioner för en bok |
| POST | `/api/books/{id}/reviews` | Skapa recension (`memberId`, `rating` 1–5, `comment`) |
| GET | `/api/members/{id}/notifications` | Notifikationer för en medlem |
| POST | `/api/notifications` | Skicka notifikation (`memberId`, `loanId` valfritt, `type`, `message`) |
| POST | `/api/notifications/{id}/read` | Markera notifikation som läst |

Statusvärden lagras med små bokstäver, som i `bibliotek.sql`:
medlemmar `active`/`suspended`/`expired`, böter `pending`/`paid`,
notifikationstyper i snake_case (t.ex. `loan_reminder`, `pending_fine`).

Vid fel returneras JSON i formatet `{"status": 404, "message": "Member not found."}`
med statuskod 400 (ogiltig data), 404 (finns inte), 409 (konflikt, t.ex. e-post finns redan) eller 500 (databasfel).

### Konsolmenyn

```bash
./mvnw -q compile exec:java -Dexec.mainClass=se.josecarlos.bibliotek.Main
```

### Docker

```bash
docker build -t bibliotek-api .
docker run -p 8090:8090 --env-file .env bibliotek-api
```

Om MySQL körs lokalt, använd `host.docker.internal` i stället för `localhost` i `DB_URL`.

## Driftsättning (Railway)

API:t byggs med `Dockerfile` (Java 25) och lyssnar på porten i `PORT`, som Railway sätter automatiskt.

1. **New Project → Deploy from GitHub repo** → välj `bibliotek-api`. Railway hittar `Dockerfile` själv.
2. **+ New → Database → MySQL** i samma projekt (tjänsten heter `MySQL`).
3. Under API-tjänstens **Variables**:

   | Variabel | Värde |
   |----------|-------|
   | `DB_URL` | `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?allowPublicKeyRetrieval=true` |
   | `DB_USER` | `${{MySQL.MYSQLUSER}}` |
   | `DB_PASSWORD` | `${{MySQL.MYSQLPASSWORD}}` |
   | `CORS_ALLOWED_ORIGINS` | `https://<din-app>.vercel.app,https://bibliotek-react-ts*.vercel.app` |
   | `DEMO_INIT_IF_EMPTY` | `true` |
   | `DEMO_RESET_CRON` | `0 0 4 * * *` |
   | `DEMO_RESET_ZONE` | *(valfri, standard `Europe/Stockholm`)* |

   `${{MySQL.…}}` är Railways referensvariabler och fylls i automatiskt via det privata nätverket.
4. **Settings → Networking → Generate Domain**. API:t finns då på `https://<namn>.up.railway.app/api`
   (testa med `/api/books`). Frontendens `VITE_API_URL` ska vara exakt den adressen.

### Exempeldata och återställning

API:t saknar inloggning, så i en publik demo kan vem som helst ändra data. Därför:

- `DEMO_INIT_IF_EMPTY=true` laddar `bibliotek.sql` automatiskt vid start om databasen saknar tabeller
  (ingen manuell import behövs första gången).
- `DEMO_RESET_CRON` raderar alla tabeller och laddar `bibliotek.sql` på nytt enligt schemat
  (Spring-cron med 6 fält: sekund minut timme dag månad veckodag; `0 0 4 * * *` = varje natt kl. 04:00).
- Utan dessa variabler gör API:t ingenting med databasen – lokalt påverkas din data aldrig.

`CREATE DATABASE`/`USE` i skriptet hoppas över, så återställningen fungerar med vilket databasnamn som helst
(Railways heter `railway`).

## Nuvarande funktionalitet

- bokkatalog
- sökning och sortering av böcker
- utökade bokdetaljer
- registrering och hantering av medlemmar
- lån och återlämningar
- böter
- rollbaserade menyer i enkel version
- medlemsprofil
- förlängning av lån
- register över förfallna lån
- recensioner
- statistik över mest utlånade böcker
- notifikationer

## Notering om roller

Rollerna `User`, `Librarian` och `Admin` är simulerade i `Presentation`-lagret.

Det finns ingen riktig inloggning eller autentisering sparad i databasen.

## Förslag på demo

1. Visa tillgängliga böcker
2. Sortera böcker efter namn, författare och ID
3. Registrera en medlem
4. Visa medlemsprofil
5. Låna en bok
6. Lämna tillbaka en bok
7. Visa böter
8. Förläng ett lån
9. Visa register över förfallna lån
10. Skapa en recension
11. Visa statistik
12. Skicka en notifikation
