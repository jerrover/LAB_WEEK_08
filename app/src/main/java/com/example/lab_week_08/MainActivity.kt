package com.example.lab_week_08

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.lab_week_08.worker.FirstWorker
import com.example.lab_week_08.worker.SecondWorker

class MainActivity : AppCompatActivity() {

    // Cara inisialisasi WorkManager yang lebih aman menggunakan lazy
    private val workManager by lazy {
        WorkManager.getInstance(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Cukup panggil layout

        // 1. Buat Constraints
        val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val id = "001"

        // 2. Buat Request untuk FirstWorker
        val firstRequest = OneTimeWorkRequest
            .Builder(FirstWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(FirstWorker.INPUT_DATA_ID, id))
            .build()

        // 3. Buat Request untuk SecondWorker
        val secondRequest = OneTimeWorkRequest
            .Builder(SecondWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(SecondWorker.INPUT_DATA_ID, id))
            .build()

        // 4. Rangkai dan jalankan Worker
        // Jalankan firstRequest, SETELAH ITU jalankan secondRequest
        workManager.beginWith(firstRequest)
            .then(secondRequest)
            .enqueue()

        // 5. Amati hasil dari FirstWorker
        workManager.getWorkInfoByIdLiveData(firstRequest.id)
            .observe(this) { info ->
                if (info != null && info.state.isFinished) {
                    showResult("First process is done")
                }
            }

        // 6. Amati hasil dari SecondWorker
        workManager.getWorkInfoByIdLiveData(secondRequest.id)
            .observe(this) { info ->
                if (info != null && info.state.isFinished) {
                    showResult("Second process is done")
                }
            }
    }

    // Build data input
    private fun getIdInputData(idKey: String, idValue: String) =
        Data.Builder()
            .putString(idKey, idValue)
            .build()

    // Menampilkan Toast
    private fun showResult(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}