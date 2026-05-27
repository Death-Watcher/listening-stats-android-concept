package com.listeningstats.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.listeningstats.app.data.model.*
import com.listeningstats.app.domain.ItemType
import com.listeningstats.app.domain.TopItemsViewModel
import com.listeningstats.app.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopItemsScreen(
    viewModel: TopItemsViewModel,
    initialType: ItemType = ItemType.TRACKS,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var selectedType by remember { mutableStateOf(initialType) }
    var selectedRange by remember { mutableStateOf(TimeRange.OVERALL) }
    var showRangeMenu by remember { mutableStateOf(false) }

    LaunchedEffect(selectedType, selectedRange) {
        viewModel.load(selectedType, selectedRange)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (selectedType) {
                            ItemType.TRACKS -> "Top Tracks"
                            ItemType.ARTISTS -> "Top Artists"
                            ItemType.ALBUMS -> "Top Albums"
                        }
                    )
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    Box {
                        TextButton(onClick = { showRangeMenu = true }) {
                            Text(
                                when (selectedRange) {
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
                                    text = {
                                        Text(
                                            when (range) {
                                                TimeRange.WEEK -> "Week"
                                                TimeRange.MONTH -> "Month"
                                                TimeRange.THREE_MONTHS -> "3 Months"
                                                TimeRange.SIX_MONTHS -> "6 Months"
                                                TimeRange.YEAR -> "Year"
                                                TimeRange.OVERALL -> "Overall"
                                            }
                                        )
                                    },
                                    onClick = { selectedRange = range; showRangeMenu = false }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                ItemType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = ItemType.entries.size),
                    ) {
                        Text(
                            when (type) {
                                ItemType.TRACKS -> "Tracks"
                                ItemType.ARTISTS -> "Artists"
                                ItemType.ALBUMS -> "Albums"
                            }
                        )
                    }
                }
            }

            when {
                state.loading -> LoadingIndicator()
                state.error != null -> ErrorView(message = state.error!!) { viewModel.load(selectedType, selectedRange) }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(state.items) { index, item ->
                            when (item) {
                                is Track -> TrackListItem(track = item, rank = index + 1)
                                is Artist -> ArtistListItem(artist = item, rank = index + 1)
                                is Album -> AlbumListItem(album = item, rank = index + 1)
                                is Genre -> GenreListItem(genre = item, rank = index + 1)
                            }
                            if (index < state.items.size - 1) {
                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
        }
    }
}
