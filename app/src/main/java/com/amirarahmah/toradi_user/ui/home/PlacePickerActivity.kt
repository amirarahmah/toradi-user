package com.amirarahmah.toradi_user.ui.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import com.amirarahmah.toradi_user.R
import com.amirarahmah.toradi_user.util.PermissionUtils
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_place_picker.*
import kotlinx.android.synthetic.main.bottom_sheet_pick_location.*
import java.util.*

class PlacePickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private var address = ""
    private var latitude : Double? = 0.0
    private var longitude : Double? = 0.0

    private var mMap: GoogleMap? = null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mLocationCallback: LocationCallback? = null
    private var mLocationRequest: LocationRequest? = null
    private var mLastLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_picker)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.location_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val type = intent.getIntExtra("type", 1)

        if(type == 1){
            tv_title.text = resources.getString(R.string.pilih_lokasi_tujuan)
        }else{
            tv_title.text = resources.getString(R.string.pilih_lokasi_penjemputan)
        }

        btn_back.setOnClickListener {
            finish()
        }

        btn_confirm.setOnClickListener {
            val resultData = Intent()
            resultData.putExtra("latitude", latitude)
            resultData.putExtra("longitude", longitude)
            resultData.putExtra("address", address)
            resultData.putExtra("type", type)
            setResult(Activity.RESULT_OK, resultData)
            onBackPressed()
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            PermissionUtils.requestPermission(this, 1,
                Manifest.permission.ACCESS_FINE_LOCATION, true)
        }

        mMap?.isMyLocationEnabled = true

        try {
            MapsInitializer.initialize(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        getDeviceLocation()

        mMap?.setOnCameraIdleListener {
            val newLatLng = mMap?.cameraPosition!!.target

            latitude = newLatLng.latitude
            longitude = newLatLng.longitude
            getAddress(newLatLng)
        }

        mMap?.setOnCameraMoveListener {
            tv_address.text = "Loading Address..."
        }
    }


    private fun getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices
            .getFusedLocationProviderClient(this)

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
            .addOnSuccessListener(this) { location ->
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


    private fun getAddress(latLng: LatLng) {
        try{
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if(addresses.isEmpty()){
                tv_address.text = "Loading Address..."
            }else{
                address = "" + addresses[0].getAddressLine(0)
                tv_address.text = address
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
    }
}
