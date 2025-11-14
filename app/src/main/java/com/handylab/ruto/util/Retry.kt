package com.handylab.ruto.util

import kotlinx.coroutines.delay

suspend inline fun <T> withRetry(
    maxAttempts:Int = 3,
    initialDelayMs:Long = 400,
    factor: Double = 2.0,
    noinline shouldRetry: (Throwable) -> Boolean = {true},
    crossinline block: suspend () -> T
) : T {
    var attempt = 0
    var delayMs = initialDelayMs
    var last: Throwable? = null

    while (attempt< maxAttempts) {
        try {
            return block.invoke()
        } catch (t: Throwable) {
            last = t
            if (!shouldRetry(t) || attempt == maxAttempts -1) break
            delay(delayMs)
            delayMs = (delayMs * factor).toLong()
            attempt++
        }
    }
    throw last ?: IllegalStateException("Unknown exception")
}