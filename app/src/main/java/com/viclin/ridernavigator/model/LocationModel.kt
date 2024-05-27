package com.viclin.ridernavigator.model

import com.google.gson.annotations.SerializedName

data class LocationModel(
    @SerializedName("location") val location: LatLngParentModel
)