package com.viclin.ridernavigator.model.direction

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil

data class Polyline(
    val points: String
) {
    val polyLine: List<LatLng>
        get() = PolyUtil.decode(points).map { LatLng(it.latitude, it.longitude) }
}
