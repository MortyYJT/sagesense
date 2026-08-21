package com.mortyyjt.sagesense.network

import retrofit2.HttpException

/**
 * User-safe failures from the Agent endpoint.
 *
 * The backend response body is deliberately not carried here: it may contain
 * implementation details and should never be shown to an older user.
 */
sealed interface AgentFailure {
    data class RateLimited(val retryAfterSeconds: Int) : AgentFailure

    data object Unavailable : AgentFailure
}

private const val DEFAULT_RETRY_AFTER_SECONDS = 30
private const val MAX_RETRY_AFTER_SECONDS = 300

/**
 * Maps an HTTP result to a safe, deterministic client failure.
 *
 * Retry-After is intentionally limited to a numeric seconds value. Invalid,
 * missing, or non-positive values use a short safe default; very large values
 * are capped so a server header cannot make the UI promise an unreasonable
 * wait. This is a pure function and does not retry the request.
 */
fun mapAgentFailure(statusCode: Int?, retryAfterHeader: String?): AgentFailure {
    if (statusCode != 429) return AgentFailure.Unavailable

    val retryAfterSeconds = retryAfterHeader
        ?.trim()
        ?.toLongOrNull()
        ?.takeIf { it > 0 }
        ?.coerceAtMost(MAX_RETRY_AFTER_SECONDS.toLong())
        ?.toInt()
        ?: DEFAULT_RETRY_AFTER_SECONDS

    return AgentFailure.RateLimited(retryAfterSeconds)
}

/** Converts Retrofit failures without exposing the response body to the UI. */
fun mapAgentFailure(throwable: Throwable): AgentFailure = when (throwable) {
    is HttpException -> {
        val response = throwable.response()
        mapAgentFailure(response?.code(), response?.headers()?.get("Retry-After"))
    }

    else -> AgentFailure.Unavailable
}

fun agentFailureMessage(failure: AgentFailure, locale: String): String = when (failure) {
    is AgentFailure.RateLimited -> if (locale == "zh-CN") {
        "请求太频繁，请等待约 ${failure.retryAfterSeconds} 秒后再试；本地风险检测仍可用"
    } else {
        "Too many requests. Please wait about ${failure.retryAfterSeconds} seconds before trying again. Local risk detection is still available."
    }

    AgentFailure.Unavailable -> if (locale == "zh-CN") {
        "无法连接 Agent。风险判断仍在本机完成，请稍后再试。"
    } else {
        "The advisor could not be reached. Local risk detection is still working. Please try again."
    }
}
