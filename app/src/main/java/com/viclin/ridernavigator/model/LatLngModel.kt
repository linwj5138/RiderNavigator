package com.viclin.ridernavigator.model

import com.google.gson.annotations.SerializedName

data class LatLngModel(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)
