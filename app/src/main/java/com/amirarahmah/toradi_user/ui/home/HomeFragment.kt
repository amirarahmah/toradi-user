package com.amirarahmah.toradi_user.ui.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
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
import com.amirarahmah.toradi_user.util.showSnackbarInfo
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
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
    private val REQUEST_CHECK_SETTINGS = 100

    /*Maps variable*/
    private var mMap: GoogleMap? = null
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0

    private var pickup_lat: Double? = 0.0
    private var pickup_lng: Double? = 0.0
    private var pickup_address = ""

    private var destination_lat: Double? = 0.0
    private var destination_lng: Double? = 0.0
    private var destination_address = ""

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mLocationCallback: LocationCallback? = null
    private lateinit var mLocationRequest: LocationRequest

    private var polyline: Polyline? = null
    private var marker_pickup: Marker? = null
    private var marker_destination: Marker? = null
    /*Maps variable*/

    private var price = 0
    private var distance = 0.0
    private var note : String? = ""
    private var totalPassenger = "1"
    private var inputValid = false

    val listDriverMarker = ArrayList<Marker>()

    private lateinit var viewModel: HomeViewModel

    interface DonePickLocation{
        fun updateData()
    }

    lateinit var mListener : DonePickLocation

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
                showSearchLocationFragment(1)
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

        checkLocationSetting()
        getNearbyDriver()
        setClick()

    }


    private fun checkLocationSetting() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 10000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest)
        builder.setAlwaysShow(true)

        val task = LocationServices.getSettingsClient(context!!)
            .checkLocationSettings(builder.build())

        task.addOnCompleteListener { task ->
            try {
                // All location settings are satisfied. The client can initialize location
                val response = task.getResult(ApiException::class.java)
                getDeviceLocation()

            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            val resolvable = exception as ResolvableApiException
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            startIntentSenderForResult(resolvable.resolution.intentSender,
                                REQUEST_CHECK_SETTINGS, null,0,0, 0,null)
                        } catch (e: IntentSender.SendIntentException) {
                            // Ignore the error.
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }// Location settings are not satisfied. However, we have no way to fix the
                // settings so we won't show the dialog.
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getNearbyDriver()
    }

    private fun setClick() {

        pickup.setOnClickListener {
            showSearchLocationFragment(2)
        }

        destination.setOnClickListener {
           showSearchLocationFragment(1)
        }

        btn_passenger.setOnClickListener {
            showPassengerFragment()
        }

        btn_order.setOnClickListener {
            if(note!!.isBlank()){
                YoYo.with(Techniques.Shake)
                    .duration(600)
                    .repeat(0)
                    .playOn(btn_passenger)
                activity?.showSnackbarInfo("Mohon masukkan data penumpang")
            }else{
                if(inputValid){
                    val i = Intent(context, FindDriverActivity::class.java)
                    i.putExtra("pickup_lat", pickup_lat)
                    i.putExtra("pickup_lng", pickup_lng)
                    i.putExtra("pickup_address", pickup_address)
                    i.putExtra("destination_lat", destination_lat)
                    i.putExtra("destination_lng", destination_lng)
                    i.putExtra("destination_address", destination_address)
                    i.putExtra("note", note)
                    i.putExtra("passenger", 1)
                    i.putExtra("distance", distance)
                    i.putExtra("price", price)
                    startActivity(i)
                }
            }
        }
    }


    private fun getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices
            .getFusedLocationProviderClient(activity?.applicationContext as Context)

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    return
                }
                val location = locationResult.lastLocation
                latitude = location.latitude
                longitude = location.longitude
                moveCamera(latitude!!, longitude!!)
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


    private fun getNearbyDriver(){
        if(listDriverMarker.size > 0){
            for(marker in listDriverMarker){
                marker.remove()
            }

            listDriverMarker.clear()
        }

        viewModel.getNeabyDriver()

        viewModel.nearbyDriver.observe(this, androidx.lifecycle.Observer {
            when(it.status){
                Status.SUCCESS -> {
                    if(it.data != null){
                        val responseList = it.data

                        if(responseList.isNotEmpty()){
                            for(driver in responseList){
                                val loc = LatLng(driver.latitude_now, driver.longitude_now)

                                Log.d("NearbyDriver",
                                    "lat driver : ${driver.latitude_now}, Lng driver : ${driver.longitude_now}")

                                addDriverMarker(loc)

                            }
                        }
                    }
                }
                Status.ERROR -> {
                    activity?.showSnackbarInfo(""+it.message)
                }
            }
        })
    }


    private fun addDriverMarker(loc: LatLng) {
        val icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_driver)
        val marker = mMap?.addMarker(MarkerOptions()
            .position(loc)
            .title("Driver")
            .icon(icon))
        listDriverMarker.add(marker!!)
    }


    private fun showSearchLocationFragment(type: Int) {
        val searchLocationFragment = SearchLocationFragment
            .newInstance(type, latitude!!, longitude!!, pickup_address, destination_address)
        searchLocationFragment.setTargetFragment(this, 2)
        searchLocationFragment.show(
            activity!!.supportFragmentManager,
            searchLocationFragment.tag
        )
    }


    private fun showPassengerFragment() {
        val passengerFragment = PassengerFragment()
        passengerFragment.setTargetFragment(this, 3)
        passengerFragment.show(
            activity!!.supportFragmentManager,
            passengerFragment.tag
        )
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
                if(adddress_type == 1){
                    destination_lat = bundle.getDouble("latitude", 0.0)
                    destination_lng = bundle.getDouble("longitude", 0.0)

                    addPolylinesToMaps(pickup_lat!!, pickup_lng!!, destination_lat!!, destination_lng!!)
                    showOrderSummary()
                }else{
                    pickup_lat = bundle.getDouble("latitude", 0.0)
                    pickup_lng = bundle.getDouble("longitude", 0.0)

                    if(destination_lat != 0.0 && destination_lng != 0.0){
                        addPolylinesToMaps(pickup_lat!!, pickup_lng!!, destination_lat!!, destination_lng!!)
                        showOrderSummary()
                    }

                }

            }
        }else if(requestCode == 3  && resultCode == Activity.RESULT_OK){
            val bundle = data!!.extras
            note = bundle!!.getString("condition")
            totalPassenger = bundle.getString("total").toString()
            tv_passanger.text = "$totalPassenger Penumpang"
            atrs.visibility = View.GONE
        }else if(requestCode == REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK){
            getDeviceLocation()
        }
    }


    private fun getLatLngFromAddress(address: String, type: Int) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(address, 1)
            if (addresses.isNotEmpty()) {
                if (type == 1) { // address type is destination address
                    destination_lat = addresses[0].latitude
                    destination_lng = addresses[0].longitude

                    addPolylinesToMaps(pickup_lat!!, pickup_lng!!, destination_lat!!, destination_lng!!)
                    showOrderSummary()

                } else { // pickup address
                    pickup_lat = addresses[0].latitude
                    pickup_lng = addresses[0].longitude

                    if(destination_lat != 0.0 && destination_lng != 0.0){
                        addPolylinesToMaps(pickup_lat!!, pickup_lng!!, destination_lat!!, destination_lng!!)
                        showOrderSummary()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun showOrderSummary() {
        bottom_sheet_main.visibility = View.GONE
        val bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet_order)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
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

                //get distance in km
                val distanceMeter = legs.getJSONObject(0)
                    .getJSONObject("distance").getInt("value")
                distance = distanceMeter.toDouble()/1000
                tv_distance.text = "Jarak: $distance km"

                getTransportPrice()

                for (i in 0 until steps.length()) {
                    val points =
                        steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                    path.add(PolyUtil.decode(points))
                }

                val lineOptions = PolylineOptions()

                for (i in 0 until path.size) {

                    lineOptions.addAll(path[i])
                    lineOptions.width(12f)
                    lineOptions.color(ContextCompat.getColor(context!!, R.color.colorAccent))

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


    private fun getTransportPrice() {
        viewModel.getTransportPrice(distance)

        viewModel.price.observe(this, androidx.lifecycle.Observer {
            when(it.status){
                Status.SUCCESS -> {
                    if(it.data != null){
                        inputValid = true
                        price = it.data.transport_price
                        val textPrice = "Harga: Rp $price"
                        tv_price.text = textPrice
                        mListener.updateData()
                    }
                }
                Status.ERROR -> {
                    activity?.showSnackbarInfo(""+it.message)
                }
            }
        })
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


    override fun onDestroy() {
        super.onDestroy()
        if(mFusedLocationProviderClient != null){
            mFusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback!!)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            this.mListener = activity as DonePickLocation
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnCompleteListener")
        }

    }

}