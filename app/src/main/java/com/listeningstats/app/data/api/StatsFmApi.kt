package com.listeningstats.app.data.api

import com.listeningstats.app.data.api.dto.StatsFmItemsResponse
import com.listeningstats.app.data.api.dto.StatsFmTopTrackItem
import com.listeningstats.app.data.api.dto.StatsFmTopArtistItem
import com.listeningstats.app.data.api.dto.StatsFmRecentStream
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StatsFmApi {

    @GET("/api/v1/users/{userId}/top/tracks")
    suspend fun getTopTracks(
        @Path("userId") userId: String,
        @Query("range") range: String = "lifetime",
        @Query("limit") limit: Int = 50,
    ): StatsFmItemsResponse<StatsFmTopTrackItem>

    @GET("/api/v1/users/{userId}/top/artists")
    suspend fun getTopArtists(
        @Path("userId") userId: String,
        @Query("range") range: String = "lifetime",
        @Query("limit") limit: Int = 50,
    ): StatsFmItemsResponse<StatsFmTopArtistItem>

    @GET("/api/v1/users/{userId}/top/albums")
    suspend fun getTopAlbums(
        @Path("userId") userId: String,
        @Query("range") range: String = "lifetime",
        @Query("limit") limit: Int = 50,
    ): StatsFmItemsResponse<Unit>

    @GET("/api/v1/users/{userId}/streams")
    suspend fun getRecentStreams(
        @Path("userId") userId: String,
        @Query("limit") limit: Int = 50,
    ): StatsFmItemsResponse<StatsFmRecentStream>
}
