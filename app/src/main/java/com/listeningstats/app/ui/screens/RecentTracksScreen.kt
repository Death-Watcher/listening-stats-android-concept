package com.listeningstats.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import com.listeningstats.app.domain.RecentTracksViewModel
import com.listeningstats.app.ui.components.ErrorView
import com.listeningstats.app.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentTracksScreen(
    viewModel: RecentTracksViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recently Played") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        val hasData = state.tracks.isNotEmpty()
        when {
            state.loading && !hasData -> LoadingIndicator()
            state.error != null && !hasData -> ErrorView(message = state.error!!, onRetry = { viewModel.load() })
            else -> {
                PullToRefreshBox(
                    isRefreshing = state.loading,
                    onRefresh = { viewModel.load() },
                    modifier = Modifier.fillMaxSize().padding(padding),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        itemsIndexed(state.tracks) { index, track ->
                            RecentTrackItem(track = track)
                            if (index < state.tracks.size - 1) {
                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentTrackItem(track: com.listeningstats.app.data.model.RecentTrack) {
    ListItem(
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(track.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                if (track.nowPlaying) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("NOW", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        },
        supportingContent = { Text(track.artist, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingContent = {
            if (track.albumArtUrl != null) {
                coil.compose.AsyncImage(
                    model = track.albumArtUrl,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                )
            } else {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
    )
}
