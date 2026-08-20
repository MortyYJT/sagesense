from __future__ import annotations

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from backend.app.agent import AgentService, MODEL
from backend.app.schemas import AgentQueryRequest, AgentQueryResponse, HealthResponse


app = FastAPI(
    title="SageSense Agent API",
    version="0.1.0",
    description="Privacy-conscious, citation-backed scam explanations for the SageSense Android prototype.",
)
app.add_middleware(
    CORSMiddleware,
    allow_origins=[],
    allow_credentials=False,
    allow_methods=["GET", "POST"],
    allow_headers=["Content-Type"],
)

agent_service = AgentService()


@app.get("/", include_in_schema=False)
async def root() -> dict[str, str]:
    return {"service": "sagesense-agent", "docs": "/docs"}


@app.get("/v1/health", response_model=HealthResponse)
async def health() -> HealthResponse:
    return HealthResponse(model=MODEL, model_configured=bool(agent_service.api_key))


@app.post("/v1/agent/query", response_model=AgentQueryResponse)
async def query_agent(request: AgentQueryRequest) -> AgentQueryResponse:
    return await agent_service.answer(request)
