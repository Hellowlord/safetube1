package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

data class BatteryState(
    val level: Int = 100,
    val isCharging: Boolean = false,
    val isLowBattery: Boolean = false,
    val isSimulated: Boolean = false
) {
    val percentage: Int get() = level
    val batteryPercent: Int get() = level

    val displayStatus: String
        get() = when {
            isCharging -> "$level% • Charging ⚡"
            isLowBattery -> "$level% • Low Battery 🪫"
            else -> "$level% • Normal 🔋"
        }
}

class BatteryMonitor(private val context: Context) {

    private var simulatedLevel: Int? = null

    val batteryStateFlow: Flow<BatteryState> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED ||
                    intent?.action == Intent.ACTION_POWER_CONNECTED ||
                    intent?.action == Intent.ACTION_POWER_DISCONNECTED
                ) {
                    trySend(getCurrentBatteryState(intent))
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }

        val stickyIntent = context.registerReceiver(receiver, filter)
        trySend(getCurrentBatteryState(stickyIntent))

        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }.distinctUntilChanged()

    fun getCurrentBatteryState(batteryIntent: Intent? = null): BatteryState {
        simulatedLevel?.let { simLevel ->
            return BatteryState(
                level = simLevel,
                isCharging = false,
                isLowBattery = simLevel <= 20,
                isSimulated = true
            )
        }

        val intent = batteryIntent ?: try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            context.registerReceiver(null, filter)
        } catch (e: Exception) {
            null
        }

        if (intent == null) {
            return BatteryState(level = 100, isCharging = false, isLowBattery = false)
        }

        val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

        val level = if (rawLevel >= 0 && scale > 0) {
            ((rawLevel.toFloat() / scale.toFloat()) * 100).toInt()
        } else {
            100
        }

        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL ||
                plugged == BatteryManager.BATTERY_PLUGGED_AC ||
                plugged == BatteryManager.BATTERY_PLUGGED_USB ||
                plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS

        val isLowBattery = level <= 20 && !isCharging

        return BatteryState(
            level = level,
            isCharging = isCharging,
            isLowBattery = isLowBattery,
            isSimulated = false
        )
    }

    fun setSimulatedBatteryLevel(level: Int?) {
        simulatedLevel = level
    }

    fun getSimulatedBatteryLevel(): Int? = simulatedLevel
}
