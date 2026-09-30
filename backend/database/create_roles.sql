-- ============================================================================
-- BITEDASH POSTGRESQL SECURE ROLES & LEAST-PRIVILEGE CONFIGURATION
-- ============================================================================

-- 1. Database Admin Role (Superuser / Full Schema Owner)
DO $$ BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'database_admin') THEN
        CREATE ROLE database_admin WITH LOGIN PASSWORD 'secure_admin_pass_replace_in_prod' CREATEDB CREATEROLE;
    END IF;
END $$;

-- 2. Migration User (For Alembic / Schema Upgrades)
DO $$ BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'migration_user') THEN
        CREATE ROLE migration_user WITH LOGIN PASSWORD 'secure_migration_pass_replace_in_prod';
    END IF;
END $$;

-- 3. Application Runtime User (Used by FastAPI backend over private network)
DO $$ BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'app_runtime') THEN
        CREATE ROLE app_runtime WITH LOGIN PASSWORD 'secure_runtime_pass_replace_in_prod';
    END IF;
END $$;

-- 4. Analytics Read-Only User (Used by Metabase, DBeaver, Grafana)
DO $$ BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'analytics_readonly') THEN
        CREATE ROLE analytics_readonly WITH LOGIN PASSWORD 'secure_readonly_pass_replace_in_prod';
    END IF;
END $$;

-- ============================================================================
-- PRIVILEGE ASSIGNMENTS
-- ============================================================================

-- Migration User: CREATE, ALTER, DROP tables in public schema
GRANT ALL ON SCHEMA public TO migration_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO migration_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO migration_user;

-- App Runtime: SELECT, INSERT, UPDATE, DELETE on tables; USAGE on sequences (NO DDL)
GRANT USAGE ON SCHEMA public TO app_runtime;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_runtime;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_runtime;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_runtime;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO app_runtime;

-- Analytics Read-Only: SELECT ONLY (Cannot insert, update, delete, or alter)
GRANT USAGE ON SCHEMA public TO analytics_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO analytics_readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO analytics_readonly;

-- Revoke dangerous permissions from public
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
