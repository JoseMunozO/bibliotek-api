# Projektsammanfattning per block

## Projektets mål

Detta projekt implementerar ett bibliotekssystem i Java med en trelagersarkitektur:

- `Presentation`
- `Business`
- `Data`

Projektet använder också:

- `DTO`
- `Mapper`
- `JDBC`
- `MySQL`

Huvudidén var att bygga ett projekt som är tydligt, funktionellt och lätt att förklara i klassrummet.

---

## Varför jag delade upp projektet i block

Att dela upp projektet i block hjälpte mig att arbeta mer strukturerat och att inte gå vidare för snabbt innan tidigare delar fungerade.

De viktigaste anledningarna var:

- Jag kunde bygga grunden först och sedan kärnan i systemet.
- Det blev lättare att hitta fel eftersom varje block hade ett tydligt mål.
- Det minskade risken att blanda för många delar samtidigt.
- Det gjorde det lättare att följa en naturlig utvecklingsordning: böcker först, sedan medlemmar och lån, sedan böter, sedan roller och till sist extras.
- Det gjorde det också enklare att mäta projektets framsteg.

Regeln jag följde var:

`inte gå vidare till nästa block förrän det föregående fungerade`

---

## Nuvarande status per block

### Block 1 - Books

Status: `Klart`

Innehåller:

- bokkatalog
- sökning
- sortering efter namn, författare och ID
- utökade bokdetaljer

Betydelse:

Detta är grunden i systemet, eftersom inga lån eller återlämningar kan fungera utan en bokkatalog.

---

### Block 2 - Members + Loans

Status: `Klart`

Innehåller:

- registrering av medlemmar
- medlemslista
- lån
- återlämningar
- aktiva lån
- lån per medlem
- förfallna lån

Betydelse:

Detta är kärnan i projektet eftersom det kopplar ihop böcker och medlemmar.

---

### Block 3 - Fines

Status: `Klart`

Innehåller:

- automatisk generering av böter vid sen återlämning
- visning av böter
- betalning av böter

Betydelse:

Detta gör systemet mer realistiskt eftersom det lägger till affärsregler för försenade återlämningar.

---

### Block 4 - Menyer per roll

Status: `Klart i enkel version`

Innehåller:

- `User`
- `Librarian`
- `Admin`

Viktig designbeslut:

Rollerna är simulerade i `Presentation`.

Det finns ingen riktig inloggning eller autentisering lagrad i databasen.

---

### Block 5 - Extras

Status: `Klart`

Implementerade extras:

- medlemsprofil
- uppdatering av medlemsinformation
- avstängning av medlem
- förlängning av lån
- utökade bokdetaljer
- utökat register över förfallna lån
- recensioner
- statistik över mest utlånade böcker
- notifikationer

Betydelse:

Detta block gör projektet mer professionellt och utnyttjar MySQL-schemat bättre.

---

### Block 6 - Städning + presentation

Status: `Klart`

Genomfört:

- städning av projektkonfiguration
- stöd för miljövariabler
- filen `.env.example`
- `README.md` med körinstruktioner och föreslagen demo
- uppdaterad sammanfattning för presentation
- utökad `.gitignore` för lokala artefakter och binärer

Rekommenderat kvar:

- slutliga manuella tester mot MySQL
- automatiska tester om tid finns

---

## Viktig kod och varför den används

### 1. Startpunkt för programmet

Fil: [Main.java](src/main/java/se/josecarlos/bibliotek/Main.java)

```java
public class Main {
    public static void main(String[] args) {
        LibraryMenu libraryMenu = new LibraryMenu();
        libraryMenu.showMainMenu();
    }
}
```

Funktion:

Startar programmet och lämnar över kontrollen till huvudmenyn.

---

### 2. Databasanslutning

Fil: [DatabaseConnection.java](src/main/java/se/josecarlos/bibliotek/data/DatabaseConnection.java)

```java
public class DatabaseConnection {
    private static final String URL = getRequiredEnv("DB_URL");
    private static final String USER = getRequiredEnv("DB_USER");
    private static final String PASSWORD = getRequiredEnv("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

Funktion:

Centraliserar JDBC-anslutningen och undviker hårdkodade databasuppgifter i koden.

---

### 3. Säker inmatning i menyer

Fil: [MenuInput.java](src/main/java/se/josecarlos/bibliotek/presentation/MenuInput.java)

```java
public static int readPositiveInt(Scanner scanner, String prompt) {
    while (true) {
        int value = readInt(scanner, prompt);

        if (value > 0) {
            return value;
        }

        System.out.println("Please enter a number greater than 0.");
    }
}
```

Funktion:

Förhindrar att applikationen kraschar på grund av ogiltig användarinmatning.

---

### 4. Lån med datakonsistens

Fil: [LoanService.java](src/main/java/se/josecarlos/bibliotek/business/LoanService.java)

```java
try (Connection conn = DatabaseConnection.getConnection()) {
    conn.setAutoCommit(false);

    boolean loanCreated = loanDAO.createLoan(conn, bookId, memberId, loanDate, dueDate);
    boolean stockUpdated = loanCreated && bookDAO.decreaseAvailableCopies(conn, bookId);

    if (loanCreated && stockUpdated) {
        conn.commit();
        return true;
    }

    conn.rollback();
    return false;
}
```

Funktion:

Säkerställer att lån och lageruppdatering sker tillsammans.

---

### 5. Återlämning med automatisk böter

Fil: [LoanService.java](src/main/java/se/josecarlos/bibliotek/business/LoanService.java)

```java
if (today.isAfter(loan.getDueDate()) && !fineDAO.hasFineForLoan(conn, loan.getId())) {
    long lateDays = ChronoUnit.DAYS.between(loan.getDueDate(), today);
    double fineAmount = lateDays * 2.0;
    boolean fineCreated = fineDAO.createFine(conn, loan.getId(), fineAmount);
```

Funktion:

Genererar böter automatiskt om en återlämning sker efter förfallodatum.

---

### 6. Medlemsprofil

Fil: [MemberDAO.java](src/main/java/se/josecarlos/bibliotek/data/MemberDAO.java)

Funktion:

Sammanfattar medlemsinformation, lån och böter i en enda fråga för att bygga en profilsida.

---

### 7. Utökade bokdetaljer

Fil: [BookDAO.java](src/main/java/se/josecarlos/bibliotek/data/BookDAO.java)

Funktion:

Kopplar ihop böcker, beskrivningar, författare och kategorier för att visa en rikare bokvy.

---

### 8. Recensioner, statistik och notifikationer

Filer:

- [ReviewService.java](src/main/java/se/josecarlos/bibliotek/business/ReviewService.java)
- [NotificationService.java](src/main/java/se/josecarlos/bibliotek/business/NotificationService.java)
- [BookService.java](src/main/java/se/josecarlos/bibliotek/business/BookService.java)

Funktion:

Utökar projektet med funktioner som ger tydligt mervärde och bygger vidare på `bibliotek.sql`.

---

## Övergripande flöde i projektet

Det viktigaste flödet som kan förklaras i demon är:

`Menu -> Service -> DAO -> Database`

Exempel:

1. Användaren väljer ett alternativ i menyn.
2. Menyn anropar ett `Service`.
3. `Service` validerar affärsregler.
4. `DAO` läser eller uppdaterar MySQL.
5. Systemet visar ett tydligt resultat i konsolen.

---

## Slutsatser för demon

Projektet har nu följande block klara:

- Block 1
- Block 2
- Block 3
- Block 4 i enkel version
- Block 5
- Block 6

Viktigt budskap att förklara:

`Systemet är funktionellt i sin kärna. Rolldelen har lösts enkelt i Presentation för att undvika onödig komplexitet i databasen. Extras i Block 5 bygger på verkliga tabeller i MySQL-schemat, vilket gör att systemet känns mer komplett utan att behöva implementera riktig inloggning.`
