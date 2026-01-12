package com.testernest.core

import org.junit.Assert.assertEquals
import org.junit.Test

class EventQueueTest {
    @Test
    fun addPeekRemove() {
        val queue = EventQueue()
        val event1 = EventPayload(
            name = "one",
            ts = 1,
            sessionId = "s1",
            testerId = null,
            screen = null,
            properties = null,
            packageName = "pkg",
            appVersion = "1.0",
            buildNumber = "1",
            platform = "android",
            deviceModel = "device",
            osVersion = "os"
        )
        val event2 = event1.copy(name = "two")

        queue.add(event1)
        queue.add(event2)

        assertEquals(2, queue.size())
        assertEquals(1, queue.peek(1).size)

        queue.remove(1)
        assertEquals(1, queue.size())
    }
}
