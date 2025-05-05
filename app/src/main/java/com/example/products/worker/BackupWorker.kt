package com.example.products.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.products.data.AuthRepository
import com.example.products.data.BackupResult
import java.time.Duration

class BackupWorker(
    val context: Context,
    params: WorkerParameters
) :
    CoroutineWorker(context, params) {
    companion object {
        private val uniqueWorkerName = BackupWorker::class.java.simpleName
        private lateinit var authRepo: AuthRepository

        fun enqueue(context: Context, authRepository: AuthRepository) {
            authRepo = authRepository
            val manager = WorkManager.getInstance(context)
            val constraints =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val requestBuilder =
                PeriodicWorkRequestBuilder<BackupWorker>(Duration.ofHours(1)).setConstraints(
                    constraints
                )

            manager.enqueueUniquePeriodicWork(
                uniqueWorkerName,
                ExistingPeriodicWorkPolicy.KEEP,
                requestBuilder.build()
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkerName)
        }
    }

    override suspend fun doWork(): Result {
        var finalResult: BackupResult = BackupResult.Loading

        authRepo.backup(context).collect { result ->
            finalResult = result
        }

        return when (finalResult) {
            is BackupResult.Success -> Result.success()

            is BackupResult.Error -> {
                if (runAttemptCount < 10) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }

            BackupResult.Loading -> TODO()
        }
    }
}