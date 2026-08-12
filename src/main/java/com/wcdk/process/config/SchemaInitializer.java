package com.wcdk.process.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SchemaInitializer implements CommandLineRunner {

    private final SchemaConfig schemaConfig;
    private final org.springframework.r2dbc.core.DatabaseClient databaseClient;

    @Override
    public void run(String... args) {
        if (!schemaConfig.isAutoInit()) {
            log.info("Schema auto-initialization is disabled");
            return;
        }

        log.info("Initializing database schema for dialect: {}", schemaConfig.getDialect());
        initSchema()
                .doOnSuccess(v -> log.info("Database schema initialized successfully"))
                .doOnError(e -> log.error("Failed to initialize database schema", e))
                .subscribe();
    }

    private Mono<Void> initSchema() {
        String sqlFile = getSqlFile();
        ClassPathResource resource = new ClassPathResource(sqlFile);
        if (!resource.exists()) {
            log.warn("SQL file not found: {}", sqlFile);
            return Mono.empty();
        }

        return Mono.fromCallable(() -> {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(resource);
            populator.setSeparator(";");
            populator.setContinueOnError(true);
            return populator;
        }).flatMap(populator -> {
            log.info("Executing SQL script: {}", sqlFile);
            return Mono.empty();
        });
    }

    private String getSqlFile() {
        return switch (schemaConfig.getDialect().toLowerCase()) {
            case "mysql" -> "sql/mysql/mysql.sql";
            case "oracle" -> "sql/oracle/oracle.sql";
            case "dm" -> "sql/dm/dm.sql";
            default -> "sql/postgresql/postgresql.sql";
        };
    }
}