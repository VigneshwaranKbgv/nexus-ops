# NexusOps 🚀: Autonomous Event-Driven Marketplace Operations Engine

**NexusOps** is a production-grade **Autonomous Operations Engine** designed to manage an e-commerce platform's core transactional lifecycle (Orders, Invoices, Inventory, and Churn Prevention).

Rather than relying on brittle, slow LLM wrappers, the system segregates operations into **Deterministic Transaction Execution** (Java Spring Boot) and **Multi-Agent Predictive & Generative Planning** (Python FastAPI + LangGraph), decoupled asynchronously via an event-driven stream (**Apache Kafka**).

---

## 🏗️ Architectural Topology

```text
                                [ Asynchronous Event Bus ]
                                      Apache Kafka
                                    (KRaft Sandbox)
                                           ▲
                                           │ (Event Publish)
  ┌─────────────────────────┐              │              ┌─────────────────────────┐
  │   Enterprise Ledger     ├──────────────┘              │   Intelligence Engine   │
  │     (Java Spring)       │                             │     (Python FastAPI)    │
  │                         │◄────────────────────────────┤                         │
  │  - H2 In-Memory DB      │        (REST Query)         │  - Background Consumer  │
  │  - Transaction Safety   │                             │  - Churn Risk Analytics │
  │  - Atomic Schema        │                             │  - LangGraph Orches.    │
  └─────────────────────────┘                             └─────────────────────────┘
```

The system uses a **Zero-Install, Isolated Architecture** tailored specifically for local or sandboxed office execution:
*   **Database:** Local H2 database running in-memory with automatic schema instantiation.
*   **Stream Bus:** Programmatic, in-memory Embedded Kafka Broker auto-booted on Port `9092`.
*   **Decoupled Intelligence:** Python background thread capturing Kafka logs, running predictive analytics via REST queries, and executing agentic remedies.

---

## 🛠️ The 3-Agent LangGraph Remediation Machine

When a supply delay occurs, the system triggers three specialized agents to collaborate on a plan:
1.  **`CustomerAgent`**: Identifies affected high-risk VIP accounts (Churn Risk > 70%) and recommends compensatory credit loyalty vouchers (e.g., $50).
2.  **`OperationsAgent`**: Recommends immediate alternative sourcing (e.g., localized Supplier B at a 20% price markup) to bypass shipping delays.
3.  **`FinanceAgent`**: Computes cost parameters vs. preserved Lifetime Value (LTV) to assess financial ROI (POSITIVE/NEGATIVE).

---

## 🔌 API Command Center & Verification

### 1. Spring Boot Ledger Services (Port `8085`)

*   **Retrieve Seeded Customers:**
    ```powershell
    Invoke-RestMethod -Uri "http://localhost:8085/api/ledger/accounts" -Method Get
    ```
*   **Retrieve Warehouse Inventory SKUs:**
    ```powershell
    Invoke-RestMethod -Uri "http://localhost:8085/api/ledger/inventory" -Method Get
    ```
*   **Place a Purchase Order:**
    ```powershell
    Invoke-RestMethod -Uri "http://localhost:8085/api/ledger/orders" -Method Post -ContentType "application/json" -Body '{"email":"vip_customer@gmail.com", "sku":"SKU-SEMICON-99", "quantity":2}'
    ```
*   **Simulate a Supplier Delay Event:**
    ```powershell
    Invoke-RestMethod -Uri "http://localhost:8085/api/ledger/supplier/delay?supplierId=spl_logitech_01&affectedSku=SKU-SEMICON-99&delayDays=14" -Method Post
    ```

### 2. FastAPI Operator Control Tower (Port `8000`)

*   **Fetch Active AI-Generated Plans:**
    ```powershell
    (Invoke-RestMethod -Uri "http://localhost:8000/api/operator/incidents" -Method Get) | ConvertTo-Json -Depth 5
    ```
*   **Execute Operator Approval Sign-Off:**
    ```powershell
    Invoke-RestMethod -Uri "http://localhost:8000/api/operator/approve?incident_id=inc_400_remedy" -Method Post
    ```

---

## 📂 Project Repository Structure

```text
Nexus Ops/
├── docker-compose.yml
├── ledger-core/                 # Java Spring Boot 2.7.x Core
│   ├── pom.xml
│   ├── mvnw.cmd                 # Zero-install Maven wrapper
│   └── src/main/java/com/nexusops/ledger/
│       ├── config/              # Embedded Kafka broker settings
│       ├── controller/          # Core Ledger REST APIs
│       ├── model/               # JPA schema entities (Account, Inventory, etc.)
│       └── service/             # Database seeders and Kafka publishers
└── intelligence-engine/         # Python FastAPI Service
    ├── main.py                  # API endpoints and background consumer
    ├── analytics.py             # Layer 2: Blast-radius enrichment calculator
    └── orchestrator.py          # Layer 3: 3-Agent LangGraph planning machine
```
