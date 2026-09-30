#!/usr/bin/env bash
# ==============================================================================
# BITEDASH AUTOMATED ENCRYPTED DATABASE BACKUP SCRIPT
# Designed for production cron scheduling & disaster recovery
# ==============================================================================
set -euo pipefail

BACKUP_DIR="${BACKUP_DIR:-/backups/postgresql}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
DATABASE_NAME="${POSTGRES_DB:-bitedash_db}"
PG_HOST="${POSTGRES_HOST:-localhost}"
PG_PORT="${POSTGRES_PORT:-5432}"
PG_USER="${POSTGRES_USER:-bitedash_user}"
RETENTION_DAYS=30
ENCRYPTION_PASSPHRASE="${BACKUP_ENCRYPTION_KEY:-bitedash_backup_passphrase_production_2026}"

mkdir -p "${BACKUP_DIR}"

BACKUP_FILE="${BACKUP_DIR}/${DATABASE_NAME}_${TIMESTAMP}.sql.gz"
ENCRYPTED_FILE="${BACKUP_FILE}.enc"

echo "[INFO] [$(date)] Starting PostgreSQL backup for ${DATABASE_NAME}..."

# 1. Execute pg_dump with custom compressed archive format
PGPASSWORD="${POSTGRES_PASSWORD:-bitedash_secure_pass}" pg_dump \
    -h "${PG_HOST}" \
    -p "${PG_PORT}" \
    -U "${PG_USER}" \
    -d "${DATABASE_NAME}" \
    --format=custom \
    --no-owner \
    --no-privileges | gzip > "${BACKUP_FILE}"

echo "[INFO] [$(date)] Database dumped and gzipped successfully: ${BACKUP_FILE}"

# 2. Encrypt with OpenSSL AES-256-CBC
openssl enc -aes-256-cbc -salt -pbkdf2 -iter 100000 \
    -in "${BACKUP_FILE}" \
    -out "${ENCRYPTED_FILE}" \
    -k "${ENCRYPTION_PASSPHRASE}"

# Remove unencrypted dump
rm -f "${BACKUP_FILE}"

echo "[INFO] [$(date)] Backup encrypted successfully: ${ENCRYPTED_FILE}"

# 3. Optional: Upload to AWS S3 / Google Cloud Storage
if command -v aws &> /dev/null && [ -n "${S3_BACKUP_BUCKET:-}" ]; then
    echo "[INFO] Uploading encrypted backup to S3 bucket ${S3_BACKUP_BUCKET}..."
    aws s3 cp "${ENCRYPTED_FILE}" "s3://${S3_BACKUP_BUCKET}/backups/${DATABASE_NAME}_${TIMESTAMP}.sql.gz.enc"
fi

# 4. Prune old backups older than RETENTION_DAYS
echo "[INFO] Cleaning up local backups older than ${RETENTION_DAYS} days..."
find "${BACKUP_DIR}" -type f -name "${DATABASE_NAME}_*.sql.gz.enc" -mtime +"${RETENTION_DAYS}" -delete

echo "[INFO] [$(date)] Backup workflow completed successfully."
