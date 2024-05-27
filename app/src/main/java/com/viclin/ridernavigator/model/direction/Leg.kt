package com.viclin.ridernavigator.model.direction

data class Leg(
    val distance: Distance,
    val duration: Duration,
    val end_address: String,
    val end_location: LocationV3,
    val start_address: String,
    val start_location: LocationV3,
    val steps: List<Step>
)
