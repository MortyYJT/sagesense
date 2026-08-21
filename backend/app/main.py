from __future__ import annotations

from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware

from backend.app.agent import AgentService
from backend.app.guardrails import InMemoryRateLimiter, request_client_key
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
# Best-effort per-process protection for the prototype.  Durable multi-instance
# enforcement belongs in Vercel WAF; no APK-provided install ID is trusted here.
rate_limiter = InMemoryRateLimiter(max_requests=8, window_seconds=60.0, max_concurrent=2)


@app.get("/", include_in_schema=False)
async def root() -> dict[str, str]:
    return {"service": "sagesense-agent", "docs": "/docs"}


@app.get("/v1/health", response_model=HealthResponse)
async def health() -> HealthResponse:
    return HealthResponse(model=agent_service.model, model_configured=bool(agent_service.api_key))


@app.post("/v1/agent/query", response_model=AgentQueryResponse)
async def query_agent(payload: AgentQueryRequest, request: Request) -> AgentQueryResponse:
    reservation = rate_limiter.acquire(request_client_key(request))
    if not reservation.allowed:
        raise HTTPException(
            status_code=429,
            detail="Too many agent requests; please retry later.",
            headers={"Retry-After": str(reservation.retry_after)},
        )
    try:
        return await agent_service.answer(payload)
    finally:
        rate_limiter.release(reservation)
