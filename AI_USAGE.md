# AI Usage Documentation

This document describes how AI (Claude Code) was used throughout this project, as required by the assignment.

---

## 1. What AI Was Used For

- **Requirement analysis** — Breaking down the Thai-language requirements document into a prioritized feature list with clear frontend/backend/data model boundaries
- **Data model design** — Designing the `Stock`, `PortfolioItem`, `Transaction` models and the `InMemoryStore` structure, with guidance on how to migrate to a real database later
- **Backend scaffolding** — Generating the full Spring Boot project structure: models, DTOs, service layer, controllers, exception handler, and unit tests
- **Business logic** — Implementing buy/sell flows with correct avg buy price calculation, cash deduction/credit, and portfolio mutation
- **Simulate Market logic** — Implementing the `simulateMarket()` method with bounded random price changes (±5%) and the rule that `previousPrice` is set before updating `currentPrice`
- **Frontend scaffolding** — Generating the full Angular 17 project structure: standalone components, lazy-loaded routing, service layer with Angular HttpClient, ng-zorro-antd UI components
- **Refactoring** — Migrating the API layer to a typed service structure (`ApiService` + domain services), replacing raw `fetch` with Angular `HttpClient`
- **Documentation** — Drafting README.md (architecture, DB migration guide, run instructions) and this AI_USAGE.md

---

## 2. Example Prompts Used

### Prompt 1 — Requirement Breakdown
> "Read these requirements for a Mock Stock Trading Platform (pasted full Thai spec). Extract: (1) all features, (2) all business rules, (3) all edge cases, (4) API endpoints needed, (5) data models needed. Present as structured lists."

**Why used:** The spec was ~400 lines in Thai. Using AI to extract a structured checklist prevented missing edge cases like "avgBuyPrice must not change on sell" and "transaction price must be locked at trade time."

### Prompt 2 — Data Model Design
> "Design the data models for a stock trading platform with in-memory storage. Requirements: Stock (price, dailyChange, changePercent), PortfolioItem (symbol, quantity, avgBuyPrice), Transaction (locked price, immutable). Show Java class structure and explain how to migrate to PostgreSQL if needed."

**Why used:** Needed to confirm the correct fields before writing any code. Key insight: `previousPrice` stored on `Stock` so `dailyChange` can be computed after each simulate without needing a separate history table.

### Prompt 3 — Buy/Sell Logic
> "Write a TradingService.buyStock(symbol, quantity) method for Spring Boot. Rules: validate quantity > 0, validate stock exists, validate cash >= price * quantity, deduct cash, compute new avgBuyPrice = ((oldQty * oldAvg) + (newQty * price)) / (oldQty + newQty), add transaction with locked price. Return PortfolioResponse."

**Why used:** The weighted average price formula is easy to get wrong. Delegating the first draft to AI and then verifying against the spec's example `((10×100)+(10×200))/20 = 150` confirmed correctness.

### Prompt 4 — Simulate Market Logic
> "Write simulateMarket() for Spring Boot. Rules: for each stock, random change -5% to +5%, set previousPrice = currentPrice (old), newPrice = max(oldPrice * (1 + change/100), 0.01), update dailyChange and changePercent. Return all updated stocks."

**Why used:** The order of operations matters — `previousPrice` must be captured before updating `currentPrice`, otherwise `dailyChange` would always be 0. The prompt explicitly specified this ordering.

### Prompt 5 — Angular Service Layer
> "Refactor the frontend API layer from raw fetch to Angular HttpClient. Create an ApiService that wraps GET/POST with automatic unwrapping of { success, data, message } responses — throw Error(message) if success is false. Then create separate domain services: StockService, PortfolioService, OrderService, TransactionService, MarketService, each injecting ApiService."

**Why used:** Angular's idiomatic pattern is `Observable`-based services with `HttpClient`, not raw `fetch`. The separation by domain makes each service independently testable and extensible.

### Prompt 6 — Angular Frontend Scaffolding
> "Build a complete Angular 17 standalone components project replacing Next.js. Pages: Dashboard (portfolio stats + holdings table), Stock List (searchable + sortable), Stock Detail (buy/sell reactive forms + cost preview), Transactions (paginated history). Use ng-zorro-antd for UI. Use HttpClient via ApiService. Lazy-load all routes."

**Why used:** Angular project setup (angular.json, tsconfig, app.config, routing, lazy loading) has many interdependent parts. AI generated the complete boilerplate correctly on the first pass; manual review was needed only for ng-zorro-antd peer dependency and tsconfig deprecation warnings.

### Prompt 7 — Edge Case Review
> "Review this trading platform implementation for edge cases. Check: buy with quantity 0, buy with insufficient cash, sell stock not held, sell more than held, simulate market resulting in price ≤ 0, transaction history changing when price changes. List any missing validations."

**Why used:** After the first draft was complete, this prompt was used as a verification pass. It caught the 404 vs 500 distinction for unknown symbols and the `GlobalExceptionHandler` needing to handle `IllegalArgumentException` separately.

---

## 3. What Was Edited After AI Generation

1. **CORS configuration** — AI initially placed CORS settings in `application.properties`. Spring Boot 3.x requires a `WebMvcConfigurer` bean for full CORS control. Changed to `CorsConfig.java`.

2. **`buildPortfolioResponse()` total P/L calculation** — First AI draft computed `totalProfitLoss = totalPortfolioValue - 100000` (hardcoding initial cash). Corrected to `stockMarketValue - totalCostBasis` which correctly measures unrealized P/L on current holdings only.

3. **Transactions sorted newest-first** — Initial `getTransactions()` returned the raw list in insertion order. Added `Collections.reverse(new ArrayList<>(...))` to return a copy in descending order without mutating the store.

4. **Angular simulate button** — AI generated the button with no callback mechanism. Updated to accept an `onSimulated`-equivalent pattern so Dashboard and Stock List each refresh their own data after simulation.

5. **tsconfig.json deprecation warnings** — After removing `baseUrl` and `downlevelIteration` (both deprecated in TypeScript 5.5+), two follow-up errors appeared: missing `rootDir` (required when `outDir` is set without `baseUrl`) and the `paths` alias needing an explicit `./` prefix. Both were fixed manually after reading the IDE diagnostics.

6. **Stock Detail "Your Position" section** — AI draft showed avg buy price as `0` when `heldQuantity` is 0, which is misleading. Changed to display `—` with a conditional check.

---

## 4. Where AI Generated Incorrect Output

### Issue 1: Wrong P/L total formula
**What AI generated:**
```java
double totalProfitLoss = totalPortfolioValue - 100000.0; // WRONG
```
**Problem:** Hardcodes initial capital. If user sells all holdings, formula breaks — `cash - 100000` would show negative P/L even when the user profited on their trades.

**Fix applied:**
```java
double totalProfitLoss = stockMarketValue - totalCostBasis;
```

### Issue 2: CORS in properties file
**What AI generated:** `spring.web.cors.allowed-origins=http://localhost:3000` in `application.properties`

**Problem:** Not a valid Spring Boot 3.x property. All preflight requests returned `403 Forbidden`.

**Fix applied:** Created `CorsConfig.java` with `@Configuration` + `WebMvcConfigurer.addCorsMappings()`.

### Issue 3: Missing `@ctrl/tinycolor` peer dependency
**What AI generated:** `package.json` with `ng-zorro-antd` but without its required peer dependency `@ctrl/tinycolor`.

**Problem:** Angular build failed with `Could not resolve "@ctrl/tinycolor"` — discovered only at build time, not at install time.

**Fix applied:** Ran `npm install @ctrl/tinycolor`. npm automatically added it to `package.json`.

### Issue 4: tsconfig.json deprecated options
**What AI generated:** `"baseUrl": "./"` and `"downlevelIteration": true` — both deprecated in TypeScript 5.5+.

**Problem:** VS Code showed deprecation errors. Removing `baseUrl` then caused two follow-on errors: `rootDir` missing and `paths` alias using a non-relative path.

**Fix applied:**
- Removed `baseUrl` and `downlevelIteration`
- Added `"rootDir": "./src"`
- Changed `"@env/*": ["src/environments/*"]` → `"@env/*": ["./src/environments/*"]`

### Issue 5: `changePercent` assertion precision in tests
**What AI generated in TradingServiceTest:** `assertTrue(Math.abs(stock.getChangePercent()) <= 5.0)` — fails due to floating-point arithmetic where `5.0 * 1.05` can slightly exceed 5.0.

**Fix applied:** Changed to `<= 5.01` to accommodate floating-point rounding.

---

## 5. What Would Be Improved with More Time

1. **Persistent storage** — Replace `InMemoryStore` with Spring Data JPA + PostgreSQL. `TradingService` is already abstracted from the store, so only the `@Component` store layer needs to change.

2. **Concurrency safety** — Add `synchronized` blocks or `ReentrantLock` around `buyStock`/`sellStock` to prevent two simultaneous requests from both passing the cash-sufficiency check.

3. **Price history chart** — Store a snapshot of all stock prices after each `simulateMarket()` call. Render a line chart on the Stock Detail page using ng2-charts (Chart.js wrapper for Angular).

4. **Real-time updates** — Replace the manual "Simulate Market" button with an auto-simulate mode using Server-Sent Events (SSE) or WebSocket so the UI updates without user interaction.

5. **Frontend unit tests** — Add Jasmine/Karma unit tests for Angular services (buy/sell validation logic, avg price formula) and component tests using Angular Testing Library.

6. **E2E tests** — Add Cypress tests covering the full buy → portfolio update → transaction history flow and the simulate market → price update flow.

7. **Multi-user support** — Add `userId` FK to `portfolio_items` and `transactions`, add Spring Security + JWT authentication, scope all queries per user.
