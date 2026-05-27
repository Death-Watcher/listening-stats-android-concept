package com.listeningstats.app.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeezerSearchResponse(
    val data: List<DeezerTrack> = emptyList(),
)

@Serializable
data class DeezerTrack(
    val album: DeezerAlbum? = null,
)

@Serializable
data class DeezerAlbum(
    val cover: String? = null,
    @SerialName("cover_medium") val coverMedium: String? = null,
    @SerialName("cover_big") val coverBig: String? = null,
    val title: String? = null,
)
