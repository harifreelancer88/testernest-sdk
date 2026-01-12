package com.testernest.core

class EventQueue {
    private val lock = Any()
    private val events = ArrayList<EventPayload>()

    fun add(event: EventPayload) {
        synchronized(lock) {
            events.add(event)
        }
    }

    fun size(): Int = synchronized(lock) { events.size }

    fun peek(max: Int): List<EventPayload> = synchronized(lock) { events.take(max).toList() }

    fun remove(count: Int) {
        synchronized(lock) {
            val toRemove = minOf(count, events.size)
            repeat(toRemove) { events.removeAt(0) }
        }
    }
}
