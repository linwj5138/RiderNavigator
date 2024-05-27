package com.viclin.ridernavigator.model

data class RouteV2(
    val distanceMeters: Int,
    val duration: String,
    val polyline: OverviewPolylineV2
)
