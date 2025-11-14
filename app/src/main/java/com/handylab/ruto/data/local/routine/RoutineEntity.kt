package com.handylab.ruto.data.local.routine

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.handylab.ruto.domain.routine.RoutineCadence
import com.handylab.ruto.domain.routine.RoutineRead

@Entity(
    tableName = "routines_cached",
    indices = [Index(value = ["ownerKey", "id"], unique = true)]
)
data class RoutineEntity(
    @PrimaryKey val pk: String,
    val ownerKey: String,
    val id: String,
    val name: String,
    val cadence: String,          // DAILY/WEEKLY/MONTHLY/YEARLY
    val start_date: String,       // YYYY-MM-DD
    val end_date: String,         // YYYY-MM-DD
    val notify_enabled: Boolean,
    val notify_time: String?,     // HH:mm:ss or null
    val timezone: String,
    val tags_json: String,        // JSON-encoded ["tag","..."]
    val updatedAt: Long = System.currentTimeMillis()
)

// DB -> Domain
fun RoutineEntity.toDomain(): RoutineRead =
    RoutineRead(
        id = id,
        name = name,
        cadence = RoutineCadence.valueOf(cadence),
        startDate = start_date,
        endDate = end_date,
        notifyEnabled = notify_enabled,
        notifyTime = notify_time,
        timezone = timezone,
        tags = kotlinx.serialization.json.Json.decodeFromString(tags_json)
    )

// Domain -> DB (ownerKey 필요)
fun RoutineRead.toEntity(ownerKey: String): RoutineEntity =
    RoutineEntity(
        pk = "${ownerKey}#$id",
        ownerKey = ownerKey,
        id = id,
        name = name,
        cadence = cadence.name,
        start_date = startDate,
        end_date = endDate,
        notify_enabled = notifyEnabled,
        notify_time = notifyTime,
        timezone = timezone,
        tags_json = kotlinx.serialization.json.Json.encodeToString(tags)
    )