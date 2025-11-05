package com.example.lab_week_08

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
// import com.example.lab_week_08.NotificationService.Companion.EXTRA_ID
import com.example.lab_week_08.worker.FirstWorker
import com.example.lab_week_08.worker.SecondWorker
import com.example.lab_week_08.worker.ThirdWorker
import com.example.lab_week_08.SecondNotificationService

class MainActivity : AppCompatActivity() {

    private val workManager by lazy {
        WorkManager.getInstance(this.applicationContext)
    }

    private val networkConstraints by lazy {
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }
    private lateinit var thirdRequest: OneTimeWorkRequest
    private val id2 = "002" // ID untuk service kedua

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v,
                                                                             insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left, systemBars.top, systemBars.right,
                systemBars.bottom
            )
            insets
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        val id = "001"

        val firstRequest = OneTimeWorkRequest
            .Builder(FirstWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(FirstWorker.INPUT_DATA_ID, id))
            .build()

        val secondRequest = OneTimeWorkRequest
            .Builder(SecondWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(SecondWorker.INPUT_DATA_ID, id))
            .build()

        thirdRequest = OneTimeWorkRequest
            .Builder(ThirdWorker::class.java)
            .setConstraints(networkConstraints) // Gunakan networkConstraints milik kelas
            .setInputData(getIdInputData(ThirdWorker.INPUT_DATA_ID, id2))
            .build()

        // 1. FirstWorker executed
        // 2. SecondWorker executed
        workManager.beginWith(firstRequest)
            .then(secondRequest)
            .enqueue()

        workManager.getWorkInfoByIdLiveData(firstRequest.id)
            .observe(this) { info ->
                if (info != null && info.state.isFinished) {
                    showResult("First process is done")
                }
            }

        // 3. NotificationService executed (setelah SecondWorker selesai)
        workManager.getWorkInfoByIdLiveData(secondRequest.id)
            .observe(this) { info ->
                if (info != null && info.state.isFinished) {
                    showResult("Second process is done")
                    launchNotificationService(id) // Beri id "001"
                }
            }

        // 5. SecondNotificationService executed (setelah ThirdWorker selesai)
        workManager.getWorkInfoByIdLiveData(thirdRequest.id) // Ini sekarang sudah benar
            .observe(this) { info ->
                if (info != null && info.state.isFinished) {
                    showResult("Third process is done")
                    launchSecondNotificationService(id2) // Beri id "002"
                }
            }

    } // --- Akhir dari onCreate ---

    private fun getIdInputData(idKey: String, idValue: String) =
        Data.Builder()
            .putString(idKey, idValue)
            .build()

    private fun showResult(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // 4. ThirdWorker executed (setelah NotificationService selesai)
    private fun launchNotificationService(channelId: String) {
        NotificationService.trackingCompletion.observe(this) { Id ->
            showResult("Process for Notification Channel ID $Id is done!")

            workManager.enqueue(thirdRequest) // Cukup panggil ini
        }

        val serviceIntent = Intent(
            this,
            NotificationService::class.java
        ).apply {
            putExtra(NotificationService.EXTRA_ID, channelId) // Gunakan ID dari parameter
        }
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun launchSecondNotificationService(channelId: String) {
        // Observe jika service kedua selesai
        SecondNotificationService.trackingCompletion.observe(this) { Id ->
            showResult("Process for Notification Channel ID $Id is done!")
            // Selesai, tidak ada proses lagi setelah ini.
        }

        // Buat Intent untuk SecondNotificationService
        val serviceIntent = Intent(
            this,
            SecondNotificationService::class.java
        ).apply {
            // Gunakan EXTRA_ID dari SecondNotificationService
            putExtra(SecondNotificationService.EXTRA_ID, channelId)
        }

        ContextCompat.startForegroundService(this, serviceIntent)
    }
}