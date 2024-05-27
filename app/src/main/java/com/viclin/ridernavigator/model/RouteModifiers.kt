package com.viclin.ridernavigator.model

import com.google.gson.annotations.SerializedName

data class RouteModifiers(
    @SerializedName("avoidTolls") val avoidTolls: Boolean = false,
    @SerializedName("avoidHighways") val avoidHighways: Boolean = false,
    @SerializedName("avoidFerries") val avoidFerries: Boolean = false
)
