package com.seiyrikon.seiyrikon_security.configuration;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class SeiyrikonSecurityTableInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;
    private final SeiyrikonSecurityTableProperties properties;


    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        if(!properties.isAutoCreateSeiyrikonSecurityTable()) return;

        try {
            createRoleTable();
            createUserRoleTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createRoleTable() {
        String sql = String.format("""
            CREATE TABLE IF NOT EXISTS %s (
                %s VARCHAR(255) PRIMARY KEY,
                %s VARCHAR(255) NOT NULL UNIQUE
            )
            """,
                properties.getRoleTable(),       // table name
                properties.getRoleIdColumn(),    // id column
                properties.getRoleNameColumn()   // name column
        );
        jdbcTemplate.execute(sql);
    }

    private void createUserRoleTable() {
        String sql = String.format("""
            CREATE TABLE IF NOT EXISTS %s (
                %s VARCHAR(255) NOT NULL,
                %s VARCHAR(255) NOT NULL,
                PRIMARY KEY (%s, %s),
                FOREIGN KEY (%s) REFERENCES %s(%s)
            )
            """,
                properties.getUserRoleTable(),           // table name
                properties.getUserRoleUserIdColumn(),     // user_id column
                properties.getUserRoleRoleIdColumn(),     // role_id column
                properties.getUserRoleUserIdColumn(),     // PK part 1
                properties.getUserRoleRoleIdColumn(),     // PK part 2
                properties.getUserRoleRoleIdColumn(),     // FK column
                properties.getRoleTable(),               // referenced table
                properties.getRoleIdColumn()             // referenced column
        );
        jdbcTemplate.execute(sql);
    }

    private void createTokenBlocklistTable() {
        String sql = String.format("""
        CREATE TABLE IF NOT EXISTS %s (
            %s VARCHAR(512) PRIMARY KEY,
            %s DATETIME NOT NULL
        )
        """,
                properties.getTokenBlacklistTable(),
                properties.getTokenBlacklistTokenColumn(),
                properties.getTokenBlacklistExpiryColumn()
        );
        jdbcTemplate.execute(sql);
    }
}
