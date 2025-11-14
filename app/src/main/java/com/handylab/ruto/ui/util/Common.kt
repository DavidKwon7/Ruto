package com.handylab.ruto.ui.util

import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
fun parseHHmm(hhmm: String?): Pair<Int, Int> {
    return try {
        if (hhmm.isNullOrBlank()) {
            val now = java.time.LocalTime.now()
            now.hour to now.minute
        } else {
            val parts = hhmm.split(":")
            parts[0].toInt() to parts[1].toInt()
        }
    } catch (_: Exception) {
        val now = java.time.LocalTime.now()
        now.hour to now.minute
    }
}

fun pad2(n: Int) = n.toString().padStart(2, '0')