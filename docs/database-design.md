# Database Design Diagram

```mermaid
erDiagram
    COUNTRIES ||--o{ PACKAGE_CATALOGS : has
    COUNTRIES ||--o{ CLASS_SCHEDULES : hosts
    APP_USERS ||--o{ PURCHASED_PACKAGES : owns
    APP_USERS ||--o{ BOOKINGS : makes
    APP_USERS ||--o{ CREDIT_TRANSACTIONS : receives
    PACKAGE_CATALOGS ||--o{ PURCHASED_PACKAGES : templates
    PURCHASED_PACKAGES ||--o{ BOOKINGS : funds
    PURCHASED_PACKAGES ||--o{ CREDIT_TRANSACTIONS : updates
    CLASS_SCHEDULES ||--o{ BOOKINGS : contains
    CLASS_SCHEDULES ||--o{ CREDIT_TRANSACTIONS : references

    COUNTRIES {
      bigint id PK
      string code UK
      string name UK
    }

    APP_USERS {
      bigint id PK
      string email UK
      string password
      string full_name
      boolean verified
      string verification_token
      string reset_password_token
    }

    PACKAGE_CATALOGS {
      bigint id PK
      bigint country_id FK
      string name
      int credits
      decimal price
      int validity_days
      boolean active
    }

    PURCHASED_PACKAGES {
      bigint id PK
      bigint user_id FK
      bigint package_catalog_id FK
      int total_credits
      int remaining_credits
      datetime purchased_at
      datetime expires_at
      string status
    }

    CLASS_SCHEDULES {
      bigint id PK
      bigint country_id FK
      string title
      string description
      datetime start_time
      datetime end_time
      int required_credits
      int capacity
      bigint version
    }

    BOOKINGS {
      bigint id PK
      bigint user_id FK
      bigint schedule_id FK
      bigint purchased_package_id FK
      string status
      int credits_charged
      datetime booked_at
      datetime cancelled_at
      datetime checked_in_at
      int waitlist_position
    }

    CREDIT_TRANSACTIONS {
      bigint id PK
      bigint user_id FK
      bigint purchased_package_id FK
      bigint schedule_id FK
      string type
      int delta_credit
      int balance_after
      string remarks
    }
```

Rules covered by this schema:

- Package catalog belongs to exactly one country.
- Purchased package inherits credit amount and expiration snapshot.
- Booking always points to the package whose credits were reserved or refunded.
- Waitlist is modeled through `BOOKINGS.status = WAITLISTED` with FIFO via `waitlist_position`.

Downloadable file:

- docs/database-structure.svg`r

