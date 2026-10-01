# bibliotek-api

Ett bibliotekssystem i Java med:

- trelagersarkitektur
- JDBC
- DTOs och mappers
- konsolbaserade menyer
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

Kompilera:

```bash
javac $(find src/main/java -name '*.java')
```

Kör:

```bash
java -cp src/main/java se.josecarlos.bibliotek.Main
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
