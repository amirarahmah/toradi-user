package com.amirarahmah.toradi_user.ui.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.amirarahmah.toradi_user.R
import com.amirarahmah.toradi_user.data.model.Status
import com.amirarahmah.toradi_user.util.PermissionUtils
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.bottom_sheet_main.*
import kotlinx.android.synthetic.main.bottom_sheet_order.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.json.JSONObject
import java.util.*


class HomeFragment : Fragment(), OnMapReadyCallback {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    /*Maps variable*/
    private var mMap: GoogleMap? = null
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0

    private var pickup_lat: Double? = 0.0
    private var pickup_lng: Double? = 0.0
    private var pickup_address = ""
    private var keterangan_awal = ""

    private var destination_lat: Double? = 0.0
    private var destination_lng: Double? = 0.0
    private var destination_address = ""
    private var keterangan_tujuan = ""

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mLocationCallback: LocationCallback? = null
    private var mLocationRequest: LocationRequest? = null

    private var polyline: Polyline? = null
    private var marker_pickup: Marker? = null
    private var marker_destination: Marker? = null
    /*Maps variable*/

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.location_map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        container_search_destination.setOnClickListener {
            if (latitude != 0.0 && longitude != 0.0) {
                val searchLocationFragment = SearchLocationFragment
                    .newInstance(latitude!!, longitude!!, pickup_address, destination_address)
                searchLocationFragment.setTargetFragment(this, 2)
                searchLocationFragment.show(
                    activity!!.supportFragmentManager,
                    searchLocationFragment.tag
                )
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(
                activity?.applicationContext as Context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                activity?.applicationContext as Context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            PermissionUtils.requestPermission(
                activity as AppCompatActivity, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
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
                latitude = location.latitude
                longitude = location.longitude
                moveCamera(latitude!!, longitude!!)
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
                    mFusedLocationProviderClient!!.requestLocationUpdates(
                        mLocationRequest,
                        mLocationCallback!!,
                        null
                    )
                }

                moveCamera(latitude!!, longitude!!)
            }
    }


    private fun moveCamera(latitude: Double, longitude: Double) {
        val cameraUpdate = CameraUpdateFactory
            .newLatLngZoom(LatLng(latitude, longitude), 17f)
        mMap?.moveCamera(cameraUpdate)
        getAddress(latitude, longitude)
    }


    private fun getAddress(latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses.isEmpty()) {
                pickup_address = ""
            } else {
                pickup_address = "" + addresses[0].getAddressLine(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            //get address, latitude and longitude from SearchLocationFragment
            val bundle = data!!.extras
            val adddress_type = bundle!!.getInt("address_type", 1)
            val from = bundle.getInt("from", 1)
            val address = bundle.getString("address", "")

            Log.d("OjekActivity", "address: $adddress_type")

            if (adddress_type == 1) {
                destination_address = address
            } else {
                pickup_address = address
            }

            if (pickup_lat == 0.0 || pickup_lng == 0.0) {
                pickup_lat = latitude
                pickup_lng = longitude
            }

            if (from == 1) { // address from place autocomplete
                getLatLngFromAddress(address, adddress_type)
            } else { // address from maps picker
                destination_lat = bundle.getDouble("latitude", 0.0)
                destination_lng = bundle.getDouble("longitude", 0.0)

                addPolylinesToMaps(pickup_lat!!, pickup_lng!!, destination_lat!!, destination_lng!!)
                showOrderSummary()
            }

        }
    }


    private fun getLatLngFromAddress(address: String, type: Int) {
        viewModel.getLatLng(address)

        viewModel.geocodeResult.observe(this, androidx.lifecycle.Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    if (it.data != null) {
                        if (type == 1) { // destination address
                            destination_lat = it.data.location.lat
                            destination_lng = it.data.location.lng

                            addPolylinesToMaps(pickup_lat!!, pickup_lng!!, destination_lat!!, destination_lng!!)
                            showOrderSummary()

                        } else { // pickup address
                            pickup_lat = it.data.location.lat
                            pickup_lng = it.data.location.lng

                            if(destination_lat != 0.0 && destination_lng != 0.0){
                                addPolylinesToMaps(pickup_lat!!, pickup_lng!!, destination_lat!!, destination_lng!!)
                                showOrderSummary()
                            }
                        }
                    }
                }
                Status.ERROR -> {

                }
            }
        })
    }


    private fun showOrderSummary() {
        bottom_sheet_main.visibility = View.GONE
        val bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet_order)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.skipCollapsed = true
        bottomSheetBehavior.peekHeight = bottom_sheet_order.height
        tv_pickup.text = pickup_address
        tv_destination.text = destination_address
    }

    fun addPolylinesToMaps(
        pickupLat: Double,
        pickupLng: Double,
        destinationLat: Double,
        destinationLng: Double
    ) {
        if (polyline != null) {
            polyline?.remove()
        }

        val path: MutableList<List<LatLng>> = ArrayList()

        val origin = "$pickupLat,$pickupLng"
        val destination = "$destinationLat,$destinationLng"

        Log.d("OjekActivity", "origin: $origin destination: $destination")

        val apiKey = resources.getString(R.string.google_maps_key2)

        val urlDirections =
            "https://maps.googleapis.com/maps/api/directions/json?origin=$origin&destination=$destination&key=$apiKey"
        val directionsRequest = object :
            StringRequest(Method.GET, urlDirections, Response.Listener<String> { response ->
                val jsonResponse = JSONObject(response)
                // Get routes
                val routes = jsonResponse.getJSONArray("routes")
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                val steps = legs.getJSONObject(0).getJSONArray("steps")
                val distance = legs.getJSONObject(0).getJSONObject("distance").getInt("value")

                val distanceKm: Double = distance.toDouble()/1000
                tv_distance.text = "Jarak: $distanceKm km"

                for (i in 0 until steps.length()) {
                    val points =
                        steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                    path.add(PolyUtil.decode(points))
                }

                val lineOptions = PolylineOptions()

                for (i in 0 until path.size) {

                    lineOptions.addAll(path[i])
                    lineOptions.width(12f)
                    lineOptions.color(ContextCompat.getColor(context!!, R.color.colorPrimary))

                }

                polyline = mMap?.addPolyline(lineOptions)

                addMarkerToMaps()

                //animate camera
                val builder = LatLngBounds.Builder()
                builder.include(LatLng(pickupLat, pickupLng))
                builder.include(LatLng(destinationLat, destinationLng))
                val bounds = builder.build()
                mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))

            }, Response.ErrorListener {
            }) {}
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(directionsRequest)

    }

    private fun addMarkerToMaps() {
        //add pickup marker
        val loc = LatLng(pickup_lat!!, pickup_lng!!)
        if (marker_pickup != null) {
            marker_pickup?.remove()
        }

        var height = 75
        var width = 75
        val b = BitmapFactory.decodeResource(resources, R.drawable.ic_pickup)
        val marker = Bitmap.createScaledBitmap(b, width, height, false)
        val markerIcon = BitmapDescriptorFactory.fromBitmap(marker)

        marker_pickup = mMap?.addMarker(
            MarkerOptions()
                .position(loc)
                .icon(markerIcon)
        )

        //add destination marker
        val loc2 = LatLng(destination_lat!!, destination_lng!!)
        if (marker_destination != null) {
            marker_destination?.remove()
        }

        height = 100
        width = 100
        val b2 = BitmapFactory.decodeResource(resources, R.drawable.ic_destination)
        val marker2 = Bitmap.createScaledBitmap(b2, width, height, false)
        val markerIcon2 = BitmapDescriptorFactory.fromBitmap(marker2)

        marker_destination = mMap?.addMarker(
            MarkerOptions()
                .position(loc2)
                .icon(markerIcon2)
        )
    }


}