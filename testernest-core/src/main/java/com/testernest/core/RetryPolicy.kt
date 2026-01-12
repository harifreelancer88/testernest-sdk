package com.testernest.core

class RetryPolicy(
    private val delaysMs: List<Long> = listOf(500, 1000),
    private val sleeper: (Long) -> Unit = { Thread.sleep(it) }
) {
    fun <T> run(action: () -> T, isSuccess: (T) -> Boolean): T {
        var result = action()
        if (isSuccess(result)) return result
        for (delay in delaysMs) {
            sleeper(delay)
            result = action()
            if (isSuccess(result)) return result
        }
        return result
    }
}
