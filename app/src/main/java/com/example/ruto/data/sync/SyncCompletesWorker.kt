package com.example.ruto.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ruto.data.local.complete.PendingCompleteDao
import com.example.ruto.data.routine.RoutineApi
import com.example.ruto.domain.routine.CompleteItem
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncCompletesWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val dao: PendingCompleteDao,
    private val api: RoutineApi
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val batch = dao.loadBatch(limit = 50)
        if (batch.isEmpty()) return Result.success()

        return runCatching {
            val payload = batch.map { pendingComplete -> CompleteItem(
                routineId = pendingComplete.routineId,
                completedAt = pendingComplete.completedAtIso,
                opId = pendingComplete.opId
            ) }
            val resp = api.completeRoutinesBatch(payload)

            if (resp.ok) {
                dao.deleteByOpIds(batch.map { it.opId })
                Result.success()
            } else {
                Result.retry()
            }
        }.getOrElse {
            Result.retry()
        }
    }
}