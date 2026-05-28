package com.listeningstats.app.domain

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.listeningstats.app.data.api.ApiClient
import com.listeningstats.app.data.local.CacheManager
import com.listeningstats.app.data.local.SettingsManager
import com.listeningstats.app.data.model.*
import com.listeningstats.app.data.repository.StatsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class DashboardState(
    val stats: StatsSummary = StatsSummary(),
    val recentTracks: List<RecentTrack> = emptyList(),
    val topTracks: List<Track> = emptyList(),
    val topArtists: List<Artist> = emptyList(),
    val topAlbums: List<Album> = emptyList(),
    val selectedRange: TimeRange = TimeRange.OVERALL,
    val loading: Boolean = false,
    val error: String? = null,
)

@Serializable
private data class DashboardCache(
    val stats: StatsSummary,
    val recentTracks: List<RecentTrack>,
    val topTracks: List<Track>,
    val topArtists: List<Artist>,
    val topAlbums: List<Album>,
    val range: String = "OVERALL",
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StatsRepository()
    private val deezerApi = ApiClient.deezerApi
    private val settingsManager = SettingsManager(application)
    private val cacheManager = CacheManager(application)
    private val json = Json { ignoreUnknownKeys = true }

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private var lastFmUser = ""
    private var lastFmApiKey = ""

    init {
        viewModelScope.launch {
            val user = settingsManager.lastFmUser.first()
            val key = settingsManager.lastFmApiKey.first()
            lastFmUser = user
            lastFmApiKey = key
        }
        viewModelScope.launch { settingsManager.lastFmUser.collect { lastFmUser = it } }
        viewModelScope.launch { settingsManager.lastFmApiKey.collect { lastFmApiKey = it } }
    }

    fun setTimeRange(range: TimeRange) {
        _state.update { it.copy(selectedRange = range) }
        refresh()
    }

    private fun cacheKey(range: TimeRange): String = "dashboard_${range.name}"

    private suspend fun searchAlbumArt(artist: String, song: String): String? {
        return try {
            val q = "artist:\"$artist\" \"$song\""
            val resp = deezerApi.searchTrack(q, 1)
            resp.data.firstOrNull()?.album?.coverMedium
        } catch (_: Exception) { null }
    }

    private suspend fun searchArtistImage(artist: String): String? {
        return try {
            val q = "artist:\"$artist\""
            val resp = deezerApi.searchTrack(q, 1)
            resp.data.firstOrNull()?.album?.coverMedium
        } catch (_: Exception) { null }
    }

    private suspend fun searchAlbumImage(artist: String, album: String): String? {
        return try {
            val q = "artist:\"$artist\" album:\"$album\""
            val resp = deezerApi.searchTrack(q, 1)
            resp.data.firstOrNull()?.album?.coverMedium
        } catch (_: Exception) { null }
    }

    fun refresh() {
        val user = lastFmUser
        val key = lastFmApiKey
        if (user.isBlank() || key.isBlank()) {
            _state.update { it.copy(loading = false) }
            return
        }

        val range = _state.value.selectedRange

        val cached = cacheManager.get(cacheKey(range))
        if (cached != null) {
            try {
                val c = json.decodeFromString<DashboardCache>(cached)
                _state.update {
                    it.copy(stats = c.stats, recentTracks = c.recentTracks,
                        topTracks = c.topTracks, topArtists = c.topArtists,
                        topAlbums = c.topAlbums, loading = true)
                }
            } catch (_: Exception) {}
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }

            val trackResult = repository.getTopTracks(Provider.LAST_FM, user, key, "", range, 10)
            val artistResult = repository.getTopArtists(Provider.LAST_FM, user, key, "", range, 10)
            val albumResult = repository.getTopAlbums(Provider.LAST_FM, user, key, "", range, 10)
            val recentResult = repository.getRecentTracks(Provider.LAST_FM, user, key, "", 10)

            val lastFmTracks = trackResult.getOrDefault(emptyList())
            val lastFmArtists = artistResult.getOrDefault(emptyList())
            val lastFmAlbums = albumResult.getOrDefault(emptyList())
            val lastFmRecent = recentResult.getOrDefault(emptyList())

            val tracksWithArt = coroutineScope {
                lastFmTracks.map { track ->
                    if (track.albumArtUrl != null) track
                    else async { track.copy(albumArtUrl = searchAlbumArt(track.artist, track.name)) }
                }.mapNotNull {
                    if (it is Track) it else (it as Deferred<Track>).await()
                }
            }

            val artistsWithArt = coroutineScope {
                lastFmArtists.map { artist ->
                    if (artist.imageUrl != null) artist
                    else async { artist.copy(imageUrl = searchArtistImage(artist.name)) }
                }.mapNotNull {
                    if (it is Artist) it else (it as Deferred<Artist>).await()
                }
            }

            val albumsWithArt = coroutineScope {
                lastFmAlbums.map { album ->
                    if (album.imageUrl != null) album
                    else async { album.copy(imageUrl = searchAlbumImage(album.artist, album.name)) }
                }.mapNotNull {
                    if (it is Album) it else (it as Deferred<Album>).await()
                }
            }

            val recentWithArt = coroutineScope {
                lastFmRecent.map { track ->
                    if (track.albumArtUrl != null) track
                    else async { track.copy(albumArtUrl = searchAlbumArt(track.artist, track.name)) }
                }.mapNotNull {
                    if (it is RecentTrack) it else (it as Deferred<RecentTrack>).await()
                }
            }

            val summary = StatsSummary(
                totalPlayCount = tracksWithArt.sumOf { it.playCount },
                topTrack = tracksWithArt.firstOrNull(),
                topArtist = artistsWithArt.firstOrNull(),
                topAlbum = albumsWithArt.firstOrNull(),
            )

            val cache = DashboardCache(summary, recentWithArt, tracksWithArt, artistsWithArt, albumsWithArt, range.name)
            try { cacheManager.set(cacheKey(range), json.encodeToString(cache)) } catch (_: Exception) {}

            _state.update {
                it.copy(stats = summary, recentTracks = recentWithArt,
                    topTracks = tracksWithArt, topArtists = artistsWithArt,
                    topAlbums = albumsWithArt, loading = false, error = null)
            }
        }
    }
}
