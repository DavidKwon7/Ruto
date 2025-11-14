package com.handylab.ruto.workManager

import android.app.Application
import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.handylab.ruto.data.sync.SyncCompletesWorker
import dagger.hilt.EntryPoints

class AppWorkerFactory(
    private val appContext: Context
) : WorkerFactory() {
    override fun createWorker(
        context: Context,
        workerClassName: String,
        params: WorkerParameters
    ): ListenableWorker? {
        android.util.Log.d("AppWorkerFactory", "createWorker: $workerClassName")

        val app = appContext.applicationContext as Application
        val deps = EntryPoints.get(app, WorkerDepsEntryPoint::class.java)

        return when (workerClassName) {
            SyncCompletesWorker::class.java.name -> {
                SyncCompletesWorker(
                    context,
                    params,
                    deps.pendingCompleteDao(),
                    deps.routineApi(),
                    deps.appLogger()
                )
            }
            else -> null
        }
    }

}