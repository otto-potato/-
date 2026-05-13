package com.yunshangguizhou.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yunshangguizhou.app.R
import com.yunshangguizhou.app.MainActivity
import com.yunshangguizhou.app.data.repository.RecommendationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DailyRecommendationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val recommendationRepository: RecommendationRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val result = recommendationRepository.generateDailyRecommendation()

            result.fold(
                onSuccess = { recommendation ->
                    showNotification(
                        title = "今日穿搭推荐已生成",
                        content = "${recommendation.weatherDesc} | ${recommendation.temperature}\n${recommendation.reasoning}"
                    )
                    Result.success()
                },
                onFailure = { error ->
                    showNotification(
                        title = "穿搭推荐生成失败",
                        content = error.message ?: "未知错误"
                    )
                    Result.retry()
                }
            )
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(title: String, content: String) {
        val channelId = "daily_recommendation"
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "每日穿搭推荐",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "每日AI穿搭推荐通知"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
