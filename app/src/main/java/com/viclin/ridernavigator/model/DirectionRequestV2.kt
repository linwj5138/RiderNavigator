package com.viclin.ridernavigator.model

import com.google.gson.annotations.SerializedName

data class DirectionRequestV2(
    @SerializedName("origin") val origin: LocationModel,
    @SerializedName("destination") val destination: LocationModel,
    @SerializedName("travelMode") val travelMode: String = "DRIVE",
    @SerializedName("languageCode") val languageCode: String = "zh-TW",
    @SerializedName("routeModifiers") val routeModifiers: RouteModifiers = RouteModifiers(),
    @SerializedName("units") val units: String = "METRIC",
    @SerializedName("routingPreference") val routingPreference: String = "TRAFFIC_AWARE",
    @SerializedName("computeAlternativeRoutes") val computeAlternativeRoutes: Boolean = false
)