package com.listeningstats.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.listeningstats.app.data.model.*
import com.listeningstats.app.domain.DashboardViewModel
import com.listeningstats.app.ui.components.*
import com.listeningstats.app.data.model.TimeRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onSeeAllTracks: () -> Unit,
    onSeeAllArtists: () -> Unit,
    onSeeAllAlbums: () -> Unit,
    onSeeAllRecent: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var showRangeMenu by remember { mutableStateOf(false) }
    val hasData = state.stats.topTrack != null

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listening Stats", fontWeight = FontWeight.Bold) },
                actions = {
                    Box {
                        TextButton(onClick = { showRangeMenu = true }) {
                            Text(
                                when (state.selectedRange) {
                                    TimeRange.WEEK -> "Week"
                                    TimeRange.MONTH -> "Month"
                                    TimeRange.THREE_MONTHS -> "3 Months"
                                    TimeRange.SIX_MONTHS -> "6 Months"
                                    TimeRange.YEAR -> "Year"
                                    TimeRange.OVERALL -> "Overall"
                                }
                            )
                        }
                        DropdownMenu(expanded = showRangeMenu, onDismissRequest = { showRangeMenu = false }) {
                            TimeRange.entries.forEach { range ->
                                DropdownMenuItem(
                                    text = { Text(
                                        when (range) {
                                            TimeRange.WEEK -> "Week"
                                            TimeRange.MONTH -> "Month"
                                            TimeRange.THREE_MONTHS -> "3 Months"
                                            TimeRange.SIX_MONTHS -> "6 Months"
                                            TimeRange.YEAR -> "Year"
                                            TimeRange.OVERALL -> "Overall"
                                        }
                                    )},
                                    onClick = {
                                        showRangeMenu = false
                                        viewModel.setTimeRange(range)
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { padding ->
        if (state.loading && !hasData) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.error != null && !hasData) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Text(state.error!!, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.refresh() }) { Text("Retry") }
            }
        } else {
            PullToRefreshBox(
                isRefreshing = state.loading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    // Summary Cards Row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SummaryCard(
                            title = "Tracks",
                            value = if (state.topTracks.isNotEmpty()) { val s = state.topTracks.sumOf { it.playCount }; if (s > 0) "$s" else "-" } else "-",
                            subtitle = state.stats.topTrack?.name ?: "No data",
                            modifier = Modifier.weight(1f),
                        )
                        SummaryCard(
                            title = "Artists",
                            value = if (state.topArtists.isNotEmpty()) { val s = state.topArtists.sumOf { it.playCount }; if (s > 0) "$s" else "-" } else "-",
                            subtitle = state.stats.topArtist?.name ?: "No data",
                            modifier = Modifier.weight(1f),
                        )
                        SummaryCard(
                            title = "Albums",
                            value = if (state.topAlbums.isNotEmpty()) { val s = state.topAlbums.sumOf { it.playCount }; if (s > 0) "$s" else "-" } else "-",
                            subtitle = state.stats.topAlbum?.name ?: "No data",
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // Provider indicator
                    val hasLf = state.topTracks.isNotEmpty() || state.stats.topTrack != null
                    val hasSf = state.recentTracks.any { it.artist == "stats.fm" }
                    if (hasLf || hasSf) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val label = buildString {
                                if (hasLf) append("Last.fm")
                                if (hasLf && hasSf) append(" + ")
                                if (hasSf) append("stats.fm")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    // Top Tracks Section
                    SectionHeader(title = "Top Tracks", onSeeAll = onSeeAllTracks)
                    state.topTracks.take(5).forEachIndexed { i, track ->
                        TrackRow(track = track, rank = i + 1)
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // Top Artists Section
                    SectionHeader(title = "Top Artists", onSeeAll = onSeeAllArtists)
                    state.topArtists.take(5).forEachIndexed { i, artist ->
                        ArtistRow(artist = artist, rank = i + 1)
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // Top Albums Section
                    SectionHeader(title = "Top Albums", onSeeAll = onSeeAllAlbums)
                    state.topAlbums.take(3).forEachIndexed { i, album ->
                        AlbumRow(album = album, rank = i + 1)
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // Recently Played Section
                    SectionHeader(title = "Recently Played", onSeeAll = onSeeAllRecent)
                    state.recentTracks.take(3).forEachIndexed { i, track ->
                        RecentRow(track = track)
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll) { Text("See All") }
        }
    }
}

@Composable
private fun TrackRow(track: Track, rank: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$rank", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (track.albumArtUrl != null) {
            AsyncImage(
                model = track.albumArtUrl,
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(track.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(if (track.playCount > 0) "${track.playCount}" else "-", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ArtistRow(artist: Artist, rank: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$rank", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (artist.imageUrl != null) {
            AsyncImage(
                model = artist.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(artist.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
        Text(if (artist.playCount > 0) "${artist.playCount}" else "-", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AlbumRow(album: Album, rank: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$rank", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (album.imageUrl != null) {
            AsyncImage(
                model = album.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Album, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(album.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(album.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(if (album.playCount > 0) "${album.playCount}" else "-", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RecentRow(track: com.listeningstats.app.data.model.RecentTrack) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (track.albumArtUrl != null) {
            AsyncImage(
                model = track.albumArtUrl,
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(track.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
