package com.viclin.ridernavigator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class MainViewModel : ViewModel() {

    private val _startLocation = MutableLiveData<LatLng>()
    val startLocation: LiveData<LatLng> get() = _startLocation

    private val _endLocation = MutableLiveData<LatLng>()
    val endLocation: LiveData<LatLng> get() = _endLocation

    private val _elapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long> get() = _elapsedTime

    private val _totalDistance = MutableLiveData<Float>()
    val totalDistance: LiveData<Float> get() = _totalDistance

    fun setStartLocation(location: LatLng) {
        _startLocation.value = location
    }

    fun setEndLocation(location: LatLng) {
        _endLocation.value = location
    }

    fun setElapsedTime(time: Long) {
        _elapsedTime.value = time
    }

    fun setTotalDistance(distance: Float) {
        _totalDistance.value = distance
    }

    fun calculateDistance(start: LatLng, end: LatLng): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            start.latitude, start.longitude,
            end.latitude, end.longitude,
            results
        )
        return results[0]
    }
}