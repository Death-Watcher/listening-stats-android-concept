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

data class TopItemsState(
    val items: List<Any> = emptyList(),
    val selectedType: ItemType = ItemType.TRACKS,
    val selectedRange: TimeRange = TimeRange.OVERALL,
    val loading: Boolean = false,
    val error: String? = null,
)

enum class ItemType { TRACKS, ARTISTS, ALBUMS, GENRES }

private val json = Json { ignoreUnknownKeys = true }

@Serializable
private data class TracksCache(val items: List<Track>)

@Serializable
private data class ArtistsCache(val items: List<Artist>)

@Serializable
private data class AlbumsCache(val items: List<Album>)

@Serializable
private data class GenresCache(val items: List<Genre>)

class TopItemsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StatsRepository()
    private val deezerApi = ApiClient.deezerApi
    private val settingsManager = SettingsManager(application)
    private val cacheManager = CacheManager(application)

    private val _state = MutableStateFlow(TopItemsState())
    val state: StateFlow<TopItemsState> = _state.asStateFlow()

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
    }

    fun load(type: ItemType = _state.value.selectedType, range: TimeRange = _state.value.selectedRange) {
        val cKey = "top_${type.name}_${range.name}"

        val cached = cacheManager.get(cKey)
        if (cached != null) {
            try {
                val items = when (type) {
                    ItemType.TRACKS -> json.decodeFromString<TracksCache>(cached).items
                    ItemType.ARTISTS -> json.decodeFromString<ArtistsCache>(cached).items
                    ItemType.ALBUMS -> json.decodeFromString<AlbumsCache>(cached).items
                    ItemType.GENRES -> json.decodeFromString<GenresCache>(cached).items
                }
                _state.update { it.copy(items = items, selectedType = type, selectedRange = range, error = null) }
            } catch (_: Exception) {}
        }

        _state.update { it.copy(selectedType = type, selectedRange = range, loading = true, error = null) }

        viewModelScope.launch {
            try {
                val user = lastFmUser; val key = lastFmApiKey
                if (user.isBlank() || key.isBlank()) {
                    _state.update { it.copy(loading = false, error = "Last.fm not configured") }
                    return@launch
                }
                val items = when (type) {
                    ItemType.TRACKS -> {
                        val raw = repository.getTopTracks(Provider.LAST_FM, user, key, "", range, 50).getOrDefault(emptyList())
                        val withArt = coroutineScope {
                            raw.take(10).map { t ->
                                if (t.albumArtUrl != null) t
                                else async { t.copy(albumArtUrl = searchAlbumArt(t.artist, t.name)) as Any }
                            }.mapNotNull { if (it is Track) it else (it as Deferred<Any>).await() as? Track } +
                            raw.drop(10)
                        }
                        try { cacheManager.set(cKey, json.encodeToString(TracksCache(withArt))) } catch (_: Exception) {}
                        withArt
                    }
                    ItemType.ARTISTS -> {
                        val raw = repository.getTopArtists(Provider.LAST_FM, user, key, "", range, 50).getOrDefault(emptyList())
                        val withArt = coroutineScope {
                            raw.take(10).map { a ->
                                if (a.imageUrl != null) a
                                else async { a.copy(imageUrl = searchArtistImage(a.name)) as Any }
                            }.mapNotNull { if (it is Artist) it else (it as Deferred<Any>).await() as? Artist } +
                            raw.drop(10)
                        }
                        try { cacheManager.set(cKey, json.encodeToString(ArtistsCache(withArt))) } catch (_: Exception) {}
                        withArt
                    }
                    ItemType.ALBUMS -> {
                        val raw = repository.getTopAlbums(Provider.LAST_FM, user, key, "", range, 50).getOrDefault(emptyList())
                        val withArt = coroutineScope {
                            raw.take(10).map { a ->
                                if (a.imageUrl != null) a
                                else async { a.copy(imageUrl = searchAlbumImage(a.artist, a.name)) as Any }
                            }.mapNotNull { if (it is Album) it else (it as Deferred<Any>).await() as? Album } +
                            raw.drop(10)
                        }
                        try { cacheManager.set(cKey, json.encodeToString(AlbumsCache(withArt))) } catch (_: Exception) {}
                        withArt
                    }
                    ItemType.GENRES -> {
                        val raw = repository.getGenres(key, user, range, 30).getOrDefault(emptyList())
                        try { cacheManager.set(cKey, json.encodeToString(GenresCache(raw))) } catch (_: Exception) {}
                        raw
                    }
                }
                _state.update { it.copy(items = items, loading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Error") }
            }
        }
    }

    private suspend fun searchAlbumArt(artist: String, song: String): String? {
        return try {
            val q = "artist:\"$artist\" \"$song\""
            deezerApi.searchTrack(q, 1).data.firstOrNull()?.album?.coverMedium
        } catch (_: Exception) { null }
    }

    private suspend fun searchArtistImage(artist: String): String? {
        return try {
            val q = "artist:\"$artist\""
            deezerApi.searchTrack(q, 1).data.firstOrNull()?.album?.coverMedium
        } catch (_: Exception) { null }
    }

    private suspend fun searchAlbumImage(artist: String, album: String): String? {
        return try {
            val q = "artist:\"$artist\" album:\"$album\""
            deezerApi.searchTrack(q, 1).data.firstOrNull()?.album?.coverMedium
        } catch (_: Exception) { null }
    }
}
