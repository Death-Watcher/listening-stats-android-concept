package com.listeningstats.app.domain

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.listeningstats.app.data.local.CacheManager
import com.listeningstats.app.data.local.SettingsManager
import com.listeningstats.app.data.model.*
import com.listeningstats.app.data.repository.StatsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class ActivityState(
    val activity: ActivityData = ActivityData(),
    val heatmap: List<HeatmapEntry> = emptyList(),
    val totalPlayCount: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val loading: Boolean = false,
    val error: String? = null,
)

@Serializable
private data class ActivityCache(
    val activity: ActivityData,
    val heatmap: List<HeatmapEntry>,
    val totalPlayCount: Int,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
)

class ActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StatsRepository()
    private val settingsManager = SettingsManager(application)
    private val cacheManager = CacheManager(application)
    private val json = Json { ignoreUnknownKeys = true }

    private val _state = MutableStateFlow(ActivityState())
    val state: StateFlow<ActivityState> = _state.asStateFlow()

    private var lastFmUser = ""
    private var lastFmApiKey = ""
    private var statsFmUser = ""

    init {
        viewModelScope.launch {
            settingsManager.lastFmUser.collect { lastFmUser = it }
        }
        viewModelScope.launch {
            settingsManager.lastFmApiKey.collect { lastFmApiKey = it }
        }
        viewModelScope.launch {
            settingsManager.statsFmUser.collect { statsFmUser = it }
        }
    }

    private fun parseTimestamp(timestamp: String): Long {
        return try {
            timestamp.toLong() * 1000
        } catch (_: NumberFormatException) {
            try {
                java.time.Instant.parse(timestamp).toEpochMilli()
            } catch (_: Exception) {
                try {
                    java.time.LocalDateTime.parse(timestamp, java.time.format.DateTimeFormatter.ISO_DATE_TIME)
                        .toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                } catch (_: Exception) {
                    java.time.LocalDate.parse(timestamp.take(10)).atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
                }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val lfDef = async {
                    if (lastFmUser.isNotBlank() && lastFmApiKey.isNotBlank())
                        repository.getRecentTracks(Provider.LAST_FM, lastFmUser, lastFmApiKey, "", 200).getOrDefault(emptyList())
                    else emptyList()
                }
                val sfDef = async {
                    if (statsFmUser.isNotBlank())
                        repository.getRecentTracks(Provider.STATS_FM, "", "", statsFmUser, 500).getOrDefault(emptyList())
                    else emptyList()
                }

                val allRecent = lfDef.await() + sfDef.await()

                val hourly = mutableMapOf<Int, Int>()
                val weekly = mutableMapOf<String, Int>()
                val monthly = mutableMapOf<String, Int>()
                val heatmapEntries = mutableListOf<HeatmapEntry>()

                allRecent.forEach { track ->
                    track.playedAt?.let { timestamp ->
                        try {
                            val millis = parseTimestamp(timestamp)
                            val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
                            val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                            hourly[hour] = (hourly[hour] ?: 0) + 1

                            val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                            val dayIndex = (cal.get(java.util.Calendar.DAY_OF_WEEK) - 2 + 7) % 7
                            val dayName = dayNames[dayIndex]
                            weekly[dayName] = (weekly[dayName] ?: 0) + 1

                            val monthKey = "%d-%02d".format(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1)
                            monthly[monthKey] = (monthly[monthKey] ?: 0) + 1

                            val dateKey = "%d-%02d-%02d".format(
                                cal.get(java.util.Calendar.YEAR),
                                cal.get(java.util.Calendar.MONTH) + 1,
                                cal.get(java.util.Calendar.DAY_OF_MONTH)
                            )
                            heatmapEntries.add(HeatmapEntry(dateKey, 1))
                        } catch (_: Exception) {}
                    }
                }

                val aggregatedHeatmap = heatmapEntries
                    .groupBy { it.date }
                    .map { (date, entries) -> HeatmapEntry(date, entries.sumOf { it.count }) }
                    .sortedBy { it.date }

                val activityData = ActivityData(hourly = hourly, weekly = weekly, monthly = monthly)

                val dateSet = aggregatedHeatmap.filter { it.count > 0 }.map { it.date }.toSet()
                val today = java.time.LocalDate.now()
                val fmt = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

                val currentStreak = run {
                    var streak = 0
                    var d = today
                    if (dateSet.contains(d.format(fmt))) {
                        streak = 1; d = d.minusDays(1)
                    } else {
                        d = d.minusDays(1)
                        if (dateSet.contains(d.format(fmt))) {
                            streak = 1; d = d.minusDays(1)
                        }
                    }
                    while (dateSet.contains(d.format(fmt))) { streak++; d = d.minusDays(1) }
                    streak
                }

                val longestStreak = run {
                    val sorted = dateSet.sorted()
                    if (sorted.isEmpty()) 0
                    else {
                        var maxRun = 1; var run = 1
                        for (i in 1 until sorted.size) {
                            val prev = java.time.LocalDate.parse(sorted[i - 1])
                            val curr = java.time.LocalDate.parse(sorted[i])
                            if (java.time.temporal.ChronoUnit.DAYS.between(prev, curr) == 1L) { run++ }
                            else { maxRun = maxOf(maxRun, run); run = 1 }
                        }
                        maxOf(maxRun, run)
                    }
                }

                try {
                    cacheManager.set("activity_v2", json.encodeToString(ActivityCache(activityData, aggregatedHeatmap, allRecent.size, currentStreak, longestStreak)))
                } catch (_: Exception) {}

                _state.update {
                    it.copy(activity = activityData, heatmap = aggregatedHeatmap, totalPlayCount = allRecent.size, currentStreak = currentStreak, longestStreak = longestStreak, loading = false)
                }
            } catch (e: Exception) {
                val cached = cacheManager.get("activity_v2")
                if (cached != null) {
                    try {
                        val c = json.decodeFromString<ActivityCache>(cached)
                        _state.update { it.copy(activity = c.activity, heatmap = c.heatmap, totalPlayCount = c.totalPlayCount, currentStreak = c.currentStreak, longestStreak = c.longestStreak, loading = false) }
                    } catch (_: Exception) {
                        _state.update { it.copy(loading = false, error = e.message) }
                    }
                } else {
                    _state.update { it.copy(loading = false, error = e.message) }
                }
            }
        }
    }
}
