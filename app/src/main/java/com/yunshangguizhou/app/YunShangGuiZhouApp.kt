package com.yunshangguizhou.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.yunshangguizhou.app.worker.DailyRecommendationWorker
import dagger.hilt.android.HiltAndroidApp
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class YunShangGuiZhouApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleDailyRecommendation()
    }

    private fun scheduleDailyRecommendation() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyRecommendationWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .addTag("daily_recommendation")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_recommendation_work",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }

    private fun calculateInitialDelay(): Long {
        val now = LocalDateTime.now()
        val target = now.withHour(3).withMinute(0).withSecond(0).withNano(0)
        val targetTime = if (target.isBefore(now)) target.plusDays(1) else target
        return java.time.Duration.between(now, targetTime).toMillis()
    }
}
