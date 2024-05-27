package com.viclin.ridernavigator.model

import com.google.gson.annotations.SerializedName

data class LatLngParentModel(
    @SerializedName("latLng") val latLng: LatLngModel
)
