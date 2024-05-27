package com.viclin.ridernavigator.model.direction

data class Step(
    val distance: Distance,
    val duration: Duration,
    val end_location: LocationV3,
    val html_instructions: String,
    val polyline: Polyline,
    val start_location: LocationV3,
    val travel_mode: String
)
