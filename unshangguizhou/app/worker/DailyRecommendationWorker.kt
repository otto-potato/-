package com.yunshangguizhou.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.yunshangguizhou.app.MainActivity
import com.yunshangguizhou.app.data.repository.RecommendationRepository
import com.yunshangguizhou.app.data.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DailyRecommendationWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val repo: RecommendationRepository,
    private val settingsRepo: SettingsRepository
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val settings = settingsRepo.getSettingsOnce()
        if (!settings.calendarEnabled) return Result.success()

        return repo.generateDaily().fold(
            onSuccess = { rec ->
                show("今日穿搭推荐",
                    "${rec.weatherDesc} ${rec.temperature} - ${rec.reasoning.take(100)}...")
                Result.success()
            },
            onFailure = { Result.retry() }
        )
    }

    private fun show(title: String, body: String) {
        val chId = "daily_rec"
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(chId, "穿搭推荐", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(applicationContext, chId)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle(title).setContentText(body)
            .setContentIntent(pi).setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()
        nm.notify(System.currentTimeMillis().toInt(), n)
    }
}
