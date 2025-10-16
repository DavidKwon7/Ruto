package com.example.ruto.data.sync

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ruto.data.local.complete.PendingComplete
import com.example.ruto.data.local.complete.PendingCompleteDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompleteQueue @Inject constructor(
    private val dao: PendingCompleteDao,
    private val wm: WorkManager
) {
    suspend fun enqueue(routineId: String, completedAt: Instant) {
        val opId = UUID.randomUUID().toString()
        val item = PendingComplete(
            opId = opId,
            routineId = routineId,
            completedAtIso = completedAt.toString() // Instant -> ISO-8601 UTC
        )
        withContext(Dispatchers.IO) { dao.upsert(item) }

        // 네트워크 연결 시 자동 전송
        val req = OneTimeWorkRequestBuilder<SyncCompletesWorker>()
    }
}