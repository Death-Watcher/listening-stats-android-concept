package com.listeningstats.app.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatsFmItemsResponse<T>(
    val items: List<T> = emptyList(),
    val total: Int? = null,
)

@Serializable
data class StatsFmTopTrackItem(
    val position: Int? = null,
    val streams: Int? = null,
    val indicator: String? = null,
    val track: StatsFmTrack? = null,
)

@Serializable
data class StatsFmTopArtistItem(
    val position: Int? = null,
    val streams: Int? = null,
    val indicator: String? = null,
    val artist: StatsFmArtistFull? = null,
)

@Serializable
data class StatsFmTrack(
    val id: Long? = null,
    val name: String? = null,
    val artists: List<StatsFmArtistRef>? = null,
    val albums: List<StatsFmAlbumRef>? = null,
    val durationMs: Long? = null,
    val explicit: Boolean? = null,
)

@Serializable
data class StatsFmArtistRef(
    val id: Long? = null,
    val name: String? = null,
    val image: String? = null,
)

@Serializable
data class StatsFmArtistFull(
    val id: Long? = null,
    val name: String? = null,
    val image: String? = null,
    val followers: Int? = null,
    val genres: List<String>? = null,
)

@Serializable
data class StatsFmAlbumRef(
    val id: Long? = null,
    val name: String? = null,
    val image: String? = null,
)

@Serializable
data class StatsFmRecentStream(
    val id: String? = null,
    val userId: String? = null,
    val endTime: String? = null,
    val playedMs: Long? = null,
    val trackId: Long? = null,
    val trackName: String? = null,
    val albumId: Long? = null,
    val artistIds: List<Long>? = null,
    val importId: Long? = null,
)
