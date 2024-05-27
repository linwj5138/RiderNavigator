package com.viclin.ridernavigator.network.service

import com.viclin.ridernavigator.model.DirectionsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsApiService {
    @GET("directions/json")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String,
        @Query("mode") mode: String = "WALKING",
        @Query("travelMode") travelMode: String = "BICYCLE"
    ): Call<DirectionsResponse>
}
