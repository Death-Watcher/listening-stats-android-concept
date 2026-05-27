package com.listeningstats.app.data.api

import com.listeningstats.app.data.api.dto.LastFmResponse
import com.listeningstats.app.data.api.dto.LastFmArtistGetInfoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LastFmApi {

    @GET("/2.0/")
    suspend fun getTopArtists(
        @Query("method") method: String = "user.gettopartists",
        @Query("user") user: String,
        @Query("period") period: String = "overall",
        @Query("limit") limit: Int = 50,
        @Query("page") page: Int = 1,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json",
    ): LastFmResponse

    @GET("/2.0/")
    suspend fun getTopTracks(
        @Query("method") method: String = "user.gettoptracks",
        @Query("user") user: String,
        @Query("period") period: String = "overall",
        @Query("limit") limit: Int = 50,
        @Query("page") page: Int = 1,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json",
    ): LastFmResponse

    @GET("/2.0/")
    suspend fun getTopAlbums(
        @Query("method") method: String = "user.gettopalbums",
        @Query("user") user: String,
        @Query("period") period: String = "overall",
        @Query("limit") limit: Int = 50,
        @Query("page") page: Int = 1,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json",
    ): LastFmResponse

    @GET("/2.0/")
    suspend fun getRecentTracks(
        @Query("method") method: String = "user.getrecenttracks",
        @Query("user") user: String,
        @Query("limit") limit: Int = 50,
        @Query("page") page: Int = 1,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json",
    ): LastFmResponse

    @GET("/2.0/")
    suspend fun getArtistInfo(
        @Query("method") method: String = "artist.getinfo",
        @Query("artist") artist: String,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json",
    ): LastFmArtistGetInfoResponse

    @GET("/2.0/")
    suspend fun getWeeklyTrackChart(
        @Query("method") method: String = "user.getweeklytrackchart",
        @Query("user") user: String,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json",
    ): LastFmResponse

    @GET("/2.0/")
    suspend fun getWeeklyArtistChart(
        @Query("method") method: String = "user.getweeklyartistchart",
        @Query("user") user: String,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json",
    ): LastFmResponse

    @GET("/2.0/")
    suspend fun getWeeklyAlbumChart(
        @Query("method") method: String = "user.getweeklyalbumchart",
        @Query("user") user: String,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json",
    ): LastFmResponse
}
