package com.viclin.ridernavigator.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.text.Html
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.Builder.IMPLICIT_MIN_UPDATE_INTERVAL
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.viclin.ridernavigator.R
import com.viclin.ridernavigator.util.NotificationHelper
import com.viclin.ridernavigator.viewmodel.MapViewModel
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.util.Locale

class MainActivity : AppCompatActivity(), OnMapReadyCallback, TextToSpeech.OnInitListener {

    private lateinit var mMap: GoogleMap
    private val mapViewModel: MapViewModel by viewModels()
    private lateinit var startButton: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var textToSpeech: TextToSpeech
    private val notificationHelper: NotificationHelper by lazy {
        NotificationHelper.create(this)
    }

    private var isNavigating: Boolean = false
    private var lastSpokenStepIndex: Int = -1

    private var directionMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        startButton = findViewById(R.id.startButton)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        textToSpeech = TextToSpeech(this, this)

        startButton.setOnClickListener {
            if (isNavigating) {
                stopNavigation()
            } else {
                if (hasLocationPermission()) {
                    if (mapViewModel.destinationMarker.value == null) {
                        showSnackbar(getString(R.string.before_navigation))
                    } else {
                        startNavigation()
                    }
                } else {
                    showSnackbar(getString(R.string.location), true)
                    requestLocationPermission()
                }
            }
        }

        mapViewModel.isPathPlanned.observe(this) { isPlanned ->
            startButton.isEnabled = isPlanned
        }

        mapViewModel.isNavigating.observe(this) { isNav ->
            isNavigating = isNav
            if (isNav) {
                startButton.text = getString(R.string.stop)
            } else {
                startButton.text = getString(R.string.start)
            }
        }

        mapViewModel.direction.observe(this) { direction ->
            lastSpokenStepIndex = -1
//            direction?.let { provideVoiceGuidance(it) }
        }

        //返回时如果正在导航先取消导航
//        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                if (isNavigating) {
//                    stopNavigation()
//                } else {
//                    this.isEnabled = false
//                    onBackPressedDispatcher.onBackPressed()
//                    this.isEnabled = true
//                }
//            }
//        })

    }

    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this, getString(R.string.location),
            LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

        if (hasLocationPermission()) {
            getLastKnownLocation()
        } else {
            showSnackbar("Location permission is required to use this app.", true)
        }

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        getLastKnownLocation()
        enableMyLocation()

        mMap.setOnMapClickListener {
            if (hasLocationPermission() && !isNavigating) {
                mapViewModel.clearDestination()
                val destinationMarker =
                    mMap.addMarker(MarkerOptions().position(it).title("Destination"))
                mapViewModel.updateDestinationMarker(destinationMarker)
            }
        }

        mapViewModel.currentLocation.observe(this) { location ->
            location?.let {

            }
        }

        if (mapViewModel.destinationMarker.value != null) {
            mapViewModel.destinationMarker.value?.let {
                val destinationMarker =
                    mMap.addMarker(MarkerOptions().position(it.position).title("Destination"))
                mapViewModel.updateDestinationMarker(destinationMarker)
            }
        }

        mapViewModel.route.observe(this) { path ->
            if (isNavigating) {
                mapViewModel.clearCurrentPolyline()
                path?.let {
                    //绘制导航路径
                    val polylineOptions = PolylineOptions()
                        .addAll(it)
                        .width(30f)
                        .color(Color.BLUE)
                    val currentPolyline = mMap.addPolyline(polylineOptions)
                    mapViewModel.updateCurrentPolyLine(currentPolyline)

                    // 调整地图视野
                    val bounds = LatLngBounds.builder().apply {
                        for (point in it) {
                            include(point)
                        }
                    }.build()

                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))

                    //实时更新当前定位
                    startLocationUpdates()
                    //定时检查是否偏离已规划路径
                    mapViewModel.startTimerForCheckingPath()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        if (hasLocationPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    mapViewModel.updateLocation(currentLatLng)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
                }
            }
        }
    }

    private fun startNavigation() {
        mapViewModel.updateIsNavigating(true)
        mapViewModel.updateNavStartTime(System.currentTimeMillis())
        //开始导航-清空历史里程
        mapViewModel.clearTripPath()
        getLastKnownLocation() // Get the current location when starting navigation

        mapViewModel.currentLocation.value?.let {
//            mapViewModel.fetchRoute(it,mapViewModel.destinationMarker.value!!.position)
//            mapViewModel.fetchRouteV2(it, mapViewModel.destinationMarker.value!!.position)
            mapViewModel.fetchDirection(it, mapViewModel.destinationMarker.value!!.position)
        }

    }

    private fun stopNavigation() {
        mapViewModel.updateIsNavigating(false)
        mapViewModel.updateNavEndTime(System.currentTimeMillis())
        mapViewModel.clearCurrentPolyline()
        // Stop location updates and show trip summary
        fusedLocationClient.removeLocationUpdates(locationCallback)
        //Stop checking inPath
        mapViewModel.stopTimerFoCheckingPath()
        directionMarker?.remove() // 取消箭头展示
    }

    private fun goToTripSummary() {
        // 跳转到行程摘要Activity
        val intent = Intent(this, TripSummaryActivity::class.java).apply {
            putParcelableArrayListExtra("pathPoints",
                mapViewModel.tripPath.value?.let { ArrayList(it) })
            putExtra("startTime", mapViewModel.navStartTime.value)
            putExtra("endTime", mapViewModel.navEndTime.value)
        }
        startActivity(intent)
    }

    // 当距离小于10米时认为已到达目的地
    private fun checkArrival(currentLocation: Location, destination: LatLng): Boolean {
        val destinationLocation = Location("").apply {
            latitude = destination.latitude
            longitude = destination.longitude
        }

        val distance = currentLocation.distanceTo(destinationLocation)
        return distance < 10
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .apply {
                setWaitForAccurateLocation(false)
                setMinUpdateIntervalMillis(IMPLICIT_MIN_UPDATE_INTERVAL)
                setMaxUpdateDelayMillis(100000)
            }.build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation ?: return
            val currentLatLng = LatLng(location.latitude, location.longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))

            // 更新用户当前位置
            mapViewModel.updateLocation(currentLatLng)

            //添加经过点到行程
            mapViewModel.addPointToTrip(currentLatLng)

            //TTS
            provideVoiceGuidance(location)

            mapViewModel.destinationMarker.value?.let {
                if (isNavigating && checkArrival(location, it.position)) {
                    //到达目的地主动结束导航并总结行程
                    stopNavigation()
                    goToTripSummary()
                }
            }
        }

        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            super.onLocationAvailability(locationAvailability)
            Log.d(
                "onLocationAvailability",
                "onLocationAvailability: ${locationAvailability.isLocationAvailable}"
            )
            if (!locationAvailability.isLocationAvailable) {
                gpsNotice("GPS信号丢失", "请检查您的GPS设置")
            } else {
                gpsNotice("GPS信号恢复", "GPS信号已恢复")
            }
        }
    }

    //在 GPS 信号丢失时通知用户
    private fun gpsNotice(title: String, content: String) {
        if (notificationEnable()) {
            notificationHelper.showNotification(this@MainActivity, title, content)
        } else {
            showSnackbar(title)
        }
    }

    private fun showSnackbar(message: String, isAction: Boolean = false) {
        if (isAction) {
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setAction("Settings") {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", packageName, null)
                    }
                    startActivity(intent)
                }
                .show()
        } else {
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
        }
    }

    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(LOCATION_PERMISSION_REQUEST_CODE)
    private fun enableMyLocation() {
        if (hasLocationPermission()) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true  // 启用我的位置按钮
            mMap.uiSettings.isCompassEnabled = true  // 启用指南针
            mMap.isTrafficEnabled = true        //交通信息
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.location),
                LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun notificationEnable(): Boolean {
        return NotificationManagerCompat.from(this).areNotificationsEnabled()
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    @SuppressLint("NewApi")
    private fun provideVoiceGuidance(location: Location) {
        if (mapViewModel.direction.value == null) return
        val route = mapViewModel.direction.value!!.routes.firstOrNull()
        route?.let {
            for (leg in it.legs) {
                for ((stepIndex, step) in leg.steps.withIndex()) {
                    val stepStartLocation = LatLng(step.start_location.lat, step.start_location.lng)
                    val stepEndLocation = LatLng(step.end_location.lat, step.end_location.lng)
                    if (isUserNearStep(location, stepStartLocation, stepEndLocation)) {
                        if (stepIndex > lastSpokenStepIndex) {
                            val instruction =
                                Html.fromHtml(step.html_instructions, Html.FROM_HTML_MODE_COMPACT)
                                    .toString()
                            textToSpeech.speak(instruction, TextToSpeech.QUEUE_ADD, null, null)
                            lastSpokenStepIndex = stepIndex
                            showDirectionArrow(stepStartLocation, stepEndLocation)
                        }
                    }
                }
            }
        }
    }

    private fun isUserNearStep(
        location: Location,
        stepStartLocation: LatLng,
        stepEndLocation: LatLng
    ): Boolean {
        val userLatLng = LatLng(location.latitude, location.longitude)
        val startDistance = FloatArray(1)
        val endDistance = FloatArray(1)
        Location.distanceBetween(
            userLatLng.latitude,
            userLatLng.longitude,
            stepStartLocation.latitude,
            stepStartLocation.longitude,
            startDistance
        )
        Location.distanceBetween(
            userLatLng.latitude,
            userLatLng.longitude,
            stepEndLocation.latitude,
            stepEndLocation.longitude,
            endDistance
        )
        return startDistance[0] < 50 || endDistance[0] < 50
    }

    private fun showDirectionArrow(startLocation: LatLng, endLocation: LatLng) {
        val angle = calculateBearing(startLocation, endLocation)
        val arrowBitmap = rotateBitmap(BitmapFactory.decodeResource(resources, R.drawable.arrow_up), angle)
        val markerOptions = MarkerOptions()
            .position(startLocation)
            .icon(BitmapDescriptorFactory.fromBitmap(arrowBitmap))
            .anchor(0.5f, 0.5f)

        directionMarker?.remove()
        directionMarker = mMap.addMarker(markerOptions)
    }

    private fun calculateBearing(startLocation: LatLng, endLocation: LatLng): Float {
        val startLat = Math.toRadians(startLocation.latitude)
        val startLng = Math.toRadians(startLocation.longitude)
        val endLat = Math.toRadians(endLocation.latitude)
        val endLng = Math.toRadians(endLocation.longitude)
        val dLng = endLng - startLng
        val y = Math.sin(dLng) * Math.cos(endLat)
        val x = Math.cos(startLat) * Math.sin(endLat) - Math.sin(startLat) * Math.cos(endLat) * Math.cos(dLng)
        return Math.toDegrees(Math.atan2(y, x)).toFloat()
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    override fun onDestroy() {
        textToSpeech.stop()
        textToSpeech.shutdown()
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale.TAIWAN
        }
    }

}
