package com.example.ruto.util

import android.os.Build
import androidx.annotation.RequiresApi

/**
 * 문자열 "YYYY-MM-DD" ↔ EpochMillis 변환
 */
@RequiresApi(Build.VERSION_CODES.O)
fun parseYYYYMMDD(value: String?): java.time.LocalDate? = try {
    if (value.isNullOrBlank()) null
    else java.time.LocalDate.parse(value, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
} catch (_: Exception) { null }

@RequiresApi(Build.VERSION_CODES.O)
fun java.time.LocalDate.toEpochMillis(zoneId: java.time.ZoneId = java.time.ZoneId.systemDefault()): Long =
    this.atStartOfDay(zoneId).toInstant().toEpochMilli()

@RequiresApi(Build.VERSION_CODES.O)
fun epochMillisToYYYYMMDD(millis: Long, zoneId: java.time.ZoneId = java.time.ZoneId.systemDefault()): String =
    java.time.Instant.ofEpochMilli(millis)
        .atZone(zoneId)
        .toLocalDate()
        .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
