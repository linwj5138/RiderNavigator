package com.viclin.ridernavigator.network.service

import com.viclin.ridernavigator.BuildConfig
import com.viclin.ridernavigator.model.DirectionRequestV2
import com.viclin.ridernavigator.model.DirectionsResponseV2
import com.viclin.ridernavigator.model.LocationModel
import com.viclin.ridernavigator.model.RouteModifiers
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

/**
 *
 * 目标地址：https://routes.googleapis.com/directions/v2:computeRoutes
 * 接口说明：https://developers.google.com/maps/documentation/routes/compute_route_directions?hl=zh_CN
 *
 */

//获取两点之间的导航路径
interface DirectionsApiServiceV2 {
    @Headers(
        "Content-Type: application/json",
        "X-Goog-Api-Key: ${BuildConfig.MAPS_API_KEY}",
        "X-Goog-FieldMask: routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline"
    )
    @POST("directions/v2:computeRoutes")
    suspend fun getDirectionV2(@Body request: DirectionRequestV2): DirectionsResponseV2
}