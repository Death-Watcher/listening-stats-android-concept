package com.listeningstats.app.data.api

import com.listeningstats.app.data.api.dto.*
import com.listeningstats.app.data.model.*

object StatsFmMapper {

    fun toRecentTrack(dto: StatsFmRecentStream): RecentTrack {
        return RecentTrack(
            name = dto.trackName ?: "Unknown",
            artist = "stats.fm",
            playedAt = dto.endTime,
        )
    }

    fun toTrack(dto: StatsFmTopTrackItem): Track {
        val artists = dto.track?.artists?.joinToString(", ") { it.name ?: "" } ?: "Unknown"
        return Track(
            name = dto.track?.name ?: "Unknown",
            artist = artists,
            album = dto.track?.albums?.firstOrNull()?.name,
            albumArtUrl = dto.track?.albums?.firstOrNull()?.image,
            playCount = dto.streams ?: 0,
            duration = dto.track?.durationMs,
        )
    }

    fun toArtist(dto: StatsFmTopArtistItem): Artist {
        return Artist(
            name = dto.artist?.name ?: "Unknown",
            imageUrl = dto.artist?.image,
            playCount = dto.streams ?: 0,
            tags = dto.artist?.genres ?: emptyList(),
        )
    }
}
