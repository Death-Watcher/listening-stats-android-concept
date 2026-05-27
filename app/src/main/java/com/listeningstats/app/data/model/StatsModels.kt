package com.listeningstats.app.data.model

import kotlinx.serialization.Serializable

enum class TimeRange {
    WEEK, MONTH, THREE_MONTHS, SIX_MONTHS, YEAR, OVERALL;

    fun toLastFm(): String = when (this) {
        WEEK -> "7day"
        MONTH -> "1month"
        THREE_MONTHS -> "3month"
        SIX_MONTHS -> "6month"
        YEAR -> "12month"
        OVERALL -> "overall"
    }
}

enum class Provider {
    LAST_FM, STATS_FM
}

@Serializable
data class Track(
    val name: String,
    val artist: String,
    val album: String? = null,
    val albumArtUrl: String? = null,
    val playCount: Int = 0,
    val url: String? = null,
    val duration: Long? = null,
)

@Serializable
data class Artist(
    val name: String,
    val imageUrl: String? = null,
    val playCount: Int = 0,
    val listeners: Int = 0,
    val url: String? = null,
    val tags: List<String> = emptyList(),
)

@Serializable
data class Album(
    val name: String,
    val artist: String,
    val imageUrl: String? = null,
    val playCount: Int = 0,
    val url: String? = null,
)

@Serializable
data class Genre(
    val name: String,
    val playCount: Int = 0,
)

@Serializable
data class RecentTrack(
    val name: String,
    val artist: String,
    val album: String? = null,
    val albumArtUrl: String? = null,
    val playedAt: String? = null,
    val url: String? = null,
    val nowPlaying: Boolean = false,
)

@Serializable
data class TrackPlay(
    val track: Track,
    val timestamp: Long,
)

@Serializable
data class WeeklyChart(
    val weekStart: String,
    val weekEnd: String,
    val tracks: List<Track> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
)

@Serializable
data class StatsSummary(
    val totalTracks: Int = 0,
    val totalArtists: Int = 0,
    val totalAlbums: Int = 0,
    val totalPlayCount: Int = 0,
    val topTrack: Track? = null,
    val topArtist: Artist? = null,
    val topAlbum: Album? = null,
    val topGenre: Genre? = null,
)

@Serializable
data class ActivityData(
    val hourly: Map<Int, Int> = emptyMap(),
    val weekly: Map<String, Int> = emptyMap(),
    val monthly: Map<String, Int> = emptyMap(),
)

@Serializable
data class HeatmapEntry(
    val date: String,
    val count: Int,
)
