package com.listeningstats.app.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listeningstats.app.domain.DashboardViewModel
import com.listeningstats.app.ui.theme.SpotifyGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareCardScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Share Cards") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            var selectedType by remember { mutableStateOf("story") }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = selectedType == "story",
                    onClick = { selectedType = "story" },
                    label = { Text("Story") },
                )
                FilterChip(
                    selected = selectedType == "landscape",
                    onClick = { selectedType = "landscape" },
                    label = { Text("Landscape") },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val cardWidth = if (selectedType == "story") 280.dp else 360.dp
            val cardHeight = if (selectedType == "story") 480.dp else 200.dp

            Card(
                modifier = Modifier.width(cardWidth).height(cardHeight),
                shape = RoundedCornerShape(if (selectedType == "story") 32.dp else 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF191414)),
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    Column {
                        Text(
                            text = "Listening Stats",
                            style = MaterialTheme.typography.labelSmall,
                            color = SpotifyGreen,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Top Track",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f),
                        )
                        Text(
                            text = state.stats.topTrack?.name ?: "N/A",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Top Artist",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f),
                        )
                        Text(
                            text = state.stats.topArtist?.name ?: "N/A",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "${state.stats.totalPlayCount} total plays",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.4f),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    captureCard(view, selectedType)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Card")
            }
        }
    }
}

private fun captureCard(view: View, type: String) {
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    view.draw(canvas)
}
