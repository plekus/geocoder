package ru.geocoder.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import org.koin.android.ext.android.get
import ru.geocoder.R
import ru.geocoder.databinding.ActivityMainBinding
import ru.geocoder.model.LocationInfo
import java.util.*


class MainActivity : MvpAppCompatActivity(), MainView, OnMapReadyCallback {

    companion object {
        private const val LOCATION_INTERVAL = 60L*1000 //60 sec
        private const val FASTEST_INTERVAL = 10L*1000 //10 sec
    }

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding!!

    @InjectPresenter
    lateinit var presenter: MainPresenter

    @ProvidePresenter
    fun provide() : MainPresenter = get()

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var map: GoogleMap? = null
    private var locationMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.map.apply {
            onCreate(null)
            onResume()
            getMapAsync(this@MainActivity)
        }

        binding.myLocation.setOnClickListener {
            locationMarker?.position?.let {
                map?.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(it).zoom(17F).build()))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkPermissions()
    }

    override fun showLocation(locationInfo: LocationInfo, isFirst: Boolean) {
        binding.address.text = locationInfo.address
        binding.progress.visibility = View.INVISIBLE
        binding.myLocation.visibility = View.VISIBLE

        val location = LatLng(locationInfo.lat, locationInfo.lon)
        if (isFirst) {
            map?.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(location).zoom(17F).build()))
        }

        locationMarker?.let {
            it.position = location
            return
        }

        locationMarker = map?.addMarker(
            MarkerOptions()
            .position(location)
            .anchor(0.5F,0.5F)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location)))
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        MapsInitializer.initialize(this)
        map = googleMap
        map?.mapType = GoogleMap.MAP_TYPE_NORMAL
        map?.uiSettings?.isZoomControlsEnabled = false
        map?.uiSettings?.isCompassEnabled = false
        map?.uiSettings?.isTiltGesturesEnabled = false
        map?.uiSettings?.isRotateGesturesEnabled = false
    }

    private fun checkPermissions() {
        val permissions = arrayOf<String>(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        val options = Permissions.Options()
                .setRationaleDialogTitle(getString(R.string.info))
                .setSettingsDialogTitle(getString(R.string.warning))
        Permissions.check(this, permissions, getString(R.string.location_permission), options, object : PermissionHandler() {
            override fun onGranted() {
                requestNewLocationData()
            }

            override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
                Toast.makeText(this@MainActivity, R.string.permission_error, Toast.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        if (!isLocationEnabled()) {
            Toast.makeText(this@MainActivity, R.string.gps_disabled, Toast.LENGTH_SHORT).show()
            return
        }
        val locationRequest = LocationRequest.create()
            .apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = LOCATION_INTERVAL
                fastestInterval = FASTEST_INTERVAL
            }

        fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
        )
    }

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location = locationResult.lastLocation
            binding.progress.visibility = View.VISIBLE
            presenter.getAddress(lastLocation.latitude, lastLocation.longitude)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onStop() {
        super.onStop()
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient = null
        _binding = null
    }
}