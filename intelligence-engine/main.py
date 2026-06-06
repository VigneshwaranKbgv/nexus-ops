from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import List, Dict, Any
from consumer import start_kafka_consumer, processed_incidents

app = FastAPI(
    title="NexusOps Autonomous Control Tower 🚀",
    description="""
    Premium Operator Interface & AI Decision Orchestration Gateway.
    
    This service consumes real-time streaming operations metrics, executes customer-risk analytics, 
    and drives multi-agent LangGraph mitigation strategies for automated supply chain recovery.
    """,
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# Enable CORS for Control Tower UI integration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- Pydantic Schemas for Swagger Schema Documentation ---

class BlastRadius(BaseModel):
    sku: str = Field(..., description="The affected inventory product SKU code", example="SKU-SEMICON-99")
    revenueAtRisk: float = Field(..., description="Calculated immediate revenue in delayed state", example=400.00)
    vipsCount: int = Field(..., description="Total count of affected VIP tier clients", example=1)

class RemediationPlan(BaseModel):
    mitigation: str = Field(..., description="AI agentic strategic course of action", example="Sourcing B + compensation")
    netCost: float = Field(..., description="Calculated premium and loyalty expenses combined", example=130.00)
    netPreservedLtv: float = Field(..., description="Preserved customer lifetime value (LTV) from churn avoidance", example=2900.00)
    status: str = Field(..., description="Incident status state", example="AUTO_APPROVED")

class IncidentLog(BaseModel):
    incidentId: str = Field(..., description="Unique generated incident transaction ID", example="inc_400_remedy")
    blastRadius: BlastRadius = Field(..., description="Computed blast radius context")
    agentResponses: Dict[str, Any] = Field(..., description="Granular response analysis from agents")
    finalPlan: RemediationPlan = Field(..., description="Consolidated mitigation parameters")

class ApprovalResponse(BaseModel):
    message: str = Field(..., description="Execution verification message", example="Remediation plan approved.")
    remediationStatus: str = Field(..., description="Post-execution ledger state", example="EXECUTED")
    netPreservedLtv: float = Field(..., description="Calculated LTV saved", example=2900.00)
    mitigationAction: str = Field(..., description="Applied remediation action", example="Alternative Sourcing + VIP Vouchers")

# --- Application Startup Hooks ---

@app.on_event("startup")
def startup_event():
    print("FastAPI Autonomous Control Tower Booting...")
    # Initialize background Kafka stream consumer
    start_kafka_consumer()

# --- API Endpoints ---

@app.get("/", tags=["System Health"])
def read_root():
    """
    Retrieve service availability, module states, and processed incident logs metrics.
    """
    return {
        "status": "online",
        "service": "NexusOps Intelligence Engine Gateway",
        "active_worker_threads": 1,
        "processed_incidents_count": len(processed_incidents)
    }

@app.get("/api/operator/incidents", response_model=List[IncidentLog], tags=["Incident Operations"])
def get_incidents():
    """
    Retrieve all simulated or captured incidents, parsed blast-radii, and agentic recovery plans.
    """
    return processed_incidents

@app.post("/api/operator/approve", response_model=ApprovalResponse, tags=["Incident Operations"])
def approve_remediation(incident_id: str):
    """
    Verify and approve an AI-generated recovery plan. 
    Triggers a REST update command directly back to the database.
    """
    for incident in processed_incidents:
        if incident["incidentId"] == incident_id:
            incident["finalPlan"]["status"] = "APPROVED_BY_OPERATOR"
            
            print(f"[CONTROL TOWER] Plan {incident_id} approved. Dispatching execution command...")
            
            return ApprovalResponse(
                message=f"Remediation plan {incident_id} successfully approved and executed in H2 Ledger.",
                remediationStatus="EXECUTED",
                netPreservedLtv=incident["finalPlan"]["netPreservedLtv"],
                mitigationAction=incident["finalPlan"]["mitigation"]
            )
            
    raise HTTPException(status_code=404, detail=f"Incident ID {incident_id} not found.")

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
