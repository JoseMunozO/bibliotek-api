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

Vid fel returneras JSON i formatet `{"status": 404, "message": "Member not found."}`
med statuskod 400 (ogiltig data), 404 (finns inte), 409 (konflikt, t.ex. e-post finns redan) eller 500 (databasfel).

### Konsolmenyn

```bash
./mvnw -q compile exec:java -Dexec.mainClass=se.josecarlos.bibliotek.Main
```

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
