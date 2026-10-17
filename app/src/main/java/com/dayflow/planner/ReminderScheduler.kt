package com.dayflow.planner

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.Instant
import java.time.ZoneId

object ReminderScheduler {
    private fun alarmIntent(context: Context, task: PlanTask): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.dayflow.planner.REMIND.${task.id}"
            putExtra("id", task.id)
        }
        return PendingIntent.getBroadcast(
            context, task.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun exactAlarmAvailable(context: Context): Boolean {
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarms.canScheduleExactAlarms()
    }

    fun cancel(context: Context, task: PlanTask) {
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarms.cancel(alarmIntent(context, task))
    }

    fun schedule(context: Context, task: PlanTask) {
        cancel(context, task)
        if (task.completed) return
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()
        var reminderTime = task.dueAt - task.remindBefore * 60_000L
        if (reminderTime <= now) {
            if (task.repeat == "Never") return
            reminderTime = nextOccurrence(task.dueAt, task.repeat, now + task.remindBefore * 60_000L) -
                task.remindBefore * 60_000L
        }
        val pending = alarmIntent(context, task)
        try {
            if (exactAlarmAvailable(context)) {
                alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pending)
            } else {
                alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pending)
            }
        } catch (_: SecurityException) {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pending)
        }
    }

    /** Advances by local calendar days so daily 9 AM stays 9 AM across daylight saving changes. */
    fun nextOccurrence(dueAt: Long, repeat: String, after: Long): Long {
        val zone = ZoneId.systemDefault()
        var date = Instant.ofEpochMilli(dueAt).atZone(zone)
        val days = if (repeat == "Weekly") 7L else 1L
        while (date.toInstant().toEpochMilli() <= after) {
            date = date.plusDays(days)
        }
        return date.toInstant().toEpochMilli()
    }
}
