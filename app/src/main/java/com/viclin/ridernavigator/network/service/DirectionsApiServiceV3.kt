package com.viclin.ridernavigator.network.service

import com.viclin.ridernavigator.BuildConfig
import com.viclin.ridernavigator.model.direction.DirectionsResponseV3
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * api: https://maps.googleapis.com/maps/api/directions/outputFormat?parameters
 * guide: https://developers.google.com/maps/documentation/directions/get-directions?hl=zh-cn#maps_http_directions_sydney_perth_waypoints_latlng-txt
 */
interface DirectionsApiServiceV3 {
    @GET("directions/json")
    suspend fun getDirectionsV3(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String = BuildConfig.MAPS_API_KEY,
        @Query("mode") mode: String = "WALKING",
        @Query("language") language: String = "zh-TW",
        @Query("units") units: String = "METRIC",
        @Query("travelMode") travelMode: String = "BICYCLE"
    ): DirectionsResponseV3
}