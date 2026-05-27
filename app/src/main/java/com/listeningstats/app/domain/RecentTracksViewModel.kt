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

data class RecentTracksState(
    val tracks: List<RecentTrack> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
)

@Serializable
private data class RecentTracksCache(val tracks: List<RecentTrack>)

class RecentTracksViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StatsRepository()
    private val deezerApi = ApiClient.deezerApi
    private val settingsManager = SettingsManager(application)
    private val cacheManager = CacheManager(application)
    private val json = Json { ignoreUnknownKeys = true }

    private val _state = MutableStateFlow(RecentTracksState())
    val state: StateFlow<RecentTracksState> = _state.asStateFlow()

    private var lastFmUser = ""
    private var lastFmApiKey = ""

    init {
        viewModelScope.launch {
            val u = settingsManager.lastFmUser.first()
            val k = settingsManager.lastFmApiKey.first()
            lastFmUser = u; lastFmApiKey = k
        }
        viewModelScope.launch {
            combine(settingsManager.lastFmUser, settingsManager.lastFmApiKey) { u, k ->
                lastFmUser = u; lastFmApiKey = k
            }.collect()
        }
        viewModelScope.launch {
            val cached = cacheManager.get("recent_tracks") ?: return@launch
            try { _state.update { it.copy(tracks = json.decodeFromString<RecentTracksCache>(cached).tracks) } } catch (_: Exception) {}
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val user = lastFmUser; val key = lastFmApiKey
                if (user.isBlank() || key.isBlank()) {
                    _state.update { it.copy(loading = false, error = "Last.fm not configured") }
                    return@launch
                }
                val raw = repository.getRecentTracks(Provider.LAST_FM, user, key, "", 50).getOrDefault(emptyList())
                val tracks = coroutineScope {
                    raw.map { t ->
                        if (t.albumArtUrl != null) t
                        else async { t.copy(albumArtUrl = searchAlbumArt(t.artist, t.name)) }
                    }.mapNotNull {
                        if (it is RecentTrack) it else (it as Deferred<RecentTrack>).await()
                    }
                }
                try { cacheManager.set("recent_tracks", json.encodeToString(RecentTracksCache(tracks))) } catch (_: Exception) {}
                _state.update { it.copy(tracks = tracks, loading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    private suspend fun searchAlbumArt(artist: String, song: String): String? {
        return try {
            val q = "artist:\"$artist\" \"$song\""
            deezerApi.searchTrack(q, 1).data.firstOrNull()?.album?.coverMedium
        } catch (_: Exception) { null }
    }
}
