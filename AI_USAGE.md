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

1. **`flyway-database-postgresql` dependency** — AI เพิ่ม artifact นี้ซึ่งใช้ได้เฉพาะ Flyway 10.x แต่ Spring Boot 3.2 ใช้ Flyway 9.x ที่รองรับ PostgreSQL ใน `flyway-core` อยู่แล้ว ลบออก

2. **`UserState.id` type mismatch** — AI ใช้ Java `long` แต่ SQL schema ใช้ `INT` Hibernate 6 validate strict กว่า → ต้องเปลี่ยน SQL เป็น `BIGINT` และเพิ่ม V3 migration สำหรับ volume เก่า

3. **`maxBuyQty` คำนวณใน frontend → ย้ายไป backend** — AI draft คำนวณ `maxBuyQty = floor(cash / price)` ใน Angular component โดยตรง แก้เป็นการเรียก `POST /order/limits` และรับ `TradeLimitsResponse` จาก backend แทน เพราะ price อาจ update ระหว่างที่ drawer เปิดอยู่ทำให้ค่าใน frontend stale

4. **TradeLimits endpoint รับ path variable → request body** — AI generate `GET /order/limits/{symbol}` แต่เปลี่ยนเป็น `POST /order/limits` ที่รับ `SymbolRequest` body เพื่อให้ consistent กับ pattern ของโปรเจกต์ที่ส่งข้อมูลผ่าน payload เสมอ

5. **Stock detail endpoint รับ path variable → request body** — AI generate `GET /stocks/{symbol}` แต่เปลี่ยนเป็น `POST /stocks/detail` ที่รับ `SymbolRequest` body ด้วยเหตุผลเดียวกัน

6. **buyStock validation — cash amount → max quantity** — AI generate ตรวจ `cash < price * quantity` คืน "Insufficient cash" แต่แก้ให้คำนวณ `maxBuyQty = floor(cash / price)` แล้วตรวจ `quantity > maxBuyQty` คืน error ที่บอก max ที่ซื้อได้ชัดเจนกว่า และ reuse logic เดิมจาก `computeMaxBuyQty()`

7. **Drawer ไม่ปิดหลัง trade สำเร็จ** — AI generate ไม่ได้ปิด drawer หลัง buy/sell เพิ่ม `this.closeDrawer()` และ reset state ใน success callback ของ `confirmBuy()` และ `confirmSell()`

8. **ตกแต่ง UI ให้ตรงกับ style ที่ต้องการ** — AI generate หน้าตา UI เบื้องต้นออกมา แต่ปรับ layout, สี, spacing และ component ต่างๆ ด้วยตัวเองให้ตรงกับ design ที่ต้องการ เช่น การจัด card, สีตัวเลขกำไร/ขาดทุน และ responsive behavior

---

## 4. กระบวนการทบทวนและปรับโครงสร้างโค้ด

หลังจาก AI generate โค้ดออกมา ไม่ได้ใช้ทุกอย่างโดยตรง แต่ทำการทดลองรันจริงและนั่งอ่านโค้ดทีละส่วนก่อนนำไปใช้

**ขั้นตอนที่ทำ:**

1. **ทดลองรันและใช้งานจริง** — รัน backend + frontend แล้วทดสอบ flow ทั้งหมดด้วยตัวเอง: ซื้อหุ้น, ขายหุ้น, ดู portfolio, simulate market เพื่อดูว่า behavior ตรงกับ spec หรือไม่

2. **อ่านโค้ดทีละไฟล์** — อ่าน service, controller, component ทุกไฟล์ที่ AI generate เพื่อทำความเข้าใจ logic จริงๆ ไม่ใช่แค่เชื่อว่า AI เขียนถูก

3. **แยกส่วนที่จำเป็นออกจากส่วนที่ไม่จำเป็น** — ตัดโค้ดที่ AI generate แต่ไม่ได้ใช้จริงออก เช่น method ซ้ำ, import ที่ไม่ใช้, logic ที่ overly complex เกินกว่า requirement

4. **ปรับโครงสร้างให้ตรงกับ design ที่ต้องการ** — reformat และจัดโครงสร้าง component, service ให้ตรงกับ pattern ที่วางไว้ เช่น ปรับ dependency injection ให้ถูกทิศทาง, ย้าย logic ที่อยู่ผิดที่ไปไว้ใน layer ที่เหมาะสม

---

## 5. สิ่งที่จะปรับปรุงถ้ามีเวลาเพิ่ม

1. **กราฟราคาหุ้น** — เก็บ snapshot ราคาทุกครั้งที่ `simulateMarket()` ถูกเรียก แสดงเป็น line chart บนหน้า Stock Detail ด้วย ng2-charts

2. **Real-time update** — แทน "Simulate Market" ด้วย auto-simulate mode ผ่าน Server-Sent Events (SSE) หรือ WebSocket

3. **Authentication** — เพิ่ม Spring Security + JWT, เพิ่ม `userId` FK บน `portfolio_items` และ `transactions` รองรับ multi-user

4. **`ResourceNotFoundException`** — เพิ่ม custom exception สำหรับ 404 (stock not found, holding not found) แทนการใช้ `IllegalArgumentException` ซึ่ง semantic ไม่ตรงกับ HTTP 404
