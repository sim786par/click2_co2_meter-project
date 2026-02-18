package com.example.digitalcarbontracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.digitalcarbontracker.data.db.UsageLog
import com.example.digitalcarbontracker.data.repository.UsageRepository
import com.example.digitalcarbontracker.utils.CarbonCalculator
import com.example.digitalcarbontracker.utils.DeviceStatsCollector

class EmissionWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

        val repo = UsageRepository(context)

        // Example Battery Capacity (5000mAh typical)
        val batteryMah = 5000
        val batteryCapacityWh = CarbonCalculator.mahToWh(batteryMah)

        val endBattery = DeviceStatsCollector.getBatteryPercent(context)

        // We'll store startBattery from last run (simple approach)
        val prefs = context.getSharedPreferences("carbon_prefs", Context.MODE_PRIVATE)
        val startBattery = prefs.getInt("last_battery", endBattery)

        val batteryDrop = (startBattery - endBattery).coerceAtLeast(0)

        val brightness = DeviceStatsCollector.getBrightnessPercent(context)
        val networkType = DeviceStatsCollector.getNetworkType(context)
        val screenTime = DeviceStatsCollector.getScreenTimeMinutesToday(context)

        val energyKwh = CarbonCalculator.calculateEnergyKwh(startBattery, endBattery, batteryCapacityWh)
        val co2Grams = CarbonCalculator.calculateCO2Grams(energyKwh)

        val log = UsageLog(
            timestamp = System.currentTimeMillis(),
            startBattery = startBattery,
            endBattery = endBattery,
            batteryDrop = batteryDrop,
            screenTimeMinutes = screenTime,
            brightnessPercent = brightness,
            networkType = networkType,
            energyKwh = energyKwh,
            co2Grams = co2Grams
        )

        repo.insertLog(log)

        // Save last battery for next cycle
        prefs.edit().putInt("last_battery", endBattery).apply()

        return Result.success()
    }

}

