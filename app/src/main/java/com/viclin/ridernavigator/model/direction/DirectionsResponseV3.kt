package com.viclin.ridernavigator.model.direction

data class DirectionsResponseV3(
    val geocoded_waypoints: List<GeocodedWaypoint>,
    val routes: List<RouteV3>,
    val status: String
)
