package com.example.ruto.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ruto.data.local.complete.PendingCompleteDao
import com.example.ruto.data.routine.RoutineApi
import com.example.ruto.domain.routine.CompleteItem
import com.example.ruto.util.AppLogger

class SyncCompletesWorker(
    appContext: Context,
    params: WorkerParameters,
    private val dao: PendingCompleteDao,
    private val api: RoutineApi,
    private val logger: AppLogger
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
            logger.d("SyncCompletesWorker", "resp: ok=${resp.ok}, code=${resp.processed}, body=${resp.ok}")

            if (resp.ok) {
                dao.deleteByOpIds(batch.map { it.opId })
                Result.success()
            } else {
                Result.retry()
            }
        }.getOrElse { e ->
            Log.e("SyncCompletesWorker", "error", e)
            Result.retry()
        }
    }
}