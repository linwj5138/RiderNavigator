package com.viclin.ridernavigator.repository;

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.viclin.ridernavigator.BuildConfig
import com.viclin.ridernavigator.model.DirectionsResponse
import com.viclin.ridernavigator.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object DirectionsRepository {
    private const val API_KEY = BuildConfig.MAPS_API_KEY

    fun getRoute(startLatLng: LatLng, endLatLng: LatLng, callback: (List<LatLng>?) -> Unit) {
        val origin = "${startLatLng.latitude},${startLatLng.longitude}"
        val destination = "${endLatLng.latitude},${endLatLng.longitude}"

        val call = RetrofitClient.directionsApiService.getDirections(origin, destination, API_KEY)
        call.enqueue(object : Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>
            ) {
                if (response.isSuccessful) {
                    val routes = response.body()?.routes
                    if (routes != null && routes.isNotEmpty()) {
                        val points = routes[0].overview_polyline.points
                        val decodedPath = decodePolyline(points)
                        callback(decodedPath)
                    } else {
                        callback(null)
                    }
                } else {
                    Log.e("DirectionsRepository", "Response not successful: ${response.message()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                Log.e("DirectionsRepository", "Request failed: ${t.message}")
                callback(null)
            }
        })
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng((lat / 1E5), (lng / 1E5))
            poly.add(p)
        }

        return poly
    }
}

