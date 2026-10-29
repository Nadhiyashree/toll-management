# Toll Management System (FASTag) — Java CLI

A complete, production-grade, object-oriented **Toll Management System (FASTag)** built in Java with a menu-driven Command-Line Interface (CLI).

---

## 1. Project Architecture & Package Structure

The project strictly follows the requested modular design pattern where each domain entity has its own dedicated CLI, Model, Repository, and Service, with all related **enums embedded directly inside the models** (eliminating standalone enum classes):

```
d:/toll management/
├── README.md
├── src/
│   └── com/
│       └── toll/
│           ├── Main.java                        # System Entry Point & Main Menu Dispatcher
│           │
│           ├── cli/                             # Entity-specific CLI controllers
│           │   ├── AdminCLI.java                # Admin authentication & session management
│           │   ├── ConsoleReader.java           # Safe input parser & menu handler
│           │   ├── DisputeCLI.java              # Dispute lifecycle management
│           │   ├── FASTagCLI.java               # FASTag issuance, recharge & blacklisting
│           │   ├── MonthlyPassCLI.java          # Pass issuance & fee configuration
│           │   ├── PaymentCLI.java              # Payment logs & tracking
│           │   ├── ReportCLI.java               # Analytics, audit & revenue reports
│           │   ├── TollCrossingCLI.java         # Interactive Toll Booth crossing workflow
│           │   ├── TollPlazaCLI.java            # Toll Plaza & Lane configuration
│           │   ├── TollRateCLI.java             # Toll rate matrix & discount rules
│           │   └── VehicleCLI.java              # Vehicle registration & lookup
│           │
│           ├── models/                          # Domain entities (with embedded enums)
│           │   ├── Admin.java                   # (embeds enum UserRole: ADMIN, OPERATOR, USER)
│           │   ├── CrossingResult.java          # Receipt data structure for toll booth crossing
│           │   ├── Dispute.java                 # (embeds enum DisputeStatus: OPEN, UNDER_REVIEW, RESOLVED, REJECTED)
│           │   ├── FASTagAccount.java           # (embeds enum AccountStatus: ACTIVE, BLOCKED, CLOSED)
│           │   ├── Lane.java                    # (embeds enum LaneType: FASTAG, CASH, EMERGENCY & LaneStatus: OPEN, CLOSED)
│           │   ├── MonthlyPass.java             # Monthly pass model
│           │   ├── Payment.java                 # (embeds enum PaymentMethod: FASTAG, CASH, EXEMPT & PaymentStatus: SUCCESS, FAILED, PENDING)
│           │   ├── TollPlaza.java               # Plaza entity managing lanes
│           │   ├── TollRate.java                # (embeds enum TripType: SINGLE, RETURN)
│           │   ├── TollTransaction.java         # (embeds enum TransactionStatus: SUCCESS, FAILED, PENDING)
│           │   └── Vehicle.java                 # (embeds enum VehicleType: CAR, VAN, BUS, TRUCK, MULTI_AXLE, AMBULANCE, FIRE_ENGINE)
│           │
│           ├── repository/                      # In-memory storage & query repositories
│           │   ├── AdminRepository.java
│           │   ├── DisputeRepository.java
│           │   ├── FASTagAccountRepository.java
│           │   ├── MonthlyPassRepository.java
│           │   ├── PaymentRepository.java
│           │   ├── TollPlazaRepository.java
│           │   ├── TollRateRepository.java
│           │   ├── TollTransactionRepository.java
│           │   └── VehicleRepository.java
│           │
│           └── services/                        # Business logic, engines & exceptions
│               ├── AdminService.java
│               ├── DemoDataService.java         # Preloads realistic test dataset
│               ├── DisputeService.java
│               ├── EntityNotFoundException.java
│               ├── FASTagService.java
│               ├── IdGenerator.java
│               ├── InsufficientBalanceException.java
│               ├── InvalidOperationException.java
│               ├── LaneService.java
│               ├── MonthlyPassService.java
│               ├── PaymentService.java
│               ├── ReportService.java
│               ├── TollCalculator.java          # Half-Up rounding to nearest Rs. 5 & discounts
│               ├── TollPlazaService.java
│               ├── TollProcessingService.java   # Core vehicle crossing engine & barrier control
│               └── VehicleService.java
```

---

## 2. Compilation and Execution

### Prerequisites
- Java JDK 17 or higher (tested on Java 27).

### Running the application:
You can run `Main.java` directly using `java`:
```powershell
java -cp src com.toll.Main
```
*(Or compile whenever needed)*:
```powershell
javac -d . (Get-ChildItem -Recurse -Filter *.java src).FullName
```

---

## 3. Implemented Business Rules Summary

### Module 1: Toll Plaza and Lane Setup
- **Admin Access Only**: Plazas and lanes can only be configured by an authenticated Administrator.
- **Operational Requirements**: A plaza must have at least **two lanes** and valid rates configured for all supported vehicle types (`CAR`, `VAN`, `BUS`, `TRUCK`, `MULTI_AXLE`) before it can be activated.
- **Lane Closure Invariant**: A lane cannot be closed while a vehicle is actively being processed in it.
- **Unique Lane Numbers**: Duplicate lane numbers within the same plaza are strictly rejected.

### Module 2: Vehicle & FASTag Accounts
- **One Active Tag per Vehicle**: Each FASTag belongs to exactly one vehicle, and each vehicle can have at most one active tag.
- **Recharge Rules**:
  - Minimum recharge: **Rs. 100**
  - Maximum single recharge: **Rs. 10,000**
  - Maximum balance limit: **Rs. 50,000** (never negative).
- **Auto-Blocking**: If an account balance falls below **Rs. 100**, status automatically becomes `BLOCKED`.
- **Auto-Reactivation**: If a recharge brings balance back to **>= Rs. 100**, a `BLOCKED` account is automatically reactivated.
- **Blacklisting**: An Admin can blacklist any tag. Blacklisted tags are rejected at all toll plazas. Closed accounts cannot be recharged.

### Module 3: Toll Processing & Barrier Control
- **Atomic Processing**: Balance deduction and transaction recording are atomic; failed deductions never open the barrier.
- **FASTAG Lane**:
  - Requires active, non-blacklisted tag with sufficient balance.
  - If insufficient balance: barrier stays closed, attempt recorded as `FAILED`, and vehicle is redirected to a `CASH` lane (pays normal cash toll).
  - If no valid tag: redirected to `CASH` lane (pays 2x double toll penalty).
- **CASH Lane**:
  - Vehicles with invalid/no tag pay **double the normal toll**.
  - Vehicles redirected due to low balance pay the **normal cash toll**.
- **EMERGENCY Lane**:
  - Dedicated strictly to ambulances and fire engines.
  - Emergency vehicles pay **Rs. 0 (Exempt)** and barrier opens. Ordinary vehicles attempting to use this lane are blocked.
- **Duplicate Crossing Restriction**:
  - A vehicle/tag cannot be charged twice at the same plaza within **2 minutes**. Enforced using timestamp auditing.
- **Vehicle Type Mismatch**:
  - If the observed type differs from the registered vehicle type, the **higher applicable toll** is charged and the transaction is automatically flagged for manual review.

### Module 4: Rates, Discounts, and Monthly Passes
- **Return Trip Discount**: If a vehicle returns through the same plaza within **24 hours** of a qualifying SINGLE crossing, it pays the RETURN rate (**50% of the single rate**). A return discount can only be claimed once per qualifying trip.
- **Rounding Policy**: All payable toll amounts are rounded to the nearest multiple of **Rs. 5** using `RoundingMode.HALF_UP` (documented in `TollCalculator.java`).
- **Monthly Passes**: Exempts the vehicle from toll at the designated plaza during the validity period. Overlapping active passes for the same vehicle and plaza are prohibited.

### Module 5: Payments, Disputes, and Reports
- **Payments**: Linked 1:1 with transactions. Prevents duplicate payments.
- **Disputes**: Authorized users can raise disputes against transactions. Admins review, resolve, or reject disputes. Original records are preserved.
- **Comprehensive Reports**:
  1. Crossings & Revenue Summary (filtered by plaza and date range).
  2. Electronic FASTag vs Manual Cash collection breakdown.
  3. Audit of Insufficient Balance, Blacklisted tag attempts, Duplicate crossing violations, and Vehicle type mismatches.
  4. Active vs Expired Monthly Passes.
  5. Disputes Summary (OPEN, UNDER_REVIEW, RESOLVED, REJECTED).

---

## 4. Preloaded Demonstration Dataset

To allow instant testing, the application initializes with a realistic demo dataset:

| Entity | Details |
|---|---|
| **Default Admin** | Username: `admin`, Password: `admin123` |
| **Plaza 1** | `PLZ-101`: *Sriperumbudur Toll Plaza* (NH-48) — Lanes 1 & 2 (FASTAG), Lane 3 (CASH), Lane 4 (EMERGENCY) [Operational] |
| **Plaza 2** | `PLZ-102`: *Kanchipuram Toll Plaza* (NH-48) — Lanes 1 (FASTAG), 2 (CASH), 3 (EMERGENCY) [Operational] |
| **Vehicle 1** | `TN01AB1234` (CAR) — Tag `FT-100001`, Balance: Rs. 1,500.00 (`ACTIVE`) |
| **Vehicle 2** | `TN02CD5678` (CAR) — Tag `FT-100002`, Balance: Rs. 40.00 (`BLOCKED` due to low balance) |
| **Vehicle 3** | `KA03EF9012` (TRUCK) — Tag `FT-100003`, Balance: Rs. 5,000.00 (`ACTIVE`) |
| **Vehicle 4** | `TN04GH3456` (VAN) — Tag `FT-100004`, Balance: Rs. 2,500.00 (`BLACKLISTED` by Admin) |
| **Vehicle 5** | `TN05IJ7890` (BUS) — Tag `FT-100005`, Active Monthly Pass for Sriperumbudur Plaza |
| **Vehicle 6** | `TN09EM9999` (AMBULANCE) — Dedicated Emergency Vehicle |
| **Vehicle 7** | `TN07KL1111` (CAR) — Registered without FASTag (tests Cash Lane 2x penalty) |
