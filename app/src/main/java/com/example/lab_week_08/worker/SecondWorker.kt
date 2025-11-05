package com.example.lab_week_08.worker

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class SecondWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    companion object {
        const val INPUT_DATA_ID = "INPUT_DATA_ID"
        private const val TAG = "SecondWorker"
    }

    override fun doWork(): Result {
        return try {
            val id = inputData.getString(INPUT_DATA_ID)
            Log.d(TAG, "doWork: Second worker process for ID $id started")

            // Simulasi proses
            Thread.sleep(3000)

            Log.d(TAG, "doWork: Second worker process for ID $id done")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "doWork: Error processing second worker", e)
            Result.failure()
        }
    }
}