package com.viclin.ridernavigator.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.PolyUtil
import com.viclin.ridernavigator.model.DirectionRequestV2
import com.viclin.ridernavigator.model.LatLngModel
import com.viclin.ridernavigator.model.LatLngParentModel
import com.viclin.ridernavigator.model.LocationModel
import com.viclin.ridernavigator.model.direction.DirectionsResponseV3
import com.viclin.ridernavigator.network.RetrofitClient
import com.viclin.ridernavigator.repository.DirectionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.Timer
import kotlin.concurrent.timerTask

class MapViewModel : ViewModel() {
    private val _currentLocation = MutableLiveData<LatLng>()
    val currentLocation: LiveData<LatLng> get() = _currentLocation

    private val _destinationMarker = MutableLiveData<Marker?>()
    val destinationMarker: LiveData<Marker?> get() = _destinationMarker

    private val _currentPolyline = MutableLiveData<Polyline?>()
    val currentPolyline: LiveData<Polyline?> get() = _currentPolyline

    private val _tripPath = MutableLiveData<MutableList<LatLng>>()
    val tripPath: MutableLiveData<MutableList<LatLng>> get() = _tripPath

    private val _isPathPlanned = MutableLiveData<Boolean>()
    val isPathPlanned: LiveData<Boolean> get() = _isPathPlanned

    private val _isNavigating = MutableLiveData<Boolean>(false)
    val isNavigating: LiveData<Boolean> get() = _isNavigating

//    private val _isPermissionAllowed = MutableLiveData<Boolean>()
//    val isPermissionAllowed: LiveData<Boolean> = _isPermissionAllowed

    private val _elapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long> get() = _elapsedTime

    private val _navStartTime = MutableLiveData<Long>(0L)
    val navStartTime: LiveData<Long> get() = _navStartTime

    private val _navEndTime = MutableLiveData<Long>(0L)
    val navEndTime: LiveData<Long> get() = _navEndTime

    private val _totalDistance = MutableLiveData<Float>()
    val totalDistance: LiveData<Float> get() = _totalDistance

    private val _route = MutableLiveData<List<LatLng>>()
    val route: LiveData<List<LatLng>> get() = _route

    private val _direction = MutableLiveData<DirectionsResponseV3>()
    val direction: LiveData<DirectionsResponseV3> get() = _direction

    fun fetchRoute(startLatLng: LatLng, endLatLng: LatLng) {
        DirectionsRepository.getRoute(startLatLng, endLatLng) { route ->
            _route.value = route!!
        }
    }

    fun fetchRouteV2(startLatLng: LatLng, endLatLng: LatLng) {
        val origin = LocationModel(
            LatLngParentModel(
                LatLngModel(
                    latitude = startLatLng.latitude,
                    longitude = startLatLng.longitude
                )
            )
        )
        val destination = LocationModel(
            LatLngParentModel(
                LatLngModel(
                    latitude = endLatLng.latitude,
                    longitude = endLatLng.longitude
                )
            )
        )
        val requestBody = DirectionRequestV2(origin, destination)
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.directionsDataSourceV2.getDirectionV2(requestBody)
                }
                //演示用只取第一条路径规划；现实不妨交由用户自己根据(时间、路程)来选路径方案
                _route.value = response.routes[0].polyline.polyLine
            } catch (e: HttpException) {
                Log.e("viewModelScope", "fetchRouteV2: ", e)
            } catch (e: Exception) {
                Log.e("viewModelScope", "fetchRouteV2: ", e)
            }
        }
    }

    fun fetchDirection(startLatLng: LatLng, endLatLng: LatLng) {
        val origin = "${startLatLng.latitude},${startLatLng.longitude}"
        val destination = "${endLatLng.latitude},${endLatLng.longitude}"
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.directionsDataSourceV3.getDirectionsV3(origin, destination)
                }
                _direction.value = response
                _route.value = response.routes[0].overview_polyline.polyLine
            } catch (e: HttpException) {
                Log.e("viewModelScope", "fetchRouteV2: ", e)
            } catch (e: Exception) {
                Log.e("viewModelScope", "fetchRouteV2: ", e)
            }
        }
    }

    // 检查用户是否偏离了计划路线，触发重新计算路径的操作
    private fun scheduleCheckInPath() {
        if (isNavigating.value == true && currentLocation.value != null && route.value != null) {
            Log.d("scheduleCheckInPath", "scheduleCheckInPath: ${isOffRoute(currentLocation.value!!, route.value!!)}")
            if (isOffRoute(currentLocation.value!!, route.value!!)) {
                destinationMarker.value?.let {
                    fetchRouteV2(
                        currentLocation.value!!,
                        it.position
                    )
                }
            }
        }
    }

    // 检查用户是否偏离计划路线 距离阈值默认30米
    private fun isOffRoute(
        currentLatLng: LatLng,
        plannedRoute: List<LatLng>,
        thresholdDistance: Double = 30.0
    ): Boolean {
        // 使用PolyUtil.isLocationOnPath()方法检查用户位置是否在计划路线上
        return !PolyUtil.isLocationOnPath(currentLatLng, plannedRoute, true, thresholdDistance)
    }

    fun updateLocation(location: LatLng) {
        _currentLocation.value = location
    }

    fun updateDestinationMarker(destination: Marker?) {
        _destinationMarker.value = destination
    }

    fun updateCurrentPolyLine(polyLine: Polyline) {
        _currentPolyline.value = polyLine
    }

    fun clearCurrentPolyline() {
        _currentPolyline.value?.remove()
    }

    fun clearDestination() {
        _destinationMarker.value?.remove()
    }

    fun updateIsNavigating(isNav: Boolean) {
        _isNavigating.value = isNav
    }

    fun updateNavStartTime(startTime: Long) {
        _navStartTime.value = startTime
    }

    fun updateNavEndTime(endTime: Long) {
        _navEndTime.value = endTime
    }

    fun addPointToTrip(point: LatLng) {
        if (_tripPath.value == null) {
            _tripPath.value = mutableListOf(point)
        } else {
            _tripPath.value?.add(point)
        }
    }

    fun clearTripPath() {
        _tripPath.value?.clear()
    }

    fun updateElapsedTime(time: Long) {
        _elapsedTime.value = time
    }

    fun updateTotalDistance(distance: Float) {
        _totalDistance.value = distance
    }

    private var timer: Timer? = null

    fun startTimerForCheckingPath() {
        if (timer == null) {
            timer = Timer().apply {
                schedule(timerTask {
                    // 定时任务的逻辑
                    viewModelScope.launch {
                        scheduleCheckInPath()
                    }
                }, 5000, 10000) // 50000 毫秒后开始执行，每隔 10000 毫秒执行一次
            }
        }
    }

    fun stopTimerFoCheckingPath() {
        timer?.cancel()
        timer = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTimerFoCheckingPath()
    }

}
