package com.example.yearbook

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

private const val STORAGE_KEY = "yearbook_state"
private const val YEAR = 2026
private const val TOTAL_WEEKS = 53
private const val SLOTS_PER_DAY = 48

private val DAY_NAMES = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
private val MOOD_EMOJIS = listOf("😢", "🙁", "😐", "🙂", "😁")

private data class MonthScheme(
    val month: Int,
    val monthName: String,
    val weekRange: IntRange,
    val background: Color,
    val panel: Color,
    val accent: Color,
    val accentText: Color,
    val eventColor: Color
)

private val MONTH_SCHEMES = listOf(
    MonthScheme(1, "Ice Blue", 1..5, Color(0xFFEAF4FF), Color(0xFFF8FBFF), Color(0xFF8BC6FF), Color(0xFF0D3B66), Color(0xFFAED8FF)),
    MonthScheme(2, "Creamy Orange", 6..9, Color(0xFFFFF5E8), Color(0xFFFFFAF2), Color(0xFFFFBE76), Color(0xFF5A3510), Color(0xFFFFD9A7)),
    MonthScheme(3, "Grass Green", 10..13, Color(0xFFF1FBEA), Color(0xFFFAFFF7), Color(0xFF8FD36E), Color(0xFF1C4A1D), Color(0xFFBDECA7)),
    MonthScheme(4, "Lavender Purple", 14..17, Color(0xFFF5EEFF), Color(0xFFFCF9FF), Color(0xFFC3A1FF), Color(0xFF3E2860), Color(0xFFDCC7FF)),
    MonthScheme(5, "Apricot Yellow", 18..22, Color(0xFFFFF9E5), Color(0xFFFFFDF3), Color(0xFFF4C542), Color(0xFF5A460C), Color(0xFFFBE08B)),
    MonthScheme(6, "Cherry Blossom Pink", 23..26, Color(0xFFFFEEF3), Color(0xFFFFF8FB), Color(0xFFF39AB5), Color(0xFF5B1F33), Color(0xFFF8C7D7)),
    MonthScheme(7, "Sky Blue", 27..31, Color(0xFFEAF7FF), Color(0xFFF7FCFF), Color(0xFF78BDF2), Color(0xFF113C59), Color(0xFFB8E0FB)),
    MonthScheme(8, "Mango Orange", 32..35, Color(0xFFFFF2E6), Color(0xFFFFFAF5), Color(0xFFFFA34D), Color(0xFF5A2C06), Color(0xFFFFC88F)),
    MonthScheme(9, "Mint Green", 36..39, Color(0xFFE8FBF4), Color(0xFFF6FFFB), Color(0xFF7BD8B4), Color(0xFF0E4635), Color(0xFFB4EED8)),
    MonthScheme(10, "Caramel Brown", 40..44, Color(0xFFFAF1E8), Color(0xFFFEF9F3), Color(0xFFC38B5A), Color(0xFF4A2B14), Color(0xFFE1B892)),
    MonthScheme(11, "Coffee Brown", 45..48, Color(0xFFF7EFEA), Color(0xFFFDF8F5), Color(0xFFA67B5B), Color(0xFF3C2516), Color(0xFFD1B29B)),
    MonthScheme(12, "Coral Red", 49..53, Color(0xFFFFEDEE), Color(0xFFFFF8F8), Color(0xFFEF7F7F), Color(0xFF5A1F1F), Color(0xFFF5B1B1))
)

private data class CalendarDay(val year: Int, val month: Int, val day: Int)

private data class DiaryEvent(
    val id: String,
    val week: Int,
    val dayIndex: Int,
    val startSlot: Int,
    val endSlot: Int,
    val text: String
)

private data class TodoItem(
    val id: String,
    val text: String,
    val done: Boolean
)

private data class PlacedImage(
    val id: String,
    val week: Int,
    val uri: String,
    val x: Float,
    val y: Float,
    val sizeDp: Float,
    val linkedEventId: String? = null
)

private data class YearBookState(
    val currentWeek: Int = 18,
    val todos: List<TodoItem> = listOf(TodoItem(newId(), "", false)),
    val events: List<DiaryEvent> = emptyList(),
    val moods: Map<String, Int> = emptyMap(),
    val weekSummaries: Map<Int, String> = emptyMap(),
    val images: List<PlacedImage> = emptyList()
)

private fun newId(): String = UUID.randomUUID().toString()

private class YearBookStore(context: Context) {
    private val prefs = context.getSharedPreferences("yearbook_diary", Context.MODE_PRIVATE)

    var state by mutableStateOf(loadState())
        private set

    fun setWeek(week: Int) {
        update(state.copy(currentWeek = week.coerceIn(1, TOTAL_WEEKS)))
    }

    fun addTodo() {
        update(state.copy(todos = state.todos + TodoItem(newId(), "", false)))
    }

    fun removeTodo(todoId: String) {
        if (state.todos.size <= 1) return
        update(state.copy(todos = state.todos.filterNot { it.id == todoId }))
    }

    fun updateTodo(todoId: String, text: String) {
        update(
            state.copy(
                todos = state.todos.map { todo ->
                    if (todo.id == todoId) todo.copy(text = text) else todo
                }
            )
        )
    }

    fun toggleTodo(todoId: String) {
        update(
            state.copy(
                todos = state.todos.map { todo ->
                    if (todo.id == todoId) todo.copy(done = !todo.done) else todo
                }
            )
        )
    }

    fun addEvent(week: Int, dayIndex: Int, startSlot: Int, endSlot: Int) {
        val minSlot = minOf(startSlot, endSlot)
        val maxSlot = maxOf(startSlot, endSlot)
        val hasOverlap = state.events.any { event ->
            event.week == week &&
                event.dayIndex == dayIndex &&
                rangesOverlap(minSlot, maxSlot, event.startSlot, event.endSlot)
        }
        if (hasOverlap) return

        val event = DiaryEvent(
            id = newId(),
            week = week,
            dayIndex = dayIndex,
            startSlot = minSlot,
            endSlot = maxSlot,
            text = ""
        )
        update(state.copy(events = state.events + event))
    }

    fun updateEventText(eventId: String, text: String) {
        update(
            state.copy(
                events = state.events.map { event ->
                    if (event.id == eventId) event.copy(text = text) else event
                }
            )
        )
    }

    fun deleteEvent(eventId: String) {
        update(
            state.copy(
                events = state.events.filterNot { it.id == eventId },
                images = state.images.map { image ->
                    if (image.linkedEventId == eventId) image.copy(linkedEventId = null) else image
                }
            )
        )
    }

    fun setMood(week: Int, dayIndex: Int, moodIndex: Int) {
        val key = moodKey(week, dayIndex)
        val next = state.moods.toMutableMap()
        if (next[key] == moodIndex) {
            next.remove(key)
        } else {
            next[key] = moodIndex
        }
        update(state.copy(moods = next))
    }

    fun updateWeekSummary(week: Int, text: String) {
        val next = state.weekSummaries.toMutableMap()
        next[week] = text
        update(state.copy(weekSummaries = next))
    }

    fun addImage(week: Int, uri: String) {
        val image = PlacedImage(
            id = newId(),
            week = week,
            uri = uri,
            x = 280f,
            y = 220f,
            sizeDp = 92f,
            linkedEventId = null
        )
        update(state.copy(images = state.images + image))
    }

    fun setImageTag(imageId: String, eventId: String?) {
        update(
            state.copy(
                images = state.images.map { image ->
                    if (image.id == imageId) image.copy(linkedEventId = eventId) else image
                }
            )
        )
    }

    fun moveImage(id: String, deltaX: Float, deltaY: Float) {
        update(
            state.copy(
                images = state.images.map { image ->
                    if (image.id == id) {
                        image.copy(
                            x = (image.x + deltaX).coerceAtLeast(0f),
                            y = (image.y + deltaY).coerceAtLeast(0f)
                        )
                    } else {
                        image
                    }
                }
            )
        )
    }

    fun scaleImage(id: String, zoomFactor: Float) {
        if (!zoomFactor.isFinite() || zoomFactor <= 0f) return

        update(
            state.copy(
                images = state.images.map { image ->
                    if (image.id == id) {
                        image.copy(sizeDp = (image.sizeDp * zoomFactor).coerceIn(56f, 240f))
                    } else {
                        image
                    }
                }
            )
        )
    }

    fun autoLayoutImagesForWeek(week: Int, columns: Int) {
        val weekImages = state.images.filter { it.week == week }.sortedBy { it.id }
        if (weekImages.isEmpty()) return

        val safeColumns = columns.coerceAtLeast(1)
        val startX = 16f
        val startY = 20f
        val cellWidth = 116f
        val cellHeight = 126f

        val positionMap = mutableMapOf<String, Pair<Float, Float>>()
        weekImages.forEachIndexed { index, image ->
            val col = index % safeColumns
            val row = index / safeColumns
            val x = startX + col * cellWidth
            val y = startY + row * cellHeight
            positionMap[image.id] = x to y
        }

        update(
            state.copy(
                images = state.images.map { image ->
                    val target = positionMap[image.id]
                    if (target != null) {
                        image.copy(x = target.first, y = target.second)
                    } else {
                        image
                    }
                }
            )
        )
    }

    fun deleteImage(id: String) {
        update(state.copy(images = state.images.filterNot { it.id == id }))
    }

    private fun update(newState: YearBookState) {
        state = newState
        persist()
    }

    private fun persist() {
        val root = JSONObject().apply {
            put("currentWeek", state.currentWeek)
            put("todos", JSONArray().apply {
                state.todos.forEach { todo ->
                    put(
                        JSONObject().apply {
                            put("id", todo.id)
                            put("text", todo.text)
                            put("done", todo.done)
                        }
                    )
                }
            })
            put("events", JSONArray().apply {
                state.events.forEach { event ->
                    put(
                        JSONObject().apply {
                            put("id", event.id)
                            put("week", event.week)
                            put("dayIndex", event.dayIndex)
                            put("startSlot", event.startSlot)
                            put("endSlot", event.endSlot)
                            put("text", event.text)
                        }
                    )
                }
            })
            put("moods", JSONObject().apply {
                state.moods.forEach { (key, value) ->
                    put(key, value)
                }
            })
            put("weekSummaries", JSONObject().apply {
                state.weekSummaries.forEach { (week, text) ->
                    put(week.toString(), text)
                }
            })
            put("images", JSONArray().apply {
                state.images.forEach { image ->
                    put(
                        JSONObject().apply {
                            put("id", image.id)
                            put("week", image.week)
                            put("uri", image.uri)
                            put("x", image.x)
                            put("y", image.y)
                            put("sizeDp", image.sizeDp)
                            put("linkedEventId", image.linkedEventId)
                        }
                    )
                }
            })
        }

        prefs.edit().putString(STORAGE_KEY, root.toString()).apply()
    }

    private fun loadState(): YearBookState {
        val raw = prefs.getString(STORAGE_KEY, null) ?: return YearBookState()
        return runCatching {
            val root = JSONObject(raw)
            val todos = mutableListOf<TodoItem>()
            val todosArray = root.optJSONArray("todos") ?: JSONArray()
            for (i in 0 until todosArray.length()) {
                val obj = todosArray.optJSONObject(i) ?: continue
                todos.add(
                    TodoItem(
                        id = obj.optString("id", newId()),
                        text = obj.optString("text", ""),
                        done = obj.optBoolean("done", false)
                    )
                )
            }
            if (todos.isEmpty()) {
                todos.add(TodoItem(newId(), "", false))
            }

            val events = mutableListOf<DiaryEvent>()
            val eventsArray = root.optJSONArray("events") ?: JSONArray()
            for (i in 0 until eventsArray.length()) {
                val obj = eventsArray.optJSONObject(i) ?: continue
                events.add(
                    DiaryEvent(
                        id = obj.optString("id", newId()),
                        week = obj.optInt("week", 18),
                        dayIndex = obj.optInt("dayIndex", 0),
                        startSlot = obj.optInt("startSlot", 0).coerceIn(0, SLOTS_PER_DAY - 1),
                        endSlot = obj.optInt("endSlot", 0).coerceIn(0, SLOTS_PER_DAY - 1),
                        text = obj.optString("text", "")
                    )
                )
            }

            val moods = mutableMapOf<String, Int>()
            val moodsObj = root.optJSONObject("moods") ?: JSONObject()
            moodsObj.keys().forEach { key ->
                moods[key] = moodsObj.optInt(key, 2)
            }

            val summaries = mutableMapOf<Int, String>()
            val summariesObj = root.optJSONObject("weekSummaries") ?: JSONObject()
            summariesObj.keys().forEach { key ->
                val week = key.toIntOrNull() ?: return@forEach
                summaries[week] = summariesObj.optString(key, "")
            }

            val images = mutableListOf<PlacedImage>()
            val imagesArray = root.optJSONArray("images") ?: JSONArray()
            for (i in 0 until imagesArray.length()) {
                val obj = imagesArray.optJSONObject(i) ?: continue
                images.add(
                    PlacedImage(
                        id = obj.optString("id", newId()),
                        week = obj.optInt("week", 18),
                        uri = obj.optString("uri", ""),
                        x = obj.optDouble("x", 200.0).toFloat(),
                        y = obj.optDouble("y", 200.0).toFloat(),
                        sizeDp = obj.optDouble("sizeDp", 92.0).toFloat(),
                        linkedEventId = obj.optString("linkedEventId").ifBlank { null }
                    )
                )
            }

            YearBookState(
                currentWeek = root.optInt("currentWeek", 18).coerceIn(1, TOTAL_WEEKS),
                todos = todos,
                events = events,
                moods = moods,
                weekSummaries = summaries,
                images = images
            )
        }.getOrDefault(YearBookState())
    }
}

private fun moodKey(week: Int, dayIndex: Int): String = "$week-$dayIndex"

private fun rangesOverlap(startA: Int, endA: Int, startB: Int, endB: Int): Boolean {
    return maxOf(startA, startB) <= minOf(endA, endB)
}

private fun formatTimeBySlot(slot: Int): String {
    val safeSlot = slot.coerceIn(0, SLOTS_PER_DAY)
    val hour = safeSlot / 2 + 1
    val minute = if (safeSlot % 2 == 0) 0 else 30
    return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
}

private fun displayEventName(event: DiaryEvent): String {
    val custom = event.text.lineSequence().firstOrNull()?.trim().orEmpty()
    val timePart = "${DAY_NAMES[event.dayIndex]} ${formatTimeBySlot(event.startSlot)}-${formatTimeBySlot(event.endSlot + 1)}"
    return if (custom.isBlank()) timePart else "$custom · $timePart"
}

private fun schemeForWeek(week: Int): MonthScheme {
    return MONTH_SCHEMES.firstOrNull { week in it.weekRange } ?: MONTH_SCHEMES.last()
}

private fun dayOfWeekIndexMondayFirst(calendar: Calendar): Int {
    val sundayFirst = calendar.get(Calendar.DAY_OF_WEEK)
    return (sundayFirst + 5) % 7
}

private fun buildWeekDays(week: Int): List<CalendarDay?> {
    val jan1 = Calendar.getInstance().apply {
        set(Calendar.YEAR, YEAR)
        set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val firstWeekStart = (jan1.clone() as Calendar).apply {
        add(Calendar.DAY_OF_MONTH, -dayOfWeekIndexMondayFirst(this))
    }

    val thisWeekStart = (firstWeekStart.clone() as Calendar).apply {
        add(Calendar.DAY_OF_MONTH, (week - 1) * 7)
    }

    return (0..6).map { offset ->
        val cell = (thisWeekStart.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, offset)
        }
        val year = cell.get(Calendar.YEAR)
        if (year != YEAR) {
            null
        } else {
            CalendarDay(
                year = year,
                month = cell.get(Calendar.MONTH) + 1,
                day = cell.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
}

private fun monthMeta(month: Int): Triple<Int, Int, String> {
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, YEAR)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val firstOffset = dayOfWeekIndexMondayFirst(cal)
    val days = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val label = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH) ?: "Month"
    return Triple(firstOffset, days, label)
}

private data class DragSelection(val dayIndex: Int, val startSlot: Int, val endSlot: Int)

private sealed interface PlannerColumnItem {
    data class Day(val dayIndex: Int, val day: CalendarDay?) : PlannerColumnItem
    data object Summary : PlannerColumnItem
}

private fun buildCompactPages(weekDays: List<CalendarDay?>): List<List<PlannerColumnItem>> {
    return listOf(
        listOf(PlannerColumnItem.Day(0, weekDays.getOrNull(0)), PlannerColumnItem.Day(1, weekDays.getOrNull(1))),
        listOf(PlannerColumnItem.Day(2, weekDays.getOrNull(2)), PlannerColumnItem.Day(3, weekDays.getOrNull(3))),
        listOf(PlannerColumnItem.Day(4, weekDays.getOrNull(4)), PlannerColumnItem.Day(5, weekDays.getOrNull(5))),
        listOf(PlannerColumnItem.Day(6, weekDays.getOrNull(6)), PlannerColumnItem.Summary)
    )
}

private fun compactPageForDayIndex(dayIndex: Int): Int {
    return when (dayIndex) {
        0, 1 -> 0
        2, 3 -> 1
        4, 5 -> 2
        else -> 3
    }
}

@Composable
fun YearBookDiaryApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val store = remember { YearBookStore(context) }
    val state = store.state
    val currentWeek = state.currentWeek
    val scheme = schemeForWeek(currentWeek)
    val weekDays = remember(currentWeek) { buildWeekDays(currentWeek) }
    val isCompact = configuration.screenWidthDp < 900
    val compactPanelWidth = (configuration.screenWidthDp.dp * 0.82f).coerceAtMost(340.dp)
    val todayCompactPage = remember {
        compactPageForDayIndex(dayOfWeekIndexMondayFirst(Calendar.getInstance()))
    }

    var dragSelection by remember { mutableStateOf<DragSelection?>(null) }
    var isSidePanelExpanded by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var isGalleryMode by remember { mutableStateOf(false) }
    var selectedEventId by remember { mutableStateOf<String?>(null) }
    var selectedImageId by remember { mutableStateOf<String?>(null) }
    var hasAppliedInitialCompactPage by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            store.addImage(currentWeek, uri.toString())
        }
    }

    val sidePanelWidth = 220.dp
    val slotHeight = 24.dp
    val timeLabelWidth = 30.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .padding(8.dp)
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, scheme.accent, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scheme.panel)
                    .padding(10.dp)
            ) {
                if (isGalleryMode) {
                    GalleryTopBar(
                        currentWeek = currentWeek,
                        scheme = scheme,
                        isCompact = isCompact,
                        onPrev = { store.setWeek(currentWeek - 1) },
                        onNext = { store.setWeek(currentWeek + 1) },
                        onBackToDiary = { isGalleryMode = false },
                        onAutoLayout = {
                            store.autoLayoutImagesForWeek(
                                week = currentWeek,
                                columns = if (isCompact) 2 else 4
                            )
                        },
                        onAddImage = { imagePicker.launch(arrayOf("image/*")) }
                    )
                } else {
                    TopBar(
                        currentWeek = currentWeek,
                        scheme = scheme,
                        isCompact = isCompact,
                        isSidePanelExpanded = isSidePanelExpanded,
                        isEditMode = isEditMode,
                        onPrev = { store.setWeek(currentWeek - 1) },
                        onNext = { store.setWeek(currentWeek + 1) },
                        onToggleSidePanel = { isSidePanelExpanded = !isSidePanelExpanded },
                        onToggleEditMode = { isEditMode = !isEditMode },
                        onOpenGallery = {
                            isSidePanelExpanded = false
                            isGalleryMode = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isGalleryMode) {
                    GalleryBoard(
                        scheme = scheme,
                        images = state.images.filter { it.week == currentWeek },
                        weekEvents = state.events
                            .filter { it.week == currentWeek }
                            .sortedWith(compareBy<DiaryEvent> { it.dayIndex }.thenBy { it.startSlot }),
                        selectedImageId = selectedImageId,
                        onSelectImage = { selectedImageId = it },
                        onMoveImage = { imageId, dx, dy -> store.moveImage(imageId, dx, dy) },
                        onScaleImage = { imageId, zoomFactor -> store.scaleImage(imageId, zoomFactor) },
                        onDeleteImage = { imageId ->
                            store.deleteImage(imageId)
                            if (selectedImageId == imageId) {
                                selectedImageId = null
                            }
                        },
                        onSetImageTag = { imageId, eventId -> store.setImageTag(imageId, eventId) }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (isCompact) {
                            Planner(
                                scheme = scheme,
                                weekDays = weekDays,
                                week = currentWeek,
                                isCompact = true,
                                initialCompactPage = todayCompactPage,
                                shouldApplyInitialCompactPage = !hasAppliedInitialCompactPage,
                                onInitialCompactPageApplied = { hasAppliedInitialCompactPage = true },
                                isEditMode = isEditMode,
                                events = state.events.filter { it.week == currentWeek },
                                selectedEventId = selectedEventId,
                                moods = state.moods,
                                weekSummary = state.weekSummaries[currentWeek].orEmpty(),
                                dragSelection = dragSelection,
                                slotHeight = slotHeight,
                                timeLabelWidth = timeLabelWidth,
                                onStartDrag = { dayIndex, slot ->
                                    dragSelection = DragSelection(dayIndex, slot, slot)
                                },
                                onDrag = { dayIndex, slot ->
                                    val now = dragSelection
                                    if (now != null && now.dayIndex == dayIndex) {
                                        dragSelection = now.copy(endSlot = slot)
                                    }
                                },
                                onFinishDrag = {
                                    val selected = dragSelection
                                    if (selected != null) {
                                        store.addEvent(
                                            week = currentWeek,
                                            dayIndex = selected.dayIndex,
                                            startSlot = selected.startSlot,
                                            endSlot = selected.endSlot
                                        )
                                    }
                                    dragSelection = null
                                },
                                onUpdateEventText = store::updateEventText,
                                onDeleteEvent = { eventId ->
                                    store.deleteEvent(eventId)
                                    if (selectedEventId == eventId) {
                                        selectedEventId = null
                                    }
                                },
                                onSelectEvent = { selectedEventId = it },
                                onDeleteSelectedEvent = {
                                    val target = selectedEventId
                                    if (target != null) {
                                        store.deleteEvent(target)
                                        selectedEventId = null
                                    }
                                },
                                onClearSelectedEvent = { selectedEventId = null },
                                onSelectMood = { dayIndex, moodIndex ->
                                    store.setMood(currentWeek, dayIndex, moodIndex)
                                },
                                onUpdateWeekSummary = { store.updateWeekSummary(currentWeek, it) }
                            )
                        } else {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Sidebar(
                                    scheme = scheme,
                                    todos = state.todos,
                                    weekDays = weekDays,
                                    onAddTodo = store::addTodo,
                                    onToggleTodo = store::toggleTodo,
                                    onUpdateTodo = store::updateTodo,
                                    onDeleteTodo = store::removeTodo,
                                    width = sidePanelWidth
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Planner(
                                    scheme = scheme,
                                    weekDays = weekDays,
                                    week = currentWeek,
                                    isCompact = false,
                                    initialCompactPage = todayCompactPage,
                                    shouldApplyInitialCompactPage = false,
                                    onInitialCompactPageApplied = {},
                                    isEditMode = isEditMode,
                                    events = state.events.filter { it.week == currentWeek },
                                    selectedEventId = selectedEventId,
                                    moods = state.moods,
                                    weekSummary = state.weekSummaries[currentWeek].orEmpty(),
                                    dragSelection = dragSelection,
                                    slotHeight = slotHeight,
                                    timeLabelWidth = timeLabelWidth,
                                    onStartDrag = { dayIndex, slot ->
                                        dragSelection = DragSelection(dayIndex, slot, slot)
                                    },
                                    onDrag = { dayIndex, slot ->
                                        val now = dragSelection
                                        if (now != null && now.dayIndex == dayIndex) {
                                            dragSelection = now.copy(endSlot = slot)
                                        }
                                    },
                                    onFinishDrag = {
                                        val selected = dragSelection
                                        if (selected != null) {
                                            store.addEvent(
                                                week = currentWeek,
                                                dayIndex = selected.dayIndex,
                                                startSlot = selected.startSlot,
                                                endSlot = selected.endSlot
                                            )
                                        }
                                        dragSelection = null
                                    },
                                    onUpdateEventText = store::updateEventText,
                                    onDeleteEvent = { eventId ->
                                        store.deleteEvent(eventId)
                                        if (selectedEventId == eventId) {
                                            selectedEventId = null
                                        }
                                    },
                                    onSelectEvent = { selectedEventId = it },
                                    onDeleteSelectedEvent = {
                                        val target = selectedEventId
                                        if (target != null) {
                                            store.deleteEvent(target)
                                            selectedEventId = null
                                        }
                                    },
                                    onClearSelectedEvent = { selectedEventId = null },
                                    onSelectMood = { dayIndex, moodIndex ->
                                        store.setMood(currentWeek, dayIndex, moodIndex)
                                    },
                                    onUpdateWeekSummary = { store.updateWeekSummary(currentWeek, it) }
                                )
                            }
                        }

                        if (isCompact && isSidePanelExpanded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.25f))
                                    .clickable { isSidePanelExpanded = false }
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .fillMaxHeight()
                                    .width(compactPanelWidth)
                            ) {
                                Sidebar(
                                    scheme = scheme,
                                    todos = state.todos,
                                    weekDays = weekDays,
                                    onAddTodo = store::addTodo,
                                    onToggleTodo = store::toggleTodo,
                                    onUpdateTodo = store::updateTodo,
                                    onDeleteTodo = store::removeTodo,
                                    width = compactPanelWidth
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Avoid leaving a dangling selection when recomposition changes weeks.
    LaunchedEffect(currentWeek) {
        dragSelection = null
        selectedEventId = null
        selectedImageId = null
    }
}

@Composable
private fun TopBar(
    currentWeek: Int,
    scheme: MonthScheme,
    isCompact: Boolean,
    isSidePanelExpanded: Boolean,
    isEditMode: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToggleSidePanel: () -> Unit,
    onToggleEditMode: () -> Unit,
    onOpenGallery: () -> Unit
) {
    val weekLabel = "Week $currentWeek · ${scheme.month}月"

    if (isCompact) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(scheme.accent.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                .border(1.dp, scheme.accent.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onPrev, enabled = currentWeek > 1) {
                    Text("<", color = scheme.accentText)
                }

                Text(
                    text = weekLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.accentText,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                TextButton(onClick = onNext, enabled = currentWeek < TOTAL_WEEKS) {
                    Text(">", color = scheme.accentText)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onToggleSidePanel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = scheme.accent,
                        contentColor = scheme.accentText
                    )
                ) {
                    Text(if (isSidePanelExpanded) "Close" else "Panel")
                }

                Button(
                    onClick = onToggleEditMode,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEditMode) scheme.accentText else Color.White,
                        contentColor = if (isEditMode) Color.White else scheme.accentText
                    )
                ) {
                    Text(if (isEditMode) "Edit ON" else "Edit OFF")
                }

                Button(
                    onClick = onOpenGallery,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = scheme.accent,
                        contentColor = scheme.accentText
                    )
                ) {
                    Text("Gallery")
                }
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(scheme.accent.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                .border(1.dp, scheme.accent.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onPrev, enabled = currentWeek > 1) {
                Text("<", color = scheme.accentText)
            }

            Text(
                text = weekLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.accentText,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            TextButton(onClick = onNext, enabled = currentWeek < TOTAL_WEEKS) {
                Text(">", color = scheme.accentText)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onToggleEditMode,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEditMode) scheme.accentText else Color.White,
                    contentColor = if (isEditMode) Color.White else scheme.accentText
                )
            ) {
                Text(if (isEditMode) "Edit ON" else "Edit OFF")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onOpenGallery,
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.accent,
                    contentColor = scheme.accentText
                )
            ) {
                Text("Gallery")
            }
        }
    }
}

@Composable
private fun GalleryTopBar(
    currentWeek: Int,
    scheme: MonthScheme,
    isCompact: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onBackToDiary: () -> Unit,
    onAutoLayout: () -> Unit,
    onAddImage: () -> Unit
) {
    val weekLabel = "Gallery · Week $currentWeek · ${scheme.month}月"

    if (isCompact) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(scheme.accent.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                .border(1.dp, scheme.accent.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onPrev, enabled = currentWeek > 1) {
                    Text("<", color = scheme.accentText)
                }

                Text(
                    text = weekLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.accentText,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                TextButton(onClick = onNext, enabled = currentWeek < TOTAL_WEEKS) {
                    Text(">", color = scheme.accentText)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBackToDiary,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = scheme.accentText
                    )
                ) {
                    Text("Back")
                }

                Button(
                    onClick = onAutoLayout,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = scheme.accentText
                    )
                ) {
                    Text("Auto")
                }

                Button(
                    onClick = onAddImage,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = scheme.accent,
                        contentColor = scheme.accentText
                    )
                ) {
                    Text("Add Image")
                }
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(scheme.accent.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                .border(1.dp, scheme.accent.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onPrev, enabled = currentWeek > 1) {
                Text("<", color = scheme.accentText)
            }

            Text(
                text = weekLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.accentText,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            TextButton(onClick = onNext, enabled = currentWeek < TOTAL_WEEKS) {
                Text(">", color = scheme.accentText)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onBackToDiary,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = scheme.accentText
                )
            ) {
                Text("Back To Diary")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onAutoLayout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = scheme.accentText
                )
            ) {
                Text("Auto Layout")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onAddImage,
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.accent,
                    contentColor = scheme.accentText
                )
            ) {
                Text("Add Image")
            }
        }
    }
}

@Composable
private fun GalleryBoard(
    scheme: MonthScheme,
    images: List<PlacedImage>,
    weekEvents: List<DiaryEvent>,
    selectedImageId: String?,
    onSelectImage: (String) -> Unit,
    onMoveImage: (imageId: String, dx: Float, dy: Float) -> Unit,
    onScaleImage: (imageId: String, zoomFactor: Float) -> Unit,
    onDeleteImage: (imageId: String) -> Unit,
    onSetImageTag: (imageId: String, eventId: String?) -> Unit
) {
    val selectedImage = images.firstOrNull { it.id == selectedImageId }
    val taggedEvent = selectedImage?.linkedEventId?.let { targetId ->
        weekEvents.firstOrNull { it.id == targetId }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, scheme.accent.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.accent.copy(alpha = 0.06f))
        )

        Text(
            text = "Drag to move. Pinch to resize.",
            style = MaterialTheme.typography.labelMedium,
            color = scheme.accentText.copy(alpha = 0.65f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        )

        if (images.isEmpty()) {
            Text(
                text = "Gallery is empty. Tap Add Image to start.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.accentText.copy(alpha = 0.75f),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        images.forEach { image ->
            val tagLabel = image.linkedEventId?.let { targetId ->
                weekEvents.firstOrNull { it.id == targetId }?.let { displayEventName(it) }
            }
            DraggableImage(
                image = image,
                accent = scheme.accent,
                isEditMode = true,
                isSelected = image.id == selectedImageId,
                tagLabel = tagLabel,
                onSelect = { onSelectImage(image.id) },
                onDrag = { dx, dy -> onMoveImage(image.id, dx, dy) },
                onScale = { zoomFactor -> onScaleImage(image.id, zoomFactor) },
                onDelete = { onDeleteImage(image.id) }
            )
        }

        if (selectedImage != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(10.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.95f))
                    .border(1.dp, scheme.accent.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "Selected image tag",
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.accentText,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Current: ${taggedEvent?.let { displayEventName(it) } ?: "No tag"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.accentText.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onSetImageTag(selectedImage.id, null) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = scheme.accentText
                        )
                    ) {
                        Text("Clear Tag")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (weekEvents.isEmpty()) {
                    Text(
                        text = "This week has no time blocks yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.accentText.copy(alpha = 0.7f)
                    )
                } else {
                    val eventListScroll = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .height(140.dp)
                            .verticalScroll(eventListScroll),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        weekEvents.forEach { event ->
                            val selectedTag = selectedImage.linkedEventId == event.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selectedTag) scheme.accent.copy(alpha = 0.25f)
                                        else scheme.accent.copy(alpha = 0.08f)
                                    )
                                    .clickable { onSetImageTag(selectedImage.id, event.id) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = displayEventName(event),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = scheme.accentText,
                                    modifier = Modifier.weight(1f)
                                )
                                if (selectedTag) {
                                    Text(
                                        text = "Tagged",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = scheme.accentText,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Sidebar(
    scheme: MonthScheme,
    todos: List<TodoItem>,
    weekDays: List<CalendarDay?>,
    onAddTodo: () -> Unit,
    onToggleTodo: (String) -> Unit,
    onUpdateTodo: (String, String) -> Unit,
    onDeleteTodo: (String) -> Unit,
    width: Dp
) {
    val editingState = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, scheme.accent.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Global Todo",
                fontWeight = FontWeight.Bold,
                color = scheme.accentText
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onAddTodo,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = scheme.accent,
                        contentColor = scheme.accentText
                    )
                ) {
                    Text("Add")
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(todos, key = { it.id }) { todo ->
                LaunchedEffect(todo.id) {
                    if (!editingState.containsKey(todo.id)) {
                        editingState[todo.id] = todo.text.isBlank()
                    }
                }
                val isEditing = editingState[todo.id] ?: false

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = todo.done,
                        onCheckedChange = { onToggleTodo(todo.id) }
                    )

                    if (isEditing) {
                        OutlinedTextField(
                            value = todo.text,
                            onValueChange = { onUpdateTodo(todo.id, it) },
                            modifier = Modifier.weight(1f),
                            singleLine = false,
                            minLines = 1,
                            maxLines = 4,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = if (todo.done) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            placeholder = { Text("Task") }
                        )

                        Button(
                            onClick = { editingState[todo.id] = false },
                            enabled = todo.text.isNotBlank(),
                            modifier = Modifier
                                .height(40.dp)
                                .padding(start = 6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = scheme.accentText,
                                disabledContainerColor = Color(0xFFEDEDED),
                                disabledContentColor = scheme.accentText.copy(alpha = 0.5f)
                            )
                        ) {
                            Text("Done")
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 10.dp, start = 4.dp, end = 4.dp)
                        ) {
                            Text(
                                text = todo.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.accentText,
                                textDecoration = if (todo.done) TextDecoration.LineThrough else TextDecoration.None
                            )
                        }

                        Button(
                            onClick = { editingState[todo.id] = true },
                            modifier = Modifier
                                .height(40.dp)
                                .padding(start = 6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = scheme.accentText
                            )
                        ) {
                            Text("Edit")
                        }
                    }

                    Button(
                        onClick = {
                            editingState.remove(todo.id)
                            onDeleteTodo(todo.id)
                        },
                        enabled = todos.size > 1,
                        modifier = Modifier
                            .height(40.dp)
                            .padding(start = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = scheme.accentText,
                            disabledContainerColor = Color(0xFFEDEDED),
                            disabledContentColor = scheme.accentText.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("Del")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        MiniCalendar(
            month = scheme.month,
            scheme = scheme,
            highlightedDays = weekDays
                .filterNotNull()
                .filter { it.month == scheme.month }
                .map { it.day }
                .toSet()
        )
    }
}

@Composable
private fun MiniCalendar(
    month: Int,
    scheme: MonthScheme,
    highlightedDays: Set<Int>
) {
    val (offset, dayCount, monthName) = remember(month) { monthMeta(month) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, scheme.accent.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "$monthName $YEAR",
            color = scheme.accentText,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DAY_NAMES.forEach { name ->
                Text(
                    text = name.first().toString(),
                    modifier = Modifier.width(18.dp),
                    color = scheme.accentText.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val cells = mutableListOf<Int?>()
        repeat(offset) { cells.add(null) }
        for (day in 1..dayCount) {
            cells.add(day)
        }
        while (cells.size % 7 != 0) {
            cells.add(null)
        }

        cells.chunked(7).forEach { weekRow ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                weekRow.forEach { day ->
                    val selected = day != null && day in highlightedDays
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (selected) scheme.accent
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day?.toString() ?: "",
                            color = if (selected) scheme.accentText else scheme.accentText.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun Planner(
    scheme: MonthScheme,
    weekDays: List<CalendarDay?>,
    week: Int,
    isCompact: Boolean,
    initialCompactPage: Int,
    shouldApplyInitialCompactPage: Boolean,
    onInitialCompactPageApplied: () -> Unit,
    isEditMode: Boolean,
    events: List<DiaryEvent>,
    selectedEventId: String?,
    moods: Map<String, Int>,
    weekSummary: String,
    dragSelection: DragSelection?,
    slotHeight: Dp,
    timeLabelWidth: Dp,
    onStartDrag: (dayIndex: Int, slot: Int) -> Unit,
    onDrag: (dayIndex: Int, slot: Int) -> Unit,
    onFinishDrag: () -> Unit,
    onUpdateEventText: (eventId: String, text: String) -> Unit,
    onDeleteEvent: (eventId: String) -> Unit,
    onSelectEvent: (eventId: String) -> Unit,
    onDeleteSelectedEvent: () -> Unit,
    onClearSelectedEvent: () -> Unit,
    onSelectMood: (dayIndex: Int, moodIndex: Int) -> Unit,
    onUpdateWeekSummary: (String) -> Unit
) {
    val desktopColumns = remember(weekDays) {
        weekDays.mapIndexed { dayIndex, day -> PlannerColumnItem.Day(dayIndex, day) }
    }
    val compactPages = remember(weekDays) { buildCompactPages(weekDays) }
    val pagerState = rememberPagerState(pageCount = { compactPages.size })
    val selectedEvent = remember(selectedEventId, events) {
        events.firstOrNull { it.id == selectedEventId }
    }

    LaunchedEffect(selectedEventId, events) {
        if (selectedEventId != null && selectedEvent == null) {
            onClearSelectedEvent()
        }
    }

    LaunchedEffect(isCompact, shouldApplyInitialCompactPage, initialCompactPage, compactPages.size) {
        if (isCompact && shouldApplyInitialCompactPage) {
            val targetPage = initialCompactPage.coerceIn(0, compactPages.lastIndex)
            pagerState.scrollToPage(targetPage)
            onInitialCompactPageApplied()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, scheme.accent.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
    ) {
        if (isEditMode && selectedEvent != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(scheme.accent.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val rangeText = "${DAY_NAMES[selectedEvent.dayIndex]} ${formatTimeBySlot(selectedEvent.startSlot)}-${formatTimeBySlot(selectedEvent.endSlot + 1)}"
                Text(
                    text = "Selected: $rangeText",
                    color = scheme.accentText,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onClearSelectedEvent) {
                    Text("Cancel", color = scheme.accentText)
                }
                Button(
                    onClick = onDeleteSelectedEvent,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = scheme.accentText,
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete")
                }
            }
        }

        if (isCompact) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(scheme.accent.copy(alpha = 0.14f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Page ${pagerState.currentPage + 1}/${compactPages.size}",
                    color = scheme.accentText,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = if (isEditMode) "Editing: drag creates blocks" else "Swipe left/right to switch days",
                    color = scheme.accentText.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = !isEditMode,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                PlannerPageContent(
                    scheme = scheme,
                    columns = compactPages[page],
                    week = week,
                    events = events,
                    moods = moods,
                    weekSummary = weekSummary,
                    isEditMode = isEditMode,
                    dragSelection = dragSelection,
                    slotHeight = slotHeight,
                    timeLabelWidth = timeLabelWidth,
                    onStartDrag = onStartDrag,
                    onDrag = onDrag,
                    onFinishDrag = onFinishDrag,
                    onUpdateEventText = onUpdateEventText,
                    onDeleteEvent = onDeleteEvent,
                    selectedEventId = selectedEventId,
                    onSelectEvent = onSelectEvent,
                    onSelectMood = onSelectMood,
                    onUpdateWeekSummary = onUpdateWeekSummary
                )
            }
        } else {
            PlannerPageContent(
                scheme = scheme,
                columns = desktopColumns,
                week = week,
                events = events,
                moods = moods,
                weekSummary = weekSummary,
                isEditMode = isEditMode,
                dragSelection = dragSelection,
                slotHeight = slotHeight,
                timeLabelWidth = timeLabelWidth,
                onStartDrag = onStartDrag,
                onDrag = onDrag,
                onFinishDrag = onFinishDrag,
                onUpdateEventText = onUpdateEventText,
                onDeleteEvent = onDeleteEvent,
                selectedEventId = selectedEventId,
                onSelectEvent = onSelectEvent,
                onSelectMood = onSelectMood,
                onUpdateWeekSummary = onUpdateWeekSummary
            )
        }
    }
}

@Composable
private fun PlannerPageContent(
    scheme: MonthScheme,
    columns: List<PlannerColumnItem>,
    week: Int,
    events: List<DiaryEvent>,
    moods: Map<String, Int>,
    weekSummary: String,
    isEditMode: Boolean,
    dragSelection: DragSelection?,
    slotHeight: Dp,
    timeLabelWidth: Dp,
    onStartDrag: (dayIndex: Int, slot: Int) -> Unit,
    onDrag: (dayIndex: Int, slot: Int) -> Unit,
    onFinishDrag: () -> Unit,
    onUpdateEventText: (eventId: String, text: String) -> Unit,
    onDeleteEvent: (eventId: String) -> Unit,
    selectedEventId: String?,
    onSelectEvent: (eventId: String) -> Unit,
    onSelectMood: (dayIndex: Int, moodIndex: Int) -> Unit,
    onUpdateWeekSummary: (String) -> Unit
) {
    val scroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(scheme.accent.copy(alpha = 0.2f))
        ) {
            columns.forEach { column ->
                when (column) {
                    is PlannerColumnItem.Day -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = DAY_NAMES[column.dayIndex],
                                color = scheme.accentText,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = if (column.day == null) "--" else "${column.day.month}/${column.day.day}",
                                color = scheme.accentText.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    PlannerColumnItem.Summary -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "WEEK SUMMARY",
                                color = scheme.accentText,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = "Sun note",
                                color = scheme.accentText.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(slotHeight * SLOTS_PER_DAY)
            ) {
                columns.forEach { column ->
                    when (column) {
                        is PlannerColumnItem.Day -> {
                            DayColumn(
                                modifier = Modifier.weight(1f),
                                enabled = column.day != null,
                                dayIndex = column.dayIndex,
                                scheme = scheme,
                                events = events.filter { it.dayIndex == column.dayIndex },
                                dragSelection = dragSelection,
                                isEditMode = isEditMode,
                                slotHeight = slotHeight,
                                timeLabelWidth = timeLabelWidth,
                                onStartDrag = onStartDrag,
                                onDrag = onDrag,
                                onFinishDrag = onFinishDrag,
                                onUpdateEventText = onUpdateEventText,
                                onDeleteEvent = onDeleteEvent,
                                selectedEventId = selectedEventId,
                                onSelectEvent = onSelectEvent
                            )
                        }

                        PlannerColumnItem.Summary -> {
                            SummaryColumn(
                                modifier = Modifier.weight(1f),
                                scheme = scheme,
                                text = weekSummary,
                                isEditMode = isEditMode,
                                onTextChange = onUpdateWeekSummary
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(scheme.accent.copy(alpha = 0.12f))
                .padding(vertical = 6.dp)
        ) {
            columns.forEach { column ->
                when (column) {
                    is PlannerColumnItem.Day -> {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (column.day == null) {
                                Text("", modifier = Modifier.height(30.dp))
                            } else {
                                MOOD_EMOJIS.forEachIndexed { moodIndex, emoji ->
                                    val selected = moods[moodKey(week, column.dayIndex)] == moodIndex
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (selected) scheme.accent.copy(alpha = 0.55f)
                                                else Color.Transparent
                                            )
                                            .clickable { onSelectMood(column.dayIndex, moodIndex) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = emoji,
                                            fontSize = 13.sp,
                                            lineHeight = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    PlannerColumnItem.Summary -> {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Summary",
                                color = scheme.accentText.copy(alpha = 0.75f),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayColumn(
    modifier: Modifier,
    enabled: Boolean,
    dayIndex: Int,
    scheme: MonthScheme,
    events: List<DiaryEvent>,
    dragSelection: DragSelection?,
    isEditMode: Boolean,
    slotHeight: Dp,
    timeLabelWidth: Dp,
    onStartDrag: (dayIndex: Int, slot: Int) -> Unit,
    onDrag: (dayIndex: Int, slot: Int) -> Unit,
    onFinishDrag: () -> Unit,
    onUpdateEventText: (eventId: String, text: String) -> Unit,
    onDeleteEvent: (eventId: String) -> Unit,
    selectedEventId: String?,
    onSelectEvent: (eventId: String) -> Unit
) {
    val dragModifier = if (enabled && isEditMode) {
        Modifier.pointerInput(enabled, isEditMode, events) {
            awaitPointerEventScope {
                while (true) {
                    val down = awaitPointerEvent().changes.firstOrNull { it.pressed } ?: continue
                    val startSlot = (down.position.y / slotHeight.toPx()).toInt().coerceIn(0, SLOTS_PER_DAY - 1)
                    val startsOnExisting = events.any { event ->
                        startSlot in event.startSlot..event.endSlot
                    }
                    if (startsOnExisting) {
                        continue
                    }
                    onStartDrag(dayIndex, startSlot)
                    down.consume()

                    var dragging = true
                    while (dragging) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue
                        val anyPressed = event.changes.any { it.pressed }

                        if (anyPressed) {
                            val slot = (change.position.y / slotHeight.toPx()).toInt().coerceIn(0, SLOTS_PER_DAY - 1)
                            onDrag(dayIndex, slot)
                            change.consume()
                        } else {
                            dragging = false
                        }
                    }

                    onFinishDrag()
                }
            }
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .border(0.5.dp, scheme.accent.copy(alpha = 0.35f))
            .background(if (enabled) Color.White else Color(0xFFF5F5F5))
            .then(dragModifier)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            repeat(SLOTS_PER_DAY) { slot ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(slotHeight)
                        .border(
                            width = if (slot % 2 == 0 && slot != 0) 0.8.dp else 0.4.dp,
                            color = if (slot % 2 == 0 && slot != 0) scheme.accent.copy(alpha = 0.45f) else scheme.accent.copy(alpha = 0.2f)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .width(timeLabelWidth)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (slot % 2 == 0) {
                            Text(
                                text = "${slot / 2 + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.accentText.copy(alpha = 0.75f)
                            )
                        }
                    }
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }

        if (dragSelection != null && dragSelection.dayIndex == dayIndex) {
            val topSlot = minOf(dragSelection.startSlot, dragSelection.endSlot)
            val bottomSlot = maxOf(dragSelection.startSlot, dragSelection.endSlot)
            Box(
                modifier = Modifier
                    .offset(y = slotHeight * topSlot)
                    .fillMaxWidth()
                    .height(slotHeight * (bottomSlot - topSlot + 1))
                    .background(scheme.accent.copy(alpha = 0.25f))
            )
        }

        events.forEach { event ->
            val heightSlots = (event.endSlot - event.startSlot + 1).coerceAtLeast(1)
            val isSelected = selectedEventId == event.id
            val readOnlyScroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .offset(y = slotHeight * event.startSlot + 2.dp)
                    .fillMaxWidth()
                    .height(slotHeight * heightSlots - 4.dp)
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(scheme.eventColor.copy(alpha = 0.9f))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) scheme.accentText else scheme.accent.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectEvent(event.id) }
                        .padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${formatTimeBySlot(event.startSlot)}-${formatTimeBySlot(event.endSlot + 1)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.accentText,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    if (isEditMode && isSelected) {
                        Text(
                            text = "Selected",
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.accentText.copy(alpha = 0.8f)
                        )
                    }
                }

                if (isEditMode) {
                    BasicTextField(
                        value = event.text,
                        onValueChange = { onUpdateEventText(event.id, it) },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = scheme.accentText),
                        cursorBrush = SolidColor(scheme.accentText),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (event.text.isEmpty()) {
                                    Text(
                                        text = "Add note",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = scheme.accentText.copy(alpha = 0.55f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(readOnlyScroll)
                    ) {
                        Text(
                            text = if (event.text.isBlank()) "(empty)" else event.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.accentText,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryColumn(
    modifier: Modifier,
    scheme: MonthScheme,
    text: String,
    isEditMode: Boolean,
    onTextChange: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .border(0.5.dp, scheme.accent.copy(alpha = 0.35f))
            .background(Color(0xFFFEFBF4))
            .padding(6.dp)
    ) {
        if (isEditMode) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxSize(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = scheme.accentText),
                cursorBrush = SolidColor(scheme.accentText),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (text.isEmpty()) {
                            Text(
                                text = "Write your weekly summary...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.accentText.copy(alpha = 0.55f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        } else {
            Text(
                text = if (text.isBlank()) "Turn on Edit Mode to write weekly summary." else text,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.accentText
            )
        }
    }
}

@Composable
private fun DraggableImage(
    image: PlacedImage,
    accent: Color,
    isEditMode: Boolean,
    isSelected: Boolean,
    tagLabel: String?,
    onSelect: () -> Unit,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onScale: (zoomFactor: Float) -> Unit,
    onDelete: () -> Unit
) {
    val imageDragModifier = if (isEditMode) {
        Modifier.pointerInput(image.id, isEditMode) {
            detectTransformGestures { _, pan, zoom, _ ->
                if (pan.x != 0f || pan.y != 0f) {
                    onDrag(pan.x, pan.y)
                }
                if (zoom != 1f) {
                    onScale(zoom)
                }
            }
        }
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(image.x.roundToInt(), image.y.roundToInt()) }
            .size(image.sizeDp.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accent else accent.copy(alpha = 0.7f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onSelect() }
            .then(imageDragModifier)
    ) {
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setBackgroundColor(AndroidColor.TRANSPARENT)
                }
            },
            update = { view ->
                view.setImageURI(Uri.parse(image.uri))
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!tagLabel.isNullOrBlank()) {
            Text(
                text = tagLabel,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.92f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        if (isEditMode) {
            TextButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f))
            ) {
                Text("x", color = accent)
            }
        }
    }
}
