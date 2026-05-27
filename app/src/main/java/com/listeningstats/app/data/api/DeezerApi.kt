package com.listeningstats.app.data.api

import com.listeningstats.app.data.api.dto.DeezerSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DeezerApi {

    @GET("/search")
    suspend fun searchTrack(
        @Query("q") query: String,
        @Query("limit") limit: Int = 1,
    ): DeezerSearchResponse
}
