package com.viclin.ridernavigator.view

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.SphericalUtil
import com.viclin.ridernavigator.R

/**
 * 行程总结：行驶过的路径、路程长度、花费时间
 */
class TripSummaryActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var pathPoints: List<LatLng>
    private var startTime: Long = 0
    private var endTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_summary)

        pathPoints = intent.getParcelableArrayListExtra<LatLng>("pathPoints")!!
        startTime = intent.getLongExtra("startTime", 0)
        endTime = intent.getLongExtra("endTime", 0)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.summary_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        displayTripSummary()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title = "Summary of the trip"

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        drawPathOnMap()
    }

    private fun drawPathOnMap() {
        val polylineOptions = PolylineOptions().apply {
            addAll(pathPoints)
            width(10f)
            color(Color.BLUE)
        }
        mMap.addPolyline(polylineOptions)

        val bounds = LatLngBounds.builder().apply {
            for (point in pathPoints) {
                include(point)
            }
        }.build()
        mMap.addMarker(MarkerOptions().position(pathPoints[0]).title("Origin"))
        mMap.addMarker(MarkerOptions().position(pathPoints[pathPoints.size -1]).title("Destination"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    private fun displayTripSummary() {
        val tripDuration = (endTime - startTime) / 1000

        // 计算小时、分钟和秒
        val hours = (tripDuration / (60 * 60)).toInt()
        val minutes = ( (tripDuration / 60) % 60 ).toInt()
        val seconds = (tripDuration % 60).toInt()

        // 构造时间间隔字符串
        val timeString = buildString {
            if (hours > 0) {
                append("${hours}h")
            }
            if (minutes > 0 || hours > 0) {
                append("${minutes}m")
            }
            append("${seconds}s")
        }

        val distance = calculateTotalDistance()

        findViewById<TextView>(R.id.trip_duration).text = "Elapsed time: ${timeString}"
        findViewById<TextView>(R.id.trip_distance).text = "Total distance: ${distance}meters"
    }

    private fun calculateTotalDistance(): Double {
        if (pathPoints.size < 2) {
            return 0.0
        }
        return pathPoints.zipWithNext { a, b -> SphericalUtil.computeDistanceBetween(a, b) }
            .sum()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
