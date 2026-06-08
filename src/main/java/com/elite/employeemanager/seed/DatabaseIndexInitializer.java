package com.elite.employeemanager.seed; // or your configuration package

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(0) // Run this before seeding users (Order 2)
public class DatabaseIndexInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Applying partial database indexes for soft-delete support...");

        // 1. Drop old global unique constraints if they exist
        jdbcTemplate.execute("ALTER TABLE employees DROP CONSTRAINT IF EXISTS employees_employee_code_key");
        jdbcTemplate.execute("ALTER TABLE employees DROP CONSTRAINT IF EXISTS employees_work_email_key");
        jdbcTemplate.execute("ALTER TABLE employees DROP CONSTRAINT IF EXISTS employees_phone_key");
        jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_email_key");

        // 2. Create partial unique indexes (only unique for active/non-deleted records)
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_employees_code_active ON employees (employee_code) WHERE is_deleted = false");
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_employees_work_email_active ON employees (work_email) WHERE is_deleted = false");
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_employees_phone_active ON employees (phone) WHERE is_deleted = false");

        // Ensure user_id is only unique for non-deleted employees
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_employees_user_id_active ON employees (user_id) WHERE is_deleted = false");

        // For credentials: only unique for active accounts
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_active ON users (email) WHERE is_active = true");

        System.out.println("Database partial indexes applied successfully!");
    }
}
