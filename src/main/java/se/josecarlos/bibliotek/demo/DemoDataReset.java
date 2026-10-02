package se.josecarlos.bibliotek.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.josecarlos.bibliotek.data.DatabaseConnection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Restores the sample data from bibliotek.sql for the public online demo.
 * Disabled unless DEMO_RESET_CRON and/or DEMO_INIT_IF_EMPTY are set.
 */
@Component
public class DemoDataReset {

    private static final Logger log = LoggerFactory.getLogger(DemoDataReset.class);
    private static final String SCRIPT = "db/bibliotek.sql";

    private final boolean initIfEmpty;

    public DemoDataReset(@Value("${app.demo.init-if-empty}") boolean initIfEmpty) {
        this.initIfEmpty = initIfEmpty;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initIfEmpty() {
        if (initIfEmpty && listTables().isEmpty()) {
            log.info("Database is empty, loading sample data");
            reset();
        }
    }

    @Scheduled(cron = "${app.demo.reset-cron}", zone = "${app.demo.reset-zone}")
    public void scheduledReset() {
        log.info("Scheduled demo reset");
        reset();
    }

    /**
     * Drops every table in the database from DB_URL and runs bibliotek.sql again.
     * CREATE DATABASE / USE lines are skipped so it works with any database name.
     */
    public synchronized void reset() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            for (String table : listTables(conn)) {
                stmt.execute("DROP TABLE `" + table + "`");
            }
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

            ScriptUtils.executeSqlScript(conn, new ByteArrayResource(loadScript()));
            log.info("Demo data restored from {}", SCRIPT);

        } catch (SQLException | IOException e) {
            log.error("Demo reset failed", e);
        }
    }

    private byte[] loadScript() throws IOException {
        String script = new ClassPathResource(SCRIPT).getContentAsString(StandardCharsets.UTF_8);
        String withoutDatabaseSelection = script.replaceAll("(?im)^\\s*(CREATE DATABASE|USE)\\b.*$", "");
        return withoutDatabaseSelection.getBytes(StandardCharsets.UTF_8);
    }

    private List<String> listTables() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return listTables(conn);
        } catch (SQLException e) {
            log.error("Could not list tables", e);
            return List.of("unknown");
        }
    }

    private List<String> listTables(Connection conn) throws SQLException {
        List<String> tables = new ArrayList<>();
        String sql = "SELECT table_name FROM information_schema.tables "
                + "WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tables.add(rs.getString(1));
            }
        }

        return tables;
    }
}
