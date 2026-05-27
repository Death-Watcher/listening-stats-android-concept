package com.listeningstats.app.data.repository

import com.listeningstats.app.data.api.ApiClient
import com.listeningstats.app.data.api.LastFmMapper
import com.listeningstats.app.data.api.StatsFmMapper
import com.listeningstats.app.data.model.*
import kotlinx.serialization.json.jsonPrimitive

class StatsRepository {

    private val lastFmApi = ApiClient.lastFmApi
    private val statsFmApi = ApiClient.statsFmApi

    suspend fun getTopTracks(
        provider: Provider,
        lastFmUser: String? = null,
        lastFmApiKey: String? = null,
        statsFmUser: String? = null,
        timeRange: TimeRange = TimeRange.OVERALL,
        limit: Int = 50,
    ): Result<List<Track>> = runCatching {
        when (provider) {
            Provider.LAST_FM -> {
                val response = lastFmApi.getTopTracks(
                    user = lastFmUser ?: "",
                    period = timeRange.toLastFm(),
                    limit = limit,
                    apiKey = lastFmApiKey ?: "",
                )
                response.toptracks?.track?.map { LastFmMapper.toTrack(it) }
                    ?: throw Exception(response.message ?: "Failed to fetch top tracks")
            }
            Provider.STATS_FM -> {
                val response = statsFmApi.getTopTracks(
                    userId = statsFmUser ?: "",
                    range = timeRange.toStatsFm(),
                    limit = limit,
                )
                response.items.map { StatsFmMapper.toTrack(it) }
            }
        }
    }

    suspend fun getTopArtists(
        provider: Provider,
        lastFmUser: String? = null,
        lastFmApiKey: String? = null,
        statsFmUser: String? = null,
        timeRange: TimeRange = TimeRange.OVERALL,
        limit: Int = 50,
    ): Result<List<Artist>> = runCatching {
        when (provider) {
            Provider.LAST_FM -> {
                val response = lastFmApi.getTopArtists(
                    user = lastFmUser ?: "",
                    period = timeRange.toLastFm(),
                    limit = limit,
                    apiKey = lastFmApiKey ?: "",
                )
                response.topartists?.artist?.map { LastFmMapper.toArtist(it) }
                    ?: throw Exception(response.message ?: "Failed to fetch top artists")
            }
            Provider.STATS_FM -> {
                val response = statsFmApi.getTopArtists(
                    userId = statsFmUser ?: "",
                    range = timeRange.toStatsFm(),
                    limit = limit,
                )
                response.items.map { StatsFmMapper.toArtist(it) }
            }
        }
    }

    suspend fun getTopAlbums(
        provider: Provider,
        lastFmUser: String? = null,
        lastFmApiKey: String? = null,
        statsFmUser: String? = null,
        timeRange: TimeRange = TimeRange.OVERALL,
        limit: Int = 50,
    ): Result<List<Album>> = runCatching {
        when (provider) {
            Provider.LAST_FM -> {
                val response = lastFmApi.getTopAlbums(
                    user = lastFmUser ?: "",
                    period = timeRange.toLastFm(),
                    limit = limit,
                    apiKey = lastFmApiKey ?: "",
                )
                response.topalbums?.album?.map { LastFmMapper.toAlbum(it) }
                    ?: throw Exception(response.message ?: "Failed to fetch top albums")
            }
            Provider.STATS_FM -> {
                emptyList()
            }
        }
    }

    suspend fun getRecentTracks(
        provider: Provider,
        lastFmUser: String? = null,
        lastFmApiKey: String? = null,
        statsFmUser: String? = null,
        limit: Int = 50,
    ): Result<List<RecentTrack>> = runCatching {
        when (provider) {
            Provider.LAST_FM -> {
                val response = lastFmApi.getRecentTracks(
                    user = lastFmUser ?: "",
                    limit = limit,
                    apiKey = lastFmApiKey ?: "",
                )
                response.recenttracks?.track?.map { LastFmMapper.toRecentTrack(it) }
                    ?: throw Exception(response.message ?: "Failed to fetch recent tracks")
            }
            Provider.STATS_FM -> {
                val response = statsFmApi.getRecentStreams(
                    userId = statsFmUser ?: "",
                    limit = limit,
                )
                response.items.map { StatsFmMapper.toRecentTrack(it) }
            }
        }
    }

    suspend fun getArtistInfo(
        artistName: String,
        apiKey: String,
    ): Result<Artist> = runCatching {
        val response = lastFmApi.getArtistInfo(
            artist = artistName,
            apiKey = apiKey,
        )
        response.artist?.let { LastFmMapper.toArtistInfo(it) }
            ?: throw Exception("Failed to fetch artist info")
    }

    suspend fun getGenres(
        apiKey: String,
        username: String,
        timeRange: TimeRange = TimeRange.OVERALL,
        limit: Int = 30,
    ): Result<List<Genre>> = runCatching {
        val response = lastFmApi.getTopArtists(
            user = username,
            period = timeRange.toLastFm(),
            limit = limit,
            apiKey = apiKey,
        )
        val artists = response.topartists?.artist ?: emptyList()
        val genreCounts = mutableMapOf<String, Int>()
        for (artist in artists.take(20)) {
            val infoResp = lastFmApi.getArtistInfo(
                artist = artist.name,
                apiKey = apiKey,
            )
            val tags = infoResp.artist?.tags?.tag?.map { it.name.lowercase() } ?: emptyList()
            val genreTags = tags.filter { tag ->
                val invalid = listOf("seen live", "all", "mysterious", "male", "female", "electronic", "rock", "pop", "hip-hop", "rnb", "jazz", "classical", "metal", "punk", "indie", "alternative", "folk", "country", "blues", "soul", "funk", "reggae", "dance", "world", "latin")
                tag !in invalid
            }
            val genre = genreTags.firstOrNull() ?: "Other"
            val count = try { artist.playcount?.jsonPrimitive?.content?.toIntOrNull() ?: 1 } catch (_: Exception) { 1 }
            genreCounts[genre] = (genreCounts[genre] ?: 0) + count
        }
        genreCounts.map { (name, count) -> Genre(name = name.replaceFirstChar { it.uppercase() }, playCount = count) }
            .sortedByDescending { it.playCount }
            .take(limit)
    }

    private fun TimeRange.toStatsFm(): String = when (this) {
        TimeRange.WEEK -> "7days"
        TimeRange.MONTH -> "month"
        TimeRange.THREE_MONTHS -> "3months"
        TimeRange.SIX_MONTHS -> "6months"
        TimeRange.YEAR -> "12months"
        TimeRange.OVERALL -> "lifetime"
    }
}
