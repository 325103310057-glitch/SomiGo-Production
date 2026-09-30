# SomiGo External Cloud Database Inspection & Administration Guide

This document explains how to inspect and manage the authoritative **SomiGo Cloud PostgreSQL Database** outside the Android client application.

---

## 1. Architecture Isolation Rule

The Android application is **strictly a client**.
* **NO** production database exists on the mobile device.
* The Android app connects only via HTTPS to the SomiGo Backend API Gateway (`https://api.somigo.in`).
* The Backend API communicates privately with Cloud PostgreSQL.
* All customer accounts, orders, restaurants, payments, group ordering sessions, delivery pins, support tickets, and delivery partner tracking are authoritatively persisted in the Cloud Database.

```
+--------------------------------+
|  SomiGo Android App (Play Store)|
+--------------------------------+
               |
               | HTTPS (REST API)
               v
+--------------------------------+
|  SomiGo Backend API (FastAPI)  |
+--------------------------------+
               |
               | Private VPC Network (Port 5432)
               v
+--------------------------------+       +------------------------------------+
|  Cloud PostgreSQL Database     | <---> | pgAdmin / DBeaver / Cloud Console  |
|  (Authoritative Production DB) |       | (External DB Inspection Tools)     |
+--------------------------------+       +------------------------------------+
```

---

## 2. Option A: Web Database Console (pgAdmin 4)

If running the containerized infrastructure via `docker compose`:

1. Start the stack:
   ```bash
   cd backend
   docker compose up -d
   ```
2. Open your web browser:
   * **URL:** `http://localhost:5050`
   * **Username:** `admin@somigo.in`
   * **Password:** `somigo_admin_secret_2026`
3. Expand **Servers** -> **SomiGo Production Cloud DB** -> **Databases** -> **somigo_db** -> **Schemas** -> **public** -> **Tables**.
4. Right-click on any table (e.g. `users`, `orders`, `group_orders`, `payments`, `restaurants`) and select **View/Edit Data** -> **All Rows**.

---

## 3. Option B: Desktop SQL Clients (DBeaver / DataGrip / TablePlus)

To connect from your local computer:

* **Host:** `localhost` (or your cloud RDS endpoint e.g., `db.somigo.internal` / AWS RDS hostname)
* **Port:** `5432`
* **Database:** `somigo_db`
* **Username:** `database_admin` (or `analytics_readonly` for reporting)
* **Password:** your secure role password
* **SSL Mode:** `require` or `prefer`

### Pre-configured Profiles
* DBeaver: `backend/database/dbeaver_connection.json`
* pgAdmin: `backend/database/pgadmin_servers.json`

---

## 4. Complete Database Tables (22 Tables)

```text
DATABASE: somigo_db
└── public
    ├── users                      (Customers, Owners, Drivers, Admins)
    ├── customer_addresses         (Delivery locations, GPS coords)
    ├── restaurants                (Approved vendors, menus, operating hours)
    ├── restaurant_branches        (Multi-location branches)
    ├── categories                 (Biryani, Pizza, Burgers, etc.)
    ├── menu_items                 (Authoritative server-side pricing)
    ├── item_variants              (Sizes, portions)
    ├── item_addons                (Extra toppings, sides)
    ├── carts                      (Active user sessions)
    ├── cart_items                 (Cart contents with snapshots)
    ├── orders                     (Authoritative purchase ledger with Delivery PIN)
    ├── order_items                (Historical price & item snapshots)
    ├── group_orders               (Group order session code & host)
    ├── group_order_participants   (Friend participants & split calculations)
    ├── scheduled_orders           (Advance scheduled delivery triggers)
    ├── payments                   (PCI-compliant transaction records)
    ├── delivery_partners          (Fleet status, telemetry, ratings)
    ├── delivery_tracking          (GPS breadcrumbs for live tracking)
    ├── reviews                    (Verified order ratings)
    ├── coupons                    (Discount rules and usage caps)
    ├── reward_points              (SomiGo Coins balance & ledger)
    ├── support_tickets            (Customer issue tickets & resolution status)
    ├── favorites                  (Saved restaurants)
    ├── notifications              (Push notifications ledger)
    └── audit_logs                 (Security and administrative event trail)
```

---

## 5. Sample Query Reference

### A. View Registered Customers
```sql
SELECT id, full_name, email, phone_number, role, account_status, created_at 
FROM users 
ORDER BY id ASC;
```

### B. View Group Orders & Split Bill Sessions
```sql
SELECT group_code, host_name, participant_name, item_subtotal, shared_charges, final_total
FROM group_order_participants
ORDER BY created_at DESC;
```

### C. View Customer Orders with Delivery PINs
```sql
SELECT order_number, customer_id, restaurant_id, total_amount, delivery_pin, payment_status, order_status
FROM orders
ORDER BY id DESC;
```
