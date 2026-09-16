package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    /**
     * Returns the epoch timestamp (ms) of the most recent Sunday 00:00:00.
     * This represents the beginning of the active weekly fuel quota cycle.
     */
    fun getCurrentWeekSundayEpoch(): Long {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.SUNDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Returns the epoch timestamp of the next upcoming Sunday 00:00:00 when quota renews.
     */
    fun getNextSundayEpoch(): Long {
        val currentSunday = getCurrentWeekSundayEpoch()
        return currentSunday + TimeUnit.DAYS.toMillis(7)
    }

    /**
     * Returns how many days until the next weekly quota renewal.
     */
    fun getDaysUntilNextReset(): Int {
        val diffMs = getNextSundayEpoch() - System.currentTimeMillis()
        val days = TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
        return if (days < 0) 0 else days
    }

    fun formatDateTime(epochMs: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        return sdf.format(Date(epochMs))
    }

    fun formatDate(epochMs: Long): String {
        val sdf = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        return sdf.format(Date(epochMs))
    }

    fun formatLiters(liters: Double): String {
        return if (liters % 1.0 == 0.0) {
            "%.0f".format(Locale.US, liters)
        } else {
            "%.1f".format(Locale.US, liters)
        }
    }
}
