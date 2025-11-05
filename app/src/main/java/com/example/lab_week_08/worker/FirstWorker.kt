package com.example.lab_week_08.worker

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class FirstWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    companion object {
        const val INPUT_DATA_ID = "INPUT_DATA_ID"
        private const val TAG = "FirstWorker"
    }

    override fun doWork(): Result {
        return try {
            // Mengambil input data
            val id = inputData.getString(INPUT_DATA_ID)
            Log.d(TAG, "doWork: First worker process for ID $id started")

            // Simulasi proses yang berjalan
            Thread.sleep(5000) // 5 detik

            Log.d(TAG, "doWork: First worker process for ID $id done")

            // Mengirim output data (jika diperlukan)
            val outputData = Data.Builder()
                .putString("OUTPUT_DATA_MESSAGE", "First worker process finished")
                .build()

            Result.success(outputData)
        } catch (e: Exception) {
            Log.e(TAG, "doWork: Error processing first worker", e)
            Result.failure()
        }
    }
}