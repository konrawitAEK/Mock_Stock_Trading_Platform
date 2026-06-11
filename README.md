# Mock Stock Trading Platform

ระบบจำลองการซื้อขายหุ้นแบบ Full-Stack ที่ผู้ใช้เริ่มต้นด้วยเงิน ฿100,000 สามารถซื้อ/ขายหุ้นจำลอง ติดตามพอร์ตการลงทุน และจำลองการเปลี่ยนแปลงราคาตลาดได้

---

## ภาพรวมโปรเจกต์

ระบบรองรับผู้ใช้คนเดียว เริ่มต้นด้วยเงิน ฿100,000 สามารถซื้อ/ขายหุ้นจำลอง 10 ตัว ติดตามพอร์ต กำไร/ขาดทุน จำลองราคาตลาด ±5% และดูประวัติการซื้อขาย ข้อมูลทั้งหมดเก็บใน PostgreSQL

---

## Tech Stack

| Layer    | Technology                                                         |
|----------|--------------------------------------------------------------------|
| Frontend | Angular 18 (Standalone Components), TypeScript, ng-zorro-antd 18 (Ant Design) |
| Backend  | Java 17, Spring Boot 3.2, Maven                                    |
| Storage  | PostgreSQL 16 (Spring Data JPA + Flyway migrations)                |

---

## Features ที่ทำเสร็จแล้ว

- [x] **Dashboard** — แสดงเงินสดคงเหลือ, มูลค่าหุ้นรวม, มูลค่าพอร์ตรวม, กำไร/ขาดทุน, ตารางหุ้นที่ถือ
- [x] **Stock List** — รายการหุ้น 10 ตัว, ค้นหาจาก Symbol/ชื่อบริษัท, เรียงลำดับคอลัมน์ได้
- [x] **Stock Detail** — ราคาปัจจุบัน, Daily Change, รายละเอียดบริษัท, ข้อมูลที่ถือ, ฟอร์มซื้อ/ขาย
- [x] **Buy Stock** — Validation ครบ (จำนวน, เงินสด), คำนวณ Avg Buy Price แบบ Weighted Average
- [x] **Sell Stock** — Validation ครบ (จำนวน, ตรวจสอบหุ้นที่ถือ), Avg Buy Price ไม่เปลี่ยนเมื่อขาย
- [x] **Transaction History** — ราคาล็อกตอนทำรายการ, เรียงใหม่ไปเก่า, Badge BUY/SELL
- [x] **Simulate Market** — สุ่มราคา ±5%, อัปเดต Daily Change %, พอร์ตคำนวณใหม่อัตโนมัติ
- [x] **Unit Tests** — Backend 19 test cases ครอบคลุม buy/sell/simulate logic
- [x] **PostgreSQL** — Spring Data JPA, Flyway migrations (V1: schema, V2: seed data), ข้อมูลคงอยู่เมื่อ restart
- [x] **Docker** — docker-compose พร้อม PostgreSQL + nginx (Angular) + JRE (Spring Boot)
- [x] **UI ที่ใช้งานง่าย** — Ant Design components, Responsive layout, Toast notifications
- [x] **API Response สม่ำเสมอ** — `{ success, data, message }` ทุก endpoint
- [x] **Service Layer** — Angular HttpClient แยกเป็น ApiService + domain services

## Features ที่ยังไม่เสร็จ

- [ ] Authentication / Multi-user
- [ ] Real-time price update (WebSocket)
- [ ] กราฟแสดงประวัติราคา
- [ ] เชื่อมต่อ API ตลาดหุ้นจริง

---

## วิธีติดตั้ง

### สิ่งที่ต้องมีก่อน

- Java 17+
- Maven 3.8+ (หรือใช้ `./mvnw` ที่มีให้แล้ว)
- Node.js 18+ และ npm
- PostgreSQL 16 (local หรือใช้ Docker — ดูด้านล่าง)

---

## วิธี Run Backend

ต้องมี PostgreSQL รันอยู่ก่อน สร้าง database ด้วย:

```sql
CREATE DATABASE mockstock;
CREATE USER mockstock WITH PASSWORD 'mockstock';
GRANT ALL PRIVILEGES ON DATABASE mockstock TO mockstock;
```

หรือรัน PostgreSQL ด้วย Docker เพียงอย่างเดียว:

```bash
docker run -d --name mockstock-pg \
  -e POSTGRES_DB=mockstock -e POSTGRES_USER=mockstock -e POSTGRES_PASSWORD=mockstock \
  -p 5432:5432 postgres:16-alpine
```

จากนั้น run backend:

```bash
cd backend
./mvnw spring-boot:run
```

API server จะเปิดที่ **http://localhost:8080** — Flyway จะ migrate schema และ seed ข้อมูลอัตโนมัติ

### วิธี Run Test (Backend)

```bash
cd backend
./mvnw test
```

---

## วิธี Run Frontend

```bash
cd frontend
npm install
npm start
```

UI จะเปิดที่ **http://localhost:3000**

> ต้องเปิด Backend ก่อน จึงจะโหลดข้อมูลได้

---

## วิธี Run ด้วย Docker Compose

```bash
docker-compose up --build
```

| Service  | URL                   | หมายเหตุ                    |
|----------|-----------------------|-----------------------------|
| Frontend | http://localhost:3000 | Angular เสิร์ฟด้วย nginx    |
| Backend  | http://localhost:8080 | Spring Boot REST API        |

---

## API Endpoints

| Method | Endpoint            | คำอธิบาย                              |
|--------|---------------------|---------------------------------------|
| GET    | /stocks             | ดูรายการหุ้นทั้งหมด                  |
| GET    | /stocks/{symbol}    | ดูรายละเอียดหุ้น + ข้อมูลที่ถือ      |
| GET    | /portfolio          | ดูสรุปพอร์ตการลงทุน                  |
| POST   | /orders/buy         | ซื้อหุ้น `{ symbol, quantity }`       |
| POST   | /orders/sell        | ขายหุ้น `{ symbol, quantity }`        |
| GET    | /transactions       | ดูประวัติการซื้อขาย (ใหม่สุดก่อน)   |
| POST   | /market/simulate    | จำลองการเปลี่ยนแปลงราคา ±5%         |

### รูปแบบ API Response

ทุก endpoint ตอบกลับในรูปแบบเดียวกัน:
```json
{
  "success": true,
  "data": { ... },
  "message": "ok"
}
```

กรณี error จะได้ `"success": false` พร้อม `"message"` อธิบายสาเหตุ

---

## โครงสร้างโปรเจกต์

```
Mock_Stock_Trading_Platform/
├── backend/
│   ├── mvnw / mvnw.cmd                 # Maven Wrapper
│   ├── pom.xml
│   └── src/main/java/com/mockstock/
│       ├── MockStockApplication.java
│       ├── config/CorsConfig.java
│       ├── model/                      # Stock, PortfolioItem, Transaction, UserState (@Entity)
│       ├── dto/                        # ApiResponse<T>, OrderRequest, PortfolioResponse,
│       │                               # HoldingItem, StockDetailResponse
│       ├── repository/                 # StockRepository, PortfolioItemRepository,
│       │                               # TransactionRepository, UserStateRepository
│       ├── store/                      # TradingStore (interface), JpaStore (@Component),
│       │                               # InMemoryStore (test only, no @Component)
│       ├── service/TradingService.java # Business logic (@Transactional)
│       ├── controller/                 # StockController, PortfolioController,
│       │                               # OrderController, TransactionController, MarketController
│       └── exception/GlobalExceptionHandler.java
│   └── src/main/resources/
│       ├── application.properties
│       └── db/migration/
│           ├── V1__init_schema.sql     # สร้างตาราง
│           └── V2__seed_data.sql       # seed cash + หุ้น 10 ตัว
│
├── frontend/
│   ├── angular.json
│   ├── tsconfig.json
│   ├── nginx.conf                      # SPA routing สำหรับ Docker
│   └── src/
│       ├── environments/               # environment.ts, environment.prod.ts
│       ├── app/
│       │   ├── app.config.ts           # provideHttpClient, provideRouter, provideAnimations
│       │   ├── app.routes.ts           # Lazy-loaded routes
│       │   ├── core/
│       │   │   ├── models/index.ts     # TypeScript interfaces ทั้งหมด
│       │   │   └── services/
│       │   │       ├── api.service.ts          # Base HttpClient (unwrap ApiResponse<T>)
│       │   │       ├── stock.service.ts
│       │   │       ├── portfolio.service.ts
│       │   │       ├── order.service.ts
│       │   │       ├── transaction.service.ts
│       │   │       └── market.service.ts
│       │   ├── pages/
│       │   │   ├── dashboard/          # ภาพรวมพอร์ต + ตารางหุ้นที่ถือ
│       │   │   ├── stock-list/         # รายการหุ้นพร้อมค้นหา/เรียงลำดับ
│       │   │   ├── stock-detail/       # ฟอร์มซื้อ/ขาย + ข้อมูลที่ถือ
│       │   │   └── transactions/       # ประวัติการซื้อขาย
│       │   └── shared/
│       │       └── layout/             # Sidebar layout (NzLayoutModule)
│       └── styles.scss
│
├── docker-compose.yml
├── .gitignore
├── README.md
└── AI_USAGE.md
```

---

## ออกแบบข้อมูล (PostgreSQL)

ระบบใช้ Flyway จัดการ schema โดย migration อยู่ที่ `backend/src/main/resources/db/migration/`

| Migration | เนื้อหา |
|-----------|---------|
| V1__init_schema.sql | สร้างตาราง `stocks`, `portfolio_items`, `transactions`, `user_state` |
| V2__seed_data.sql   | เริ่มต้น cash 100,000 + หุ้น 10 ตัว |

### Schema ตาราง

| ตาราง            | Primary Key | คำอธิบาย                                  |
|-----------------|-------------|-------------------------------------------|
| `stocks`        | `symbol`    | ราคาปัจจุบัน, previous, daily change, sector |
| `user_state`    | `id` (=1)   | เงินสดของผู้ใช้                            |
| `portfolio_items`| `symbol`   | จำนวนหุ้น + avg buy price                 |
| `transactions`  | `id` (UUID) | ประวัติการซื้อขาย เรียงตาม timestamp DESC  |

`TradingStore` interface + `JpaStore` implementation ทำให้ Business logic ใน `TradingService` ไม่ต้องเปลี่ยน

---

## สิ่งที่จะปรับปรุงถ้ามีเวลาเพิ่ม

- เพิ่ม `@Version` + Optimistic locking บน `user_state` เพื่อป้องกัน Race condition
- เพิ่มกราฟราคาหุ้น (เก็บ snapshot ทุกครั้งที่ Simulate → แสดงด้วย ng2-charts)
- เพิ่ม Real-time update ด้วย WebSocket / SSE
- เพิ่มระบบ Authentication ด้วย Spring Security + JWT
- เขียน Frontend unit test ด้วย Angular Testing Library + Jasmine
- เขียน E2E test ด้วย Cypress
