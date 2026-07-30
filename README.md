# Transaction Monitoring & Alerts Dashboard

> Team Project — Neueda Training Programme

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Why We Chose This Topic](#2-why-we-chose-this-topic)
3. [Technology Stack](#3-technology-stack)
4. [Project Structure](#4-project-structure)
5. [Core Concepts](#5-core-concepts)
   - 5.1 [Transaction](#51-transaction)
   - 5.2 [MonitoringRule](#52-monitoringrule)
   - 5.3 [Alert & AlertStatus](#53-alert--alertstatus)
   - 5.4 [AlertSeverity](#54-alertseverity)
6. [Monitoring Rule Types](#6-monitoring-rule-types)
7. [Alert Lifecycle](#7-alert-lifecycle)
8. [API Overview](#8-api-overview)
9. [Database Design](#9-database-design)
10. [How to Run](#10-how-to-run)
11. [Team](#11-team)

---

## 1. Project Overview

This project is a backend REST API for a **Transaction Monitoring and Alerts Dashboard**, built with Java 17 and Spring Boot.

The system accepts financial transactions, evaluates each one against a set of configurable `MonitoringRule`s, and automatically creates `Alert`s whenever a rule is triggered. Operators can then review, acknowledge, investigate, and close each `Alert` through a defined lifecycle.

The backend exposes four groups of endpoints:

| Group | Purpose |
|---|---|
| Transaction API | Record and query transactions |
| Alert API | Manage alert lifecycle |
| Monitoring Rule API | View and adjust rule thresholds |
| User API | Manage known accounts / payees |

---

## 2. Why We Chose This Topic

Three project topics were offered:

**Option A — Portfolio Manager**
Build an application to manage a financial portfolio containing stocks, bonds, and cash. The main deliverable is a REST API for saving and retrieving portfolio records, with an optional frontend for browsing and visualizing portfolio performance.

**Option B — Payment Processing System**
Build a system that handles the full lifecycle of a payment: `CREATED → VALIDATED → SENT → COMPLETED` (or `FAILED`). The focus is on correct state transitions, idempotency, and an audit trail of every status change.

**Option C — Transaction Monitoring & Alerts Dashboard** ← *our choice*
Build a system that evaluates incoming transactions against configurable rules and generates `Alert`s for suspicious or unusual activity. Operators manage the `Alert` lifecycle from `OPEN` through to `CLOSED` or `DISMISSED`.

### Why we chose Option C

1. **Real-world relevance**
   Transaction monitoring is a core part of how financial institutions detect fraud and meet compliance requirements (e.g. AML — Anti-Money Laundering). Working on this topic gives us direct exposure to a problem domain that is widely used in the industry.

2. **Richer rule logic**
   The system requires us to implement multiple distinct rule types (amount threshold, velocity, new payee, daily limit). This gave us practice writing business logic that is more varied and testable than simple CRUD operations.

3. **Two connected lifecycles**
   `Transaction`s have their own status (`PENDING`, `COMPLETED`, `FAILED`), and `Alert`s have a separate lifecycle (`OPEN → ACKNOWLEDGED → INVESTIGATING → CLOSED / DISMISSED`). Keeping these two lifecycles correct and consistent is an interesting design challenge.

4. **Configurable rules at runtime**
   `MonitoringRule`s are stored in the database so their thresholds (e.g. `thresholdAmount`, `timeWindowMinutes`, `maxCount`, `dailyLimitAmount`) can be changed without redeploying the application. This pushed us to think about how configuration and business logic interact.

5. **Clear scope for incremental delivery**
   The project has a natural minimal viable product (record a transaction, trigger an alert) that can be expanded step by step — which fits the agile approach encouraged by the training programme.

---

## 3. Technology Stack

| Item | Detail |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.1.0 |
| Web layer | Spring MVC (`spring-boot-starter-webmvc`) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL (production), H2 (tests) |
| Validation | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| Build tool | Maven (mvnw wrapper included) |
| Testing | JUnit Jupiter 5, Spring Boot Test |

---

## 4. Project Structure

```
transaction-monitoring-backend/
└── src/main/java/org/example/transactionmonitoringbackend/
    ├── controller/          HTTP endpoints (REST controllers)
    │   ├── TransactionController
    │   ├── AlertController
    │   ├── MonitoringRuleController
    │   └── UserController
    ├── service/             Business logic
    │   ├── TransactionService
    │   ├── AlertService
    │   ├── MonitoringRuleService
    │   ├── UserService
    │   └── FixedRules       (rule evaluation engine)
    ├── entity/              JPA entities and enums
    │   ├── Transaction
    │   ├── Alert
    │   ├── MonitoringRule
    │   ├── AlertStatus      (enum)
    │   ├── AlertSeverity    (enum)
    │   └── TransactionStatus (enum)
    ├── repository/          Spring Data JPA repositories
    │   ├── TransactionRepository
    │   ├── AlertRepository
    │   ├── MonitoringRuleRepository
    │   └── UserRepository
    └── exception/           Error handling
        ├── GlobalExceptionHandler
        ├── ApiError
        ├── ValidationException
        ├── TransactionNotFoundException
        ├── AlertNotFoundException
        └── InvalidStatusTransitionException

Database_Design_Document/    Database schema documents
docs/api/                    Markdown API reference
```

---

## 5. Core Concepts

### 5.1 Transaction

Represents a single financial movement between two accounts.

| Field | Description |
|---|---|
| `id` | Auto-generated primary key |
| `accountId` | The account that initiated the transaction |
| `payeeId` | The receiving account or counterparty |
| `amount` | Transaction value (up to 18 digits, 2 decimal places) |
| `currency` | ISO 4217 currency code (default: `"USD"`) |
| `transType` | Type of transaction (e.g. `"TRANSFER"`, `"PAYMENT"`) |
| `transTimestamp` | The time the transaction occurred |
| `status` | `PENDING` \| `COMPLETED` \| `FAILED` |

### 5.2 MonitoringRule

Defines the conditions under which an `Alert` should be created.

| Field | Description |
|---|---|
| `id` | Primary key |
| `ruleType` | One of the four rule types (see [section 6](#6-monitoring-rule-types)) |
| `isActive` | Whether the rule is currently enabled |
| `thresholdAmount` | Used by `AMOUNT_THRESHOLD` rule |
| `maxCount` | Used by `VELOCITY_RULE` |
| `timeWindowMinutes` | Used by `VELOCITY_RULE` |
| `dailyLimitAmount` | Used by `DAILY_LIMIT_RULE` |

Rules are read from the database on every transaction evaluation, so threshold changes take effect immediately without restarting the service. If a rule row is missing from the database, the system falls back to safe built-in defaults.

### 5.3 Alert & AlertStatus

An `Alert` is created automatically when a `Transaction` triggers one or more `MonitoringRule`s. Each `Alert` references the `Transaction` and the `MonitoringRule` that fired.

| Field | Description |
|---|---|
| `id` | Primary key |
| `transactionId` | The `Transaction` that triggered this alert |
| `ruleId` | The `MonitoringRule` that was triggered |
| `status` | Current position in the alert lifecycle |
| `severity` | `LOW` \| `MEDIUM` \| `HIGH` |
| `createdAt` | Timestamp when the alert was generated |

`AlertStatus` values and allowed transitions:

| From | To (allowed) |
|---|---|
| `OPEN` | `ACKNOWLEDGED`, `DISMISSED` |
| `ACKNOWLEDGED` | `INVESTIGATING`, `CLOSED`, `DISMISSED` |
| `INVESTIGATING` | `CLOSED`, `DISMISSED` |
| `CLOSED` | *(terminal — no further transitions)* |
| `DISMISSED` | *(terminal — no further transitions)* |

Attempting an invalid transition throws an `InvalidStatusTransitionException` (HTTP 400).

### 5.4 AlertSeverity

| Value | Meaning |
|---|---|
| `LOW` | Informational; may require follow-up |
| `MEDIUM` | Warrants prompt review |
| `HIGH` | Requires immediate attention |

---

## 6. Monitoring Rule Types

The rule evaluation engine (`FixedRules`) supports four built-in rules:

| Rule | Trigger condition | Default |
|---|---|---|
| `AMOUNT_THRESHOLD` | A single transaction's `amount` exceeds `thresholdAmount` | $10,000 |
| `VELOCITY_RULE` | The same `accountId` submits more than `maxCount` transactions within `timeWindowMinutes` | 5 transactions / 10 minutes |
| `NEW_PAYEE_RULE` | The transaction's `payeeId` has never been seen before (not present in the User table) | — |
| `DAILY_LIMIT_RULE` | Projected daily total for an `accountId` would exceed `dailyLimitAmount` | $50,000 |

A single transaction can trigger multiple rules simultaneously; a separate `Alert` is created for each triggered rule.

---

## 7. Alert Lifecycle

```
[Transaction received]
        |
        v
[FixedRules evaluates all active MonitoringRules]
        |
   (rule fires)
        |
        v
     OPEN  ─────────────────────────────────> DISMISSED
        |
        v
  ACKNOWLEDGED ──────────────────────────────> DISMISSED
        |
        v
  INVESTIGATING ─────────────────────────────> DISMISSED
        |
        v
     CLOSED
```

---

## 8. API Overview

Full endpoint documentation is in: [`docs/api/`](transaction-monitoring-backend/docs/api/)

**Transactions**

| Method | Path | Description |
|---|---|---|
| `POST` | `/transactions` | Record a new transaction |
| `GET` | `/transactions` | List all transactions |
| `GET` | `/transactions/{id}` | Get a single transaction |

**Alerts**

| Method | Path | Description |
|---|---|---|
| `GET` | `/alerts` | List all alerts |
| `GET` | `/alerts/{id}` | Get a single alert |
| `PATCH` | `/alerts/{id}/status` | Update alert status |

**Monitoring Rules**

| Method | Path | Description |
|---|---|---|
| `GET` | `/rules` | List all monitoring rules |
| `PUT` | `/rules/{id}` | Update a monitoring rule's configuration |

**Users**

| Method | Path | Description |
|---|---|---|
| `POST` | `/users` | Register a known account / payee |
| `GET` | `/users` | List all users |

---

## 9. Database Design

- **Database name:** `tmDb`
- **Timezone:** All timestamps are stored in UTC.

| Table | Purpose |
|---|---|
| `transactions` | `Transaction` records |
| `monitoring_rules` | `MonitoringRule` configuration |
| `alerts` | `Alert` records |
| `alerts_status_history` | Full audit trail of `AlertStatus` changes |
| `user_table` | Known accounts used by `NEW_PAYEE_RULE` |

Full schema details are in: [`Database_Design_Document/`](Database_Design_Document/)

---

## 10. How to Run

**Prerequisites**
- Java 17 or higher
- MySQL running locally (or adjust `application.properties`)
- Create the database: `CREATE DATABASE tmDb;`

**Steps**

```bash
# 1. Clone the repository
git clone https://github.com/Neueda-Learning/xian-BASIC-transaction_monitoring.git

# 2. Navigate to the backend directory
cd transaction-monitoring-backend

# 3. Configure the database connection in:
#    src/main/resources/application.properties

# 4. Build and start the application
./mvnw spring-boot:run

# The API will be available at: http://localhost:8080
```

**Running tests**

```bash
./mvnw test
```

---

## 11. Team

This project was built as part of the **Neueda Training Programme**.
Repository: [Neueda-Learning/xian-BASIC-transaction_monitoring](https://github.com/Neueda-Learning/xian-BASIC-transaction_monitoring)
