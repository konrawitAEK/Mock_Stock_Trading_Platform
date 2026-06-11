# บันทึกการใช้ AI (Claude Code)

เอกสารนี้บันทึกการใช้ AI ตลอดการพัฒนาโปรเจกต์นี้

---

## 1. สิ่งที่ใช้ AI ช่วย

- **วิเคราะห์ requirements** — แปลง spec ภาษาไทยให้เป็น feature list พร้อม business rules และ edge cases ที่ชัดเจน
- **ออกแบบ data model** — ออกแบบ entity `Stock`, `PortfolioItem`, `Transaction`, `UserState` และ schema PostgreSQL
- **สร้าง backend** — โครงสร้าง Spring Boot ทั้งหมด: entity, DTO, repository, store interface, service, controller, exception handler, unit tests
- **สร้าง frontend** — โครงสร้าง Angular 18 ทั้งหมด: standalone components, lazy-loaded routing, service layer, ng-zorro-antd UI
- **Migrate ไป PostgreSQL** — ออกแบบ `TradingStore` interface, `JpaStore` implementation, Flyway migrations, docker-compose
- **Refactor** — เพิ่ม Lombok, Swagger/OpenAPI, แยก DTO เป็น request/response, เปลี่ยน package `model` → `entity`
- **แก้ปัญหา Docker** — เปลี่ยน base image จาก `-alpine` เป็น `-jammy` รองรับ ARM64, เพิ่ม `.npmrc` สำหรับ peer deps
- **Service Layer Split** — แยก `TradingService` ขนาดใหญ่ออกเป็น 5 services อิสระ (MarketService, OrderService, PortfolioService, StockService, TransactionService) พร้อม tests แยกต่างหาก
- **Exception Handling** — อธิบาย flow ของ `GlobalExceptionHandler` + `@ControllerAdvice` และความสัมพันธ์กับ Service layer
- **เขียนเอกสาร** — README.md (ภาษาไทย) พร้อม Architecture Overview, Business Logic, ตัวอย่าง API และไฟล์นี้

---

## 2. ตัวอย่าง Prompt ที่ใช้

### Prompt 1 — วิเคราะห์ Requirements
> "อ่าน spec ภาษาไทยนี้สำหรับระบบซื้อขายหุ้นจำลอง แยก: (1) features ทั้งหมด (2) business rules (3) edge cases (4) API endpoints ที่ต้องการ (5) data models ที่ต้องการ"

**เหตุผล:** Spec มีกว่า 400 บรรทัด ใช้ AI สกัดเป็น checklist เพื่อป้องกันการพลาด edge case เช่น "avgBuyPrice ต้องไม่เปลี่ยนเมื่อขาย" และ "ราคาใน transaction ต้องล็อก ณ เวลาทำรายการ"

### Prompt 2 — ออกแบบ Data Model และ Business Logic
> "เขียน TradingService.buyStock(symbol, quantity) สำหรับ Spring Boot กฎ: validate quantity > 0, validate stock exists, validate cash >= price × quantity, หักเงิน, คำนวณ avgBuyPrice ใหม่แบบ weighted average, บันทึก transaction พร้อมล็อกราคา"

**เหตุผล:** สูตร weighted average price ผิดพลาดง่าย ใช้ AI draft แล้วตรวจสอบกับตัวอย่างในสเปก `((10×100)+(10×200))/20 = 150`

### Prompt 3 — Simulate Market Logic
> "เขียน simulateMarket() กฎ: random ±5%, ต้องเก็บ previousPrice = currentPrice (เก่า) ก่อนอัปเดต newPrice, คำนวณ dailyChange และ changePercent"

**เหตุผล:** ลำดับการ set ค่าสำคัญมาก — ต้องเก็บ `previousPrice` ก่อนเปลี่ยน `currentPrice` ไม่เช่นนั้น `dailyChange` จะเป็น 0 เสมอ

### Prompt 4 — Migrate ไป PostgreSQL
> "เพิ่ม PostgreSQL ใช้ Spring Data JPA + Flyway สร้าง TradingStore interface, JpaStore implementation, 4 repositories, Flyway V1 schema + V2 seed data, docker-compose พร้อม health check"

**เหตุผล:** ต้องการให้ unit tests เดิมทำงานได้โดยไม่ต้องแก้ไข ใช้ strategy pattern ทำให้ `InMemoryStore` (สำหรับ test) และ `JpaStore` (production) แยกกันอย่างชัดเจน

### Prompt 5 — Refactor ด้วย Pattern จาก POS project
> "เพิ่ม Lombok ใน entity และ DTO, เพิ่ม Swagger/OpenAPI, แยก dto/request/ และ dto/response/ พร้อม static from() factory method, เปลี่ยน package model → entity"

**เหตุผล:** ลด boilerplate ใน entity จาก ~70 บรรทัด (getter/setter ล้วน) เหลือ ~20 บรรทัด และทำให้ controller สะอาดขึ้นด้วย `StockDetailResponse.from(stock, holding)` แทนการประกอบ 12 บรรทัด

### Prompt 6 — Service Layer Split
> "แยก TradingService.java ที่มี business logic ทั้งหมดออกเป็น service แยกตาม domain: MarketService (simulate), OrderService (buy/sell), PortfolioService (portfolio summary + cash), StockService (query stocks), TransactionService (query history) พร้อมย้าย test ให้ตรงกับแต่ละ service"

**เหตุผล:** `TradingService` มีหน้าที่หลายอย่างเกินไป (God Class) ทำให้ยากต่อการเพิ่ม feature และเขียน test แยกส่วน การแยกตาม Single Responsibility ทำให้แต่ละ service inject เฉพาะ dependencies ที่ต้องการจริงๆ

### Prompt 7 — อธิบาย Exception Handling
> "GlobalExceptionHandler ไปเรียกใช้ตรงไหนและเรียกยังไง"

**เหตุผล:** ทำความเข้าใจว่า `@ControllerAdvice` ทำงานอัตโนมัติโดย Spring — Service แค่ `throw` ออกมา ไม่ต้องเขียน `try-catch` ใน Controller เลย

---

## 3. สิ่งที่แก้ไขหลัง AI Generate

1. **CORS configuration** — AI ตั้งค่าใน `application.properties` แต่ Spring Boot 3.x ต้องการ `WebMvcConfigurer` bean แก้เป็น `CorsConfig.java`

2. **`buildPortfolioResponse()` คำนวณ P/L ผิด** — Draft แรก hardcode `totalProfitLoss = totalPortfolioValue - 100000` แก้เป็น `stockMarketValue - totalCostBasis` ซึ่งวัด unrealized P/L บน holdings ปัจจุบันเท่านั้น

3. **Transaction sort order** — `getTransactions()` คืน list ตามลำดับ insert เปลี่ยนให้ sort by timestamp descending ใน `TradingService` เพื่อให้ทำงานถูกต้องกับทั้ง `InMemoryStore` (test) และ `JpaStore` (production)

4. **Docker base image ARM64** — `eclipse-temurin:17-jre-alpine` ไม่มี image สำหรับ ARM64 (Apple Silicon) เปลี่ยนเป็น `eclipse-temurin:17-jre-jammy`

5. **npm ci peer deps** — `ng-zorro-antd` มี peer dependency conflicts ทำให้ `npm ci` ใน Docker ล้มเหลว แก้ด้วยการเพิ่มไฟล์ `.npmrc` ที่มี `legacy-peer-deps=true`

6. **tsconfig.json deprecated options** — ลบ `baseUrl` และ `downlevelIteration` (deprecated ใน TypeScript 5.5+) แล้วเพิ่ม `rootDir` และแก้ `paths` alias ให้ใช้ relative path

7. **`flyway-database-postgresql` dependency** — AI เพิ่ม artifact นี้ซึ่งใช้ได้เฉพาะ Flyway 10.x แต่ Spring Boot 3.2 ใช้ Flyway 9.x ที่รองรับ PostgreSQL ใน `flyway-core` อยู่แล้ว ลบออก

8. **`UserState.id` type mismatch** — AI ใช้ Java `long` แต่ SQL schema ใช้ `INT` Hibernate 6 validate strict กว่า → ต้องเปลี่ยน SQL เป็น `BIGINT` และเพิ่ม V3 migration สำหรับ volume เก่า

9. **`StockController` inject ผิด bean** — หลัง refactor `InMemoryStore` ไม่มี `@Component` แต่ `StockController` ยังใช้ `InMemoryStore` โดยตรง แก้ให้ inject `TradingService` แทน

10. **Service Split — dependency direction** — ตอนแยก `TradingService` ออกเป็น 5 services AI draft ให้ `OrderService` คำนวณ portfolio response เองซ้ำ แก้โดยให้ `OrderService` inject `PortfolioService` และ delegate การคำนวณไปที่ `PortfolioService.buildPortfolioResponse()` แทน

---

## 4. จุดที่ AI Generate ผิด

### ปัญหา 1: สูตร P/L hardcode
```java
// AI generate (ผิด)
double totalProfitLoss = totalPortfolioValue - 100000.0;

// แก้เป็น
double totalProfitLoss = stockMarketValue - totalCostBasis;
```
**สาเหตุ:** hardcode initial capital ถ้าผู้ใช้ขายหุ้นทั้งหมด `cash - 100000` จะแสดงค่าผิด

### ปัญหา 2: CORS ใน application.properties
```properties
# AI generate (ไม่ทำงาน)
spring.web.cors.allowed-origins=http://localhost:3000
```
**สาเหตุ:** Spring Boot 3.x ไม่รองรับ property นี้ preflight request คืน `403 Forbidden` ต้องใช้ `@Configuration` bean

### ปัญหา 3: `@ctrl/tinycolor` peer dependency หายไป
**สาเหตุ:** AI generate `package.json` ที่มี `ng-zorro-antd` แต่ลืม peer dependency `@ctrl/tinycolor` ทำให้ build ล้มเหลว พบเฉพาะตอน build ไม่ใช่ตอน install

### ปัญหา 4: `changePercent` assertion precision ใน test
```java
// AI generate (อาจ fail)
assertTrue(Math.abs(stock.getChangePercent()) <= 5.0);

// แก้เป็น
assertTrue(Math.abs(stock.getChangePercent()) <= 5.01);
```
**สาเหตุ:** floating-point arithmetic ทำให้ `5.0 * 1.05` อาจเกิน 5.0 เล็กน้อย

---

## 5. สิ่งที่จะปรับปรุงถ้ามีเวลาเพิ่ม

1. **Optimistic locking** — เพิ่ม `@Version` บน `UserState` entity เพื่อป้องกัน Race condition กรณีหลาย request ทำ buy/sell พร้อมกัน

2. **กราฟราคาหุ้น** — เก็บ snapshot ราคาทุกครั้งที่ `simulateMarket()` ถูกเรียก แสดงเป็น line chart บนหน้า Stock Detail ด้วย ng2-charts

3. **Real-time update** — แทน "Simulate Market" ด้วย auto-simulate mode ผ่าน Server-Sent Events (SSE) หรือ WebSocket

4. **Authentication** — เพิ่ม Spring Security + JWT, เพิ่ม `userId` FK บน `portfolio_items` และ `transactions` รองรับ multi-user

5. **Frontend unit tests** — เขียน Jasmine/Karma tests สำหรับ Angular services และ component tests

6. **E2E tests** — เพิ่ม Cypress tests ครอบคลุม flow ซื้อ → portfolio update → transaction history → simulate market

7. **`ResourceNotFoundException`** — เพิ่ม custom exception สำหรับ 404 (stock not found, holding not found) แทนการคืน `null` หรือใช้ `IllegalArgumentException` ซึ่ง semantic ไม่ตรงกับ HTTP 404
