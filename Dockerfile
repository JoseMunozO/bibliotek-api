# --- Build ---
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline

COPY src src
# Exempeldata som paketeras i jar-filen (demo-återställning)
COPY bibliotek.sql .
RUN ./mvnw -q -B -DskipTests package

# --- Run ---
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/target/bibliotek-api-1.0-SNAPSHOT.jar app.jar

# Plattformen anger PORT; 8090 används annars
EXPOSE 8090
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
