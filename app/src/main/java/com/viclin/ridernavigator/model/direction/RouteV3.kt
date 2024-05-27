package com.viclin.ridernavigator.model.direction

data class RouteV3 (
    val bounds: Bounds,
    val legs: List<Leg>,
    val overview_polyline: Polyline,
    val summary: String,
    val warnings: List<String>,
    val waypoint_order: List<Int>
)