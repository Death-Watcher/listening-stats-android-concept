package com.listeningstats.app.data.api

import com.listeningstats.app.data.api.dto.*
import com.listeningstats.app.data.model.*
import android.util.Log
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

object LastFmMapper {

    private fun JsonElement?.toIntOrZero(): Int {
        if (this == null) return 0
        try { return jsonPrimitive.content.toIntOrNull() ?: 0 } catch (_: Exception) {}
        return 0
    }

    private fun JsonElement?.toStringOrNull(): String? {
        if (this == null) return null
        try { return jsonPrimitive.content } catch (_: Exception) {}
        return null
    }

    fun toTrack(dto: LastFmTrack): Track {
        val playcount = dto.playcount.toIntOrZero()
        val duration = dto.duration?.toLongOrNull()
        return Track(
            name = dto.name,
            artist = dto.artist?.text ?: dto.artist?.name ?: "Unknown",
            albumArtUrl = extractImage(dto.image),
            playCount = playcount,
            url = dto.url,
            duration = duration,
        )
    }

    fun toArtist(dto: LastFmArtist): Artist {
        val playcount = dto.playcount.toIntOrZero()
        val listeners = dto.listeners.toIntOrZero()
        return Artist(
            name = dto.name,
            imageUrl = extractImage(dto.image),
            playCount = playcount,
            listeners = listeners,
            url = dto.url,
        )
    }

    fun toAlbum(dto: LastFmAlbum): Album {
        val playcount = dto.playcount.toIntOrZero()
        return Album(
            name = dto.name,
            artist = dto.artist?.text ?: dto.artist?.name ?: "Unknown",
            imageUrl = extractImage(dto.image),
            playCount = playcount,
            url = dto.url,
        )
    }

    fun toRecentTrack(dto: LastFmRecentTrack): RecentTrack {
        val albumName = dto.album?.text ?: dto.album?.textAlt
        val dateUts = dto.date?.uts.toStringOrNull() ?: dto.date?.text ?: dto.date?.textAlt
        return RecentTrack(
            name = dto.name,
            artist = dto.artist?.text ?: dto.artist?.name ?: "Unknown",
            album = albumName,
            albumArtUrl = extractImage(dto.image),
            playedAt = dateUts,
            url = dto.url,
            nowPlaying = dto.attr?.nowplaying == "true",
        )
    }

    fun toArtistInfo(dto: LastFmArtistFull): Artist {
        val playcount = dto.stats?.playcount.toIntOrZero()
        val listeners = dto.stats?.listeners.toIntOrZero()
        val tags = dto.tags?.tag?.map { it.name } ?: emptyList()
        return Artist(
            name = dto.name ?: "Unknown",
            imageUrl = extractImage(dto.image),
            playCount = playcount,
            listeners = listeners,
            tags = tags,
        )
    }

    private val PLACEHOLDER_HASHES = setOf(
        "2a96cbd8b46e442fc41c2b86b821562f",
        "c6f59c1e5e7240a4c0d427abd71f3dbb",
    )

    private fun isPlaceholderImage(url: String): Boolean =
        PLACEHOLDER_HASHES.any { url.contains(it) }

    fun extractImage(dto: List<LastFmImage>?): String? {
        if (dto.isNullOrEmpty()) {
            Log.d("LastFmMapper", "extractImage: null or empty")
            return null
        }
        val sorted = listOf("mega", "extralarge", "large", "medium", "small")
        for (size in sorted) {
            val url = dto.find { it.size == size }?.url
            if (!url.isNullOrBlank()) {
                if (isPlaceholderImage(url)) {
                    Log.d("LastFmMapper", "extractImage: $size url=$url is placeholder, skipping")
                    continue
                }
                Log.d("LastFmMapper", "extractImage: found $size url=$url")
                return url
            }
        }
        val fallback = dto.lastOrNull()?.url
        Log.d("LastFmMapper", "extractImage: fallback url=$fallback")
        return if (!fallback.isNullOrBlank() && !isPlaceholderImage(fallback)) fallback else null
    }
}
