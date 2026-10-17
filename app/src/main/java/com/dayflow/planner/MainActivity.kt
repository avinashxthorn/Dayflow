package com.dayflow.planner

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Background = Color(0xFF09101D)
private val SurfaceDark = Color(0xFF152034)
private val SurfaceRaised = Color(0xFF1C2B44)
private val Muted = Color(0xFF9BAAC1)
private val Blue = Color(0xFF8AA9FF)
private val Purple = Color(0xFFA88BFF)
private val Green = Color(0xFF67DEBD)
private val Orange = Color(0xFFFFC087)
private val White = Color(0xFFF4F7FF)

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Android controls the final notification permission decision. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(9, 16, 29)
        window.navigationBarColor = android.graphics.Color.rgb(9, 16, 29)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Blue,
                    onPrimary = Background,
                    secondary = Green,
                    background = Background,
                    surface = SurfaceDark,
                    onSurface = White,
                    onBackground = White,
                    outline = Muted
                )
            ) { DayFlowApp() }
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

private fun startOfToday(): Long = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
    .toInstant().toEpochMilli()

private fun endOfToday(): Long = LocalDate.now().plusDays(1)
    .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun prettyDate(time: Long): String = Instant.ofEpochMilli(time)
    .atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("EEE, d MMM • h:mm a", Locale.getDefault()))

private fun compactDate(time: Long): String = Instant.ofEpochMilli(time)
    .atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("d MMM • h:mm a", Locale.getDefault()))

private fun categoryColor(category: String) = when (category) {
    "Study" -> Blue
    "Work" -> Purple
    "Health" -> Green
    else -> Orange
}

@Composable
private fun DayFlowApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val store = remember { TaskStore(context.applicationContext) }
    val tasks = store.tasks
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showEditor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<PlanTask?>(null) }
    var deleteCandidate by remember { mutableStateOf<PlanTask?>(null) }
    var exactGranted by remember { mutableStateOf(ReminderScheduler.exactAlarmAvailable(context)) }

    LaunchedEffect(Unit) { store.rescheduleAll() }

    val start = startOfToday()
    val end = endOfToday()
    val today = tasks.filter { it.dueAt in start until end }
    val completedToday = today.count { it.completed }
    val progress = if (today.isEmpty()) 0f else completedToday.toFloat() / today.size
    val activeTasks = tasks.filterNot { it.completed }
    val shown = when (selectedTab) {
        0 -> tasks.filter { it.dueAt < end && (!it.completed || it.dueAt >= start) }
        1 -> activeTasks.filter { it.dueAt >= end }
        2 -> activeTasks
        else -> tasks.filter { it.completed }
    }.sortedWith(compareBy<PlanTask> { it.completed }.thenBy { it.dueAt })

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(containerColor = SurfaceDark, tonalElevation = 0.dp) {
                val tabs = listOf(
                    Triple("Today", Icons.Default.Today, 0),
                    Triple("Upcoming", Icons.Default.Event, 1),
                    Triple("All", Icons.Default.ListAlt, 2),
                    Triple("Done", Icons.Default.TaskAlt, 3)
                )
                tabs.forEach { (title, icon, tab) ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Blue, selectedTextColor = White,
                            unselectedIconColor = Muted, unselectedTextColor = Muted,
                            indicatorColor = SurfaceRaised
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = null; showEditor = true },
                text = { Text("New plan", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                containerColor = Blue,
                contentColor = Background,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp, end = 20.dp, top = 24.dp, bottom = 110.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).background(Blue.copy(alpha = .14f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.EventAvailable, null, tint = Blue) }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("DAYFLOW", fontSize = 12.sp, color = Blue,
                            fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                        Text("Plan better. Do more.", fontSize = 12.sp, color = Muted)
                    }
                }
            }
            item {
                Spacer(Modifier.height(10.dp))
                Text(
                    when (selectedTab) {
                        0 -> "Make today count."
                        1 -> "What's next?"
                        2 -> "Your master plan."
                        else -> "Look at you go!"
                    },
                    fontSize = 27.sp, fontWeight = FontWeight.Bold, color = White
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())),
                    color = Muted, fontSize = 14.sp
                )
            }
            if (selectedTab == 0) {
                item { TodaySummary(completedToday, today.size, activeTasks.size, progress) }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !exactGranted) {
                    item {
                        ExactAlarmBanner {
                            context.startActivity(
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                            )
                            exactGranted = ReminderScheduler.exactAlarmAvailable(context)
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        when (selectedTab) { 0 -> "Today's focus"; 1 -> "Coming up"; 2 -> "All your plans"; else -> "Completed" },
                        fontSize = 19.sp, fontWeight = FontWeight.Bold
                    )
                    Surface(color = SurfaceRaised, shape = CircleShape) {
                        Text("${shown.size}", modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            color = Blue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            if (shown.isEmpty()) {
                item { EmptyTasks(selectedTab) }
            } else {
                items(shown, key = { it.id }) { task ->
                    TaskCard(task,
                        onToggle = { store.complete(task) },
                        onEdit = { editing = task; showEditor = true },
                        onDelete = { deleteCandidate = task }
                    )
                }
            }
        }
    }

    if (showEditor) {
        TaskEditor(
            existing = editing,
            onDismiss = { showEditor = false },
            onSave = { store.save(it); showEditor = false }
        )
    }
    deleteCandidate?.let { task ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Delete this plan?") },
            text = { Text("\"${task.title}\" will be removed from your phone.") },
            confirmButton = {
                TextButton(onClick = { store.delete(task); deleteCandidate = null }) {
                    Text("Delete", color = Orange)
                }
            },
            dismissButton = { TextButton(onClick = { deleteCandidate = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun TodaySummary(completed: Int, todayCount: Int, outstanding: Int, progress: Float) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF263D6D), Color(0xFF18253F), Color(0xFF1B1B3B))))
                .padding(22.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WbSunny, null, tint = Orange)
                Spacer(Modifier.width(8.dp))
                Text("TODAY'S MOMENTUM", color = White.copy(alpha = .82f),
                    fontSize = 11.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$completed", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = White)
                Text(" / $todayCount tasks done", modifier = Modifier.padding(bottom = 8.dp),
                    color = White.copy(alpha = .8f), fontSize = 14.sp)
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp),
                color = Green, trackColor = White.copy(alpha = .16f)
            )
            Spacer(Modifier.height(15.dp))
            Text(
                if (todayCount == 0) "A fresh page. Add your first plan!"
                else if (progress >= 1f) "All done. Amazing work today!"
                else "$outstanding active plans in your planner • keep moving!",
                color = White.copy(alpha = .85f), fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ExactAlarmBanner(onEnable: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEnable),
        shape = RoundedCornerShape(16.dp), color = Orange.copy(alpha = .1f),
        border = BorderStroke(1.dp, Orange.copy(alpha = .24f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.NotificationsActive, null, tint = Orange)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Enable precise reminders", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Tap to allow exact alarms in Settings", fontSize = 11.sp, color = Muted)
            }
            Icon(Icons.Default.KeyboardArrowRight, null, tint = Orange)
        }
    }
}

@Composable
private fun EmptyTasks(tab: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 46.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(if (tab == 3) Icons.Default.CheckCircle else Icons.Default.CalendarMonth,
                contentDescription = null, tint = Blue, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                when (tab) { 0 -> "Your day is wide open"; 1 -> "Nothing on the horizon";
                    2 -> "Start with one small step"; else -> "Your wins will appear here" },
                fontSize = 17.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (tab == 3) "Finish a plan and it will show up here."
                else "Tap New plan to organize your next move.",
                fontSize = 13.sp, color = Muted, textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: PlanTask,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val accent = categoryColor(task.category)
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(8.dp).background(accent, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(task.category.uppercase(), fontSize = 10.sp, color = accent,
                    letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                if (task.priority == "High") {
                    Icon(Icons.Default.Flag, null, modifier = Modifier.size(14.dp), tint = Orange)
                    Spacer(Modifier.width(3.dp))
                }
                Text(task.priority, color = if (task.priority == "High") Orange else Muted,
                    fontSize = 11.sp)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = task.completed,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = Green, uncheckedColor = Muted)
                )
                Spacer(Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(task.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                        color = if (task.completed) Muted else White,
                        textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (task.notes.isNotBlank()) {
                        Spacer(Modifier.height(3.dp))
                        Text(task.notes, color = Muted, fontSize = 12.sp,
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Edit plan", tint = Muted)
            }
            Spacer(Modifier.height(9.dp))
            HorizontalDivider(color = White.copy(alpha = .07f))
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Alarm, null, modifier = Modifier.size(15.dp), tint = Muted)
                Spacer(Modifier.width(5.dp))
                Text(compactDate(task.dueAt), color = Muted, fontSize = 12.sp)
                if (task.repeat != "Never") {
                    Spacer(Modifier.width(9.dp))
                    Text("↻ ${task.repeat}", color = Green, fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Delete ${task.title}",
                    modifier = Modifier.size(25.dp).clickable(onClick = onDelete).padding(3.dp),
                    tint = Muted
                )
            }
        }
    }
}

@Composable
private fun TaskEditor(existing: PlanTask?, onDismiss: () -> Unit, onSave: (PlanTask) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val defaultDue = remember(existing?.id) {
        val nextHour = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0)
        existing?.dueAt ?: nextHour.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    var title by remember(existing?.id) { mutableStateOf(existing?.title ?: "") }
    var notes by remember(existing?.id) { mutableStateOf(existing?.notes ?: "") }
    var dueAt by remember(existing?.id) { mutableLongStateOf(defaultDue) }
    var category by remember(existing?.id) { mutableStateOf(existing?.category ?: "Study") }
    var priority by remember(existing?.id) { mutableStateOf(existing?.priority ?: "Medium") }
    var repeat by remember(existing?.id) { mutableStateOf(existing?.repeat ?: "Never") }
    var reminder by remember(existing?.id) { mutableIntStateOf(existing?.remindBefore ?: 0) }

    fun selectDate() {
        val date = Instant.ofEpochMilli(dueAt).atZone(ZoneId.systemDefault())
        DatePickerDialog(context, { _, year, month, day ->
            dueAt = LocalDateTime.of(year, month + 1, day, date.hour, date.minute)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }, date.year, date.monthValue - 1, date.dayOfMonth).show()
    }
    fun selectTime() {
        val date = Instant.ofEpochMilli(dueAt).atZone(ZoneId.systemDefault())
        TimePickerDialog(context, { _, hour, minute ->
            dueAt = LocalDateTime.of(date.year, date.monthValue, date.dayOfMonth, hour, minute)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }, date.hour, date.minute, false).show()
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).heightIn(max = 680.dp),
            shape = RoundedCornerShape(26.dp), color = SurfaceDark
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (existing == null) Icons.Default.Add else Icons.Default.Edit,
                        contentDescription = null, tint = Blue)
                    Spacer(Modifier.width(10.dp))
                    Text(if (existing == null) "Create a plan" else "Edit your plan",
                        fontSize = 21.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(18.dp))
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("What needs to get done?") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    minLines = 2, maxLines = 3, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(18.dp))
                FieldTitle("WHEN")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = ::selectDate, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(Instant.ofEpochMilli(dueAt).atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("d MMM yyyy")), fontSize = 12.sp)
                    }
                    OutlinedButton(onClick = ::selectTime, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Alarm, null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(Instant.ofEpochMilli(dueAt).atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("h:mm a")), fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(14.dp))
                FieldTitle("CATEGORY")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Study", "Work").forEach { value ->
                        ChoiceChip(value, category == value, { category = value }, Modifier.weight(1f))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Personal", "Health").forEach { value ->
                        ChoiceChip(value, category == value, { category = value }, Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(13.dp))
                FieldTitle("PRIORITY")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Low", "Medium", "High").forEach { value ->
                        ChoiceChip(value, priority == value, { priority = value }, Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(13.dp))
                FieldTitle("REPEAT")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Never", "Daily", "Weekly").forEach { value ->
                        ChoiceChip(value, repeat == value, { repeat = value }, Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(13.dp))
                FieldTitle("REMIND ME")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0, 10, 30).forEach { value ->
                        ChoiceChip(
                            when (value) { 0 -> "At time"; 10 -> "10 min"; else -> "30 min" },
                            reminder == value, { reminder = value }, Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(22.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onSave(
                                (existing ?: PlanTask(title = title.trim(), dueAt = dueAt)).copy(
                                    title = title.trim(), notes = notes.trim(), dueAt = dueAt,
                                    category = category, priority = priority, repeat = repeat,
                                    remindBefore = reminder
                                )
                            )
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Background)
                    ) {
                        Text(if (existing == null) "Create plan" else "Save changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldTitle(text: String) {
    Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Muted, letterSpacing = 1.2.sp)
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun ChoiceChip(value: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilterChip(
        modifier = modifier,
        selected = selected, onClick = onClick,
        label = { Text(value, fontSize = 11.sp, maxLines = 1) },
        shape = RoundedCornerShape(12.dp)
    )
}
