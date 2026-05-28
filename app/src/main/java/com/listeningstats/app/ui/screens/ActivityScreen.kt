package com.listeningstats.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listeningstats.app.domain.ActivityViewModel
import com.listeningstats.app.ui.components.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        val hasData = state.activity.hourly.isNotEmpty()
        if (state.loading && !hasData) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.error != null && !hasData) {
            ErrorView(message = state.error!!, onRetry = { viewModel.load() })
        } else {
            PullToRefreshBox(
                isRefreshing = state.loading,
                onRefresh = { viewModel.load() },
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                Text(
                    text = "Activity",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Based on your ${state.totalPlayCount} most recent plays",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Calendar Heatmap (GitHub-style)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Listening Heatmap", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        CalendarHeatmap(heatmapData = state.heatmap)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Listening Streaks
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${state.currentStreak}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Day Streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${state.longestStreak}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Longest Streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hourly Activity
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Hourly Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        BarChart(data = state.activity.hourly.mapKeys { "${it.key}:00" }, maxBars = 24)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weekly Activity
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Weekly Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        val dayOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                        BarChart(data = dayOrder.associateWith { state.activity.weekly[it] ?: 0 })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Monthly Activity
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Monthly Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        BarChart(data = state.activity.monthly)
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun CalendarHeatmap(heatmapData: List<com.listeningstats.app.data.model.HeatmapEntry>) {
    val heatmapMap = heatmapData.associate { it.date to it.count }
    val maxCount = heatmapMap.values.maxOrNull() ?: 1

    val endDate = LocalDate.now()
    val startDate = endDate.minusDays(90)
    val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1

    val cellSize = 14.dp
    val cellGap = 3.dp
    val labelWidth = 28.dp

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(labelWidth))
            // weeks will fill remaining space
        }
        Spacer(modifier = Modifier.height(4.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.width(labelWidth),
            ) {
                listOf("Mon", "", "Wed", "", "Fri", "", "").forEachIndexed { i, day ->
                    Box(modifier = Modifier.height(cellSize), contentAlignment = Alignment.CenterStart) {
                        Text(day, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (i < 6) Spacer(modifier = Modifier.height(cellGap))
                }
            }

            Column {
                val weeks = (totalDays + 6) / 7
                for (week in 0 until weeks) {
                    Row {
                        for (dayOfWeek in 0..6) {
                            val dayIndex = week * 7 + dayOfWeek
                            if (dayIndex < totalDays) {
                                val date = startDate.plusDays(dayIndex.toLong())
                                val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                val count = heatmapMap[dateStr] ?: 0
                                val intensity = if (maxCount > 0) count.toFloat() / maxCount else 0f
                                val color = when {
                                    count == 0 -> MaterialTheme.colorScheme.surface
                                    intensity > 0.75f -> MaterialTheme.colorScheme.primary
                                    intensity > 0.5f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    intensity > 0.25f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(cellSize)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(color),
                                )
                            } else {
                                Spacer(modifier = Modifier.size(cellSize))
                            }
                            if (dayOfWeek < 6) Spacer(modifier = Modifier.width(cellGap))
                        }
                    }
                    if (week < weeks - 1) Spacer(modifier = Modifier.height(cellGap))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Less", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(4.dp))
            listOf(0f, 0.2f, 0.4f, 0.7f, 1f).forEach { intensity ->
                val color = when {
                    intensity == 0f -> MaterialTheme.colorScheme.surface
                    intensity > 0.75f -> MaterialTheme.colorScheme.primary
                    intensity > 0.5f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    intensity > 0.25f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                }
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
                Spacer(modifier = Modifier.width(2.dp))
            }
            Text("More", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BarChart(data: Map<String, Int>, maxBars: Int = Int.MAX_VALUE) {
    val maxValue = data.values.maxOrNull() ?: 1
    val sortedData = data.entries.take(maxBars)

    Column(modifier = Modifier.fillMaxWidth()) {
        sortedData.forEach { (label, count) ->
            Row(
                modifier = Modifier.fillMaxWidth().height(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    run {
                        val monthRegex = Regex("""^(\d{4})-(\d{2})$""")
                        monthRegex.find(label)?.let { m ->
                            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                            "${months[m.groupValues[2].toInt() - 1]} ${m.groupValues[1].takeLast(2)}"
                        } ?: label
                    },
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(48.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = count.toFloat() / maxValue.coerceAtLeast(1))
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
                Text(
                    "$count",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
        }
    }
}
