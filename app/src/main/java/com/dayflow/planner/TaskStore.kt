package com.dayflow.planner

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class PlanTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val notes: String = "",
    val dueAt: Long,
    val category: String = "Personal",
    val priority: String = "Medium",
    val repeat: String = "Never",
    val remindBefore: Int = 0,
    val completed: Boolean = false
)

class TaskStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("dayflow_plans_v1", Context.MODE_PRIVATE)

    var tasks by mutableStateOf(readTasks())
        private set

    fun save(task: PlanTask) {
        tasks.firstOrNull { it.id == task.id }?.let { ReminderScheduler.cancel(context, it) }
        tasks = (tasks.filterNot { it.id == task.id } + task).sortedBy { it.dueAt }
        writeTasks()
        ReminderScheduler.schedule(context, task)
    }

    fun delete(task: PlanTask) {
        ReminderScheduler.cancel(context, task)
        tasks = tasks.filterNot { it.id == task.id }
        writeTasks()
    }

    fun complete(task: PlanTask) {
        if (task.repeat == "Never") {
            save(task.copy(completed = !task.completed))
        } else {
            val next = ReminderScheduler.nextOccurrence(task.dueAt, task.repeat, System.currentTimeMillis())
            save(task.copy(dueAt = next, completed = false))
        }
    }

    fun rescheduleAll() = tasks.forEach { ReminderScheduler.schedule(context, it) }

    private fun readTasks(): List<PlanTask> {
        return try {
            val data = JSONArray(prefs.getString("tasks", "[]") ?: "[]")
            (0 until data.length()).map { index ->
                val item = data.getJSONObject(index)
                PlanTask(
                    id = item.getString("id"),
                    title = item.getString("title"),
                    notes = item.optString("notes", ""),
                    dueAt = item.getLong("dueAt"),
                    category = item.optString("category", "Personal"),
                    priority = item.optString("priority", "Medium"),
                    repeat = item.optString("repeat", "Never"),
                    remindBefore = item.optInt("remindBefore", 0),
                    completed = item.optBoolean("completed", false)
                )
            }.sortedBy { it.dueAt }
        } catch (_: Exception) { emptyList() }
    }

    private fun writeTasks() {
        val array = JSONArray()
        tasks.forEach { task ->
            array.put(JSONObject().apply {
                put("id", task.id)
                put("title", task.title)
                put("notes", task.notes)
                put("dueAt", task.dueAt)
                put("category", task.category)
                put("priority", task.priority)
                put("repeat", task.repeat)
                put("remindBefore", task.remindBefore)
                put("completed", task.completed)
            })
        }
        prefs.edit().putString("tasks", array.toString()).apply()
    }
}
