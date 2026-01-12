package com.testernest.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RetryPolicyTest {
    @Test
    fun retriesUntilSuccess() {
        var attempts = 0
        val policy = RetryPolicy(delaysMs = listOf(1, 1)) { }

        val result = policy.run(
            action = {
                attempts += 1
                NetworkResult(success = attempts == 3, isAuthError = false)
            },
            isSuccess = { it.success }
        )

        assertEquals(3, attempts)
        assertTrue(result.success)
    }
}
