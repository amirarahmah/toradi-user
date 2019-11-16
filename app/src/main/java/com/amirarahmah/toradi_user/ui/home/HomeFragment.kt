package com.amirarahmah.toradi_user.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.amirarahmah.toradi_user.R
import com.amirarahmah.toradi_user.util.PermissionUtils
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng

class HomeFragment : Fragment(), OnMapReadyCallback {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    /*Maps variable*/
    private var mMap: GoogleMap? = null
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0

    private var lat_awal: Double? = 0.0
    private var lng_awal: Double? = 0.0
    private var alamat_awal = ""
    private var keterangan_awal = ""

    private var lat_tujuan: Double? = 0.0
    private var lng_tujuan: Double? = 0.0
    private var alamat_tujuan = ""
    private var keterangan_tujuan = ""

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mLocationCallback: LocationCallback? = null
    private var mLocationRequest: LocationRequest? = null
    /*Maps variable*/

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.location_map) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(activity?.applicationContext as Context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(activity?.applicationContext as Context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            PermissionUtils.requestPermission(activity as AppCompatActivity, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true)
        }

        mMap?.isMyLocationEnabled = true

        try {
            MapsInitializer.initialize(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        getDeviceLocation()

    }

    private fun getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices
            .getFusedLocationProviderClient(activity?.applicationContext as Context)

        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 10000
        mLocationRequest!!.fastestInterval = 5000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    return
                }
                val location = locationResult.lastLocation
                val cameraUpdate = CameraUpdateFactory
                    .newLatLngZoom(LatLng(location.latitude, location.longitude), 17f)
                mMap?.moveCamera(cameraUpdate)
                mFusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback!!)
            }
        }

        mFusedLocationProviderClient!!.lastLocation
            .addOnSuccessListener(activity!!) { location ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                } else {
                    mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback!!, null)
                }

                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(latitude!!, longitude!!), 17f)
                mMap?.moveCamera(cameraUpdate)
            }
    }


}