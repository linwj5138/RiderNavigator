package com.viclin.ridernavigator.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil

data class OverviewPolylineV2(
    val encodedPolyline:String,
) {
    val polyLine: List<LatLng>
        get() = PolyUtil.decode(encodedPolyline).map { LatLng(it.latitude, it.longitude) }
}
