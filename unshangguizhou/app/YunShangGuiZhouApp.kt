package com.yunshangguizhou.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.yunshangguizhou.app.data.repository.SettingsRepository
import com.yunshangguizhou.app.worker.DailyRecommendationWorker
import dagger.hilt.android.HiltAndroidApp
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class YunShangGuiZhouApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var settingsRepo: SettingsRepository

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        Thread {
            val settings = kotlinx.coroutines.runBlocking { settingsRepo.getSettingsOnce() }
            if (settings.calendarEnabled) scheduleDailyRecommendation()
        }.start()
    }

    private fun scheduleDailyRecommendation() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED).build()
        val request = PeriodicWorkRequestBuilder<DailyRecommendationWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInitialDelay(calculateDelay(), TimeUnit.MILLISECONDS)
            .addTag("daily_recommendation").build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_recommendation_work", ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun rescheduleIfNeeded() {
        Thread {
            val settings = kotlinx.coroutines.runBlocking { settingsRepo.getSettingsOnce() }
            val wm = WorkManager.getInstance(this)
            if (settings.calendarEnabled) {
                wm.cancelUniqueWork("daily_recommendation_work")
                scheduleDailyRecommendation()
            } else {
                wm.cancelUniqueWork("daily_recommendation_work")
            }
        }.start()
    }

    private fun calculateDelay(): Long {
        val now = LocalDateTime.now()
        val target = now.withHour(3).withMinute(0).withSecond(0).withNano(0)
        val t = if (target.isBefore(now)) target.plusDays(1) else target
        return java.time.Duration.between(now, t).toMillis()
    }
}
