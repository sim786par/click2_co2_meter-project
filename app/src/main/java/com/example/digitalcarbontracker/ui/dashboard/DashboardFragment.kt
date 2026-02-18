package com.example.click2co2meter2.ui.dashboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.click2co2meter2.R

class DashboardFragment : Fragment() {

    private var initialBatteryLevel = 0
    private lateinit var tvCarbon: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        tvCarbon = view.findViewById(R.id.tvCarbon)

        getBatteryLevel()

        return view
    }

    private fun getBatteryLevel() {

        val batteryStatus = requireContext().registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1

        if (initialBatteryLevel == 0) {
            initialBatteryLevel = level
        }

        val batteryUsed = initialBatteryLevel - level

        val co2 = calculateCarbon(batteryUsed)

        tvCarbon.text = "$co2 g CO2"
    }

    private fun calculateCarbon(batteryPercent: Int): Double {

        val energyWh = batteryPercent * 0.05
        val energyKWh = energyWh * 0.001
        val carbon = energyKWh * 700

        return String.format("%.2f", carbon).toDouble()
    }
}
