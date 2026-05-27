package com.listeningstats.app.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.listeningstats.app.data.model.Album
import com.listeningstats.app.data.model.Artist
import com.listeningstats.app.data.model.Genre
import com.listeningstats.app.data.model.RecentTrack
import com.listeningstats.app.data.model.Track
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object StatsExporter {

    fun exportToJson(
        context: Context,
        tracks: List<Track>,
        artists: List<Artist>,
        albums: List<Album>,
        recentTracks: List<RecentTrack>,
        genres: List<Genre>,
    ): Uri? {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
        val file = File(context.cacheDir, "listening_stats_export_$timestamp.json")

        val json = buildString {
            appendLine("{")
            appendLine("  \"exportDate\": \"${LocalDateTime.now()}\",")
            appendLine("  \"topTracks\": [")
            tracks.forEachIndexed { i, t ->
                appendLine("    {")
                appendLine("      \"rank\": ${i + 1},")
                appendLine("      \"name\": \"${t.name.replace("\"", "\\\"")}\",")
                appendLine("      \"artist\": \"${t.artist.replace("\"", "\\\"")}\",")
                appendLine("      \"playCount\": ${t.playCount}")
                appendLine("    }${if (i < tracks.size - 1) "," else ""}")
            }
            appendLine("  ],")
            appendLine("  \"topArtists\": [")
            artists.forEachIndexed { i, a ->
                appendLine("    {")
                appendLine("      \"rank\": ${i + 1},")
                appendLine("      \"name\": \"${a.name.replace("\"", "\\\"")}\",")
                appendLine("      \"playCount\": ${a.playCount}")
                appendLine("    }${if (i < artists.size - 1) "," else ""}")
            }
            appendLine("  ],")
            appendLine("  \"topAlbums\": [")
            albums.forEachIndexed { i, a ->
                appendLine("    {")
                appendLine("      \"rank\": ${i + 1},")
                appendLine("      \"name\": \"${a.name.replace("\"", "\\\"")}\",")
                appendLine("      \"artist\": \"${a.artist.replace("\"", "\\\"")}\",")
                appendLine("      \"playCount\": ${a.playCount}")
                appendLine("    }${if (i < albums.size - 1) "," else ""}")
            }
            appendLine("  ],")
            appendLine("  \"recentTracks\": [")
            recentTracks.forEachIndexed { i, t ->
                appendLine("    {")
                appendLine("      \"name\": \"${t.name.replace("\"", "\\\"")}\",")
                appendLine("      \"artist\": \"${t.artist.replace("\"", "\\\"")}\",")
                appendLine("      \"playedAt\": \"${t.playedAt ?: ""}\"")
                appendLine("    }${if (i < recentTracks.size - 1) "," else ""}")
            }
            appendLine("  ],")
            appendLine("  \"topGenres\": [")
            genres.forEachIndexed { i, g ->
                appendLine("    {")
                appendLine("      \"name\": \"${g.name}\",")
                appendLine("      \"playCount\": ${g.playCount}")
                appendLine("    }${if (i < genres.size - 1) "," else ""}")
            }
            appendLine("  ]")
            appendLine("}")
        }

        return try {
            file.writeText(json)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            null
        }
    }

    fun exportToCsv(
        context: Context,
        tracks: List<Track>,
        artists: List<Artist>,
        albums: List<Album>,
        recentTracks: List<RecentTrack>,
    ): Uri? {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
        val file = File(context.cacheDir, "listening_stats_export_$timestamp.csv")

        val csv = buildString {
            appendLine("Top Tracks")
            appendLine("Rank,Name,Artist,Play Count")
            tracks.forEachIndexed { i, t ->
                appendLine("${i + 1},\"${t.name}\",\"${t.artist}\",${t.playCount}")
            }
            appendLine()
            appendLine("Top Artists")
            appendLine("Rank,Name,Play Count")
            artists.forEachIndexed { i, a ->
                appendLine("${i + 1},\"${a.name}\",${a.playCount}")
            }
            appendLine()
            appendLine("Recent Tracks")
            appendLine("Name,Artist,Played At")
            recentTracks.forEach {
                appendLine("\"${it.name}\",\"${it.artist}\",${it.playedAt ?: ""}")
            }
        }

        return try {
            file.writeText(csv)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            null
        }
    }

    fun shareFile(context: Context, uri: Uri, mimeType: String = "application/json") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Stats"))
    }
}
