package com.listeningstats.app.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class LastFmResponse(
    val topartists: LastFmTopArtists? = null,
    val toptracks: LastFmTopTracks? = null,
    val topalbums: LastFmTopAlbums? = null,
    val recenttracks: LastFmRecentTracks? = null,
    val artist: LastFmArtistInfo? = null,
    val weeklytrackchart: LastFmWeeklyTrackChart? = null,
    val weeklyartistchart: LastFmWeeklyArtistChart? = null,
    val weeklyalbumchart: LastFmWeeklyAlbumChart? = null,
    val error: Int? = null,
    val message: String? = null,
)

@Serializable
data class LastFmTopArtists(
    val artist: List<LastFmArtist>,
    @SerialName("@attr") val attr: LastFmAttr? = null,
)

@Serializable
data class LastFmTopTracks(
    val track: List<LastFmTrack>,
    @SerialName("@attr") val attr: LastFmAttr? = null,
)

@Serializable
data class LastFmTopAlbums(
    val album: List<LastFmAlbum>,
    @SerialName("@attr") val attr: LastFmAttr? = null,
)

@Serializable
data class LastFmRecentTracks(
    val track: List<LastFmRecentTrack>,
    @SerialName("@attr") val attr: LastFmAttr? = null,
)

@Serializable
data class LastFmArtistInfo(
    val name: String,
    val url: String? = null,
    val image: List<LastFmImage>? = null,
    val stats: LastFmStats? = null,
    val tags: LastFmTags? = null,
)

@Serializable
data class LastFmWeeklyTrackChart(
    val track: List<LastFmTrack> = emptyList(),
    @SerialName("@attr") val attr: LastFmWeeklyAttr? = null,
)

@Serializable
data class LastFmWeeklyArtistChart(
    val artist: List<LastFmArtist> = emptyList(),
    @SerialName("@attr") val attr: LastFmWeeklyAttr? = null,
)

@Serializable
data class LastFmWeeklyAlbumChart(
    val album: List<LastFmAlbum> = emptyList(),
    @SerialName("@attr") val attr: LastFmWeeklyAttr? = null,
)

@Serializable
data class LastFmAttr(
    val user: String? = null,
    val page: String? = null,
    val perPage: String? = null,
    val totalPages: String? = null,
    val total: String? = null,
)

@Serializable
data class LastFmWeeklyAttr(
    val user: String? = null,
    val from: String? = null,
    val to: String? = null,
)

@Serializable
data class LastFmArtist(
    val name: String,
    val playcount: JsonElement? = null,
    val listeners: JsonElement? = null,
    val url: String? = null,
    val image: List<LastFmImage>? = null,
    val streamable: String? = null,
)

@Serializable
data class LastFmTrack(
    val name: String,
    val artist: LastFmTrackArtist? = null,
    val playcount: JsonElement? = null,
    val listeners: JsonElement? = null,
    val url: String? = null,
    val image: List<LastFmImage>? = null,
    val duration: String? = null,
)

@Serializable
data class LastFmAlbum(
    val name: String,
    val artist: LastFmTrackArtist? = null,
    val playcount: JsonElement? = null,
    val url: String? = null,
    val image: List<LastFmImage>? = null,
    val mbid: String? = null,
)

@Serializable
data class LastFmRecentTrack(
    val name: String,
    val artist: LastFmTrackArtist? = null,
    val album: LastFmRecentAlbum? = null,
    val url: String? = null,
    val image: List<LastFmImage>? = null,
    val date: LastFmDate? = null,
    @SerialName("@attr") val attr: LastFmNowPlayingAttr? = null,
)

@Serializable
data class LastFmTrackArtist(
    @SerialName("#text") val text: String? = null,
    val name: String? = null,
    val mbid: String? = null,
    val url: String? = null,
)

@Serializable
data class LastFmRecentAlbum(
    val text: String? = null,
    @SerialName("#text") val textAlt: String? = null,
)

@Serializable
data class LastFmImage(
    @SerialName("#text") val url: String? = null,
    val size: String? = null,
)

@Serializable
data class LastFmDate(
    val uts: JsonElement? = null,
    val text: String? = null,
    @SerialName("#text") val textAlt: String? = null,
)

@Serializable
data class LastFmNowPlayingAttr(
    val nowplaying: String? = null,
)

@Serializable
data class LastFmStats(
    val playcount: JsonElement? = null,
    val listeners: JsonElement? = null,
)

@Serializable
data class LastFmTags(
    val tag: List<LastFmTag>? = null,
)

@Serializable
data class LastFmTag(
    val name: String,
    val url: String? = null,
)

@Serializable
data class LastFmArtistGetInfoResponse(
    val artist: LastFmArtistFull? = null,
)

@Serializable
data class LastFmArtistFull(
    val name: String? = null,
    val stats: LastFmStats? = null,
    val tags: LastFmTags? = null,
    val image: List<LastFmImage>? = null,
)
