package com.amirarahmah.toradi_user.ui.detailorder

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amirarahmah.toradi_user.R
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.activity_detail_order.*
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.ArrayList

class DetailOrderActivity : AppCompatActivity(), OnMapReadyCallback {

    //for broadcastreceiver
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var mIntentFilter: IntentFilter

    private var mMap: GoogleMap? = null
    private var pickup_lat: Double? = 0.0
    private var pickup_lng: Double? = 0.0
    private var pickup_address: String? = ""

    private var destination_lat: Double? = 0.0
    private var destination_lng: Double? = 0.0
    private var destination_address: String? = ""

    private var polyline: Polyline? = null

    private var price = 0
    private var distance = 0.0
    private var note: String? = ""

    //driver data
    private var driver_id: Int? = 0
    private var driver_name: String? = ""
    private var driver_phone: String? = ""
    private var driver_image: String? = ""
    private var license_plat: String? = ""

    private var status: Int = 0
    private var hasReviewed: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_order)

        supportActionBar?.title = "Detail Pesanan"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.location_map) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        mMap?.uiSettings?.isMyLocationButtonEnabled = false

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        mMap?.isMyLocationEnabled = true

        getDetailOrder()
    }


    private fun getDetailOrder() {
        setLayout()
        setClick()
    }


    private fun setClick() {
        btn_review.setOnClickListener {
            val reviewFragment = ReviewFragment.newInstance(1)
            reviewFragment.show(supportFragmentManager, reviewFragment.tag)
        }
    }


    private fun setLayout(){
        if (driver_id == null || driver_id == 0) {
            container_driver.visibility = View.GONE
        }

        /* Driver data */
        tv_fullname.text = "Nama Driver"
        tv_license_plat.text = "W 4564 QK"
        Glide.with(this)
            .load(R.drawable.no_profile_image)
            .into(image_driver)
        /* Driver data */

        /* Order data */
        tv_pickup.text = "Jl Terusan Cikampek 11"
        tv_destination.text = "Jl Mayjen Panjaitan 14"

        val formatter = DecimalFormat("#,###,###")
        val priceText = "Rp ${formatter.format(price)}"
        tv_price.text = priceText

        tv_distance.text = "$distance km"
        tv_status.text = "Driver sedang menuju Anda"

        btn_review.visibility = View.VISIBLE
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

                for (i in 0 until steps.length()) {
                    val points =
                        steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                    path.add(PolyUtil.decode(points))
                }

                val lineOptions = PolylineOptions()

                for (i in 0 until path.size) {

                    lineOptions.addAll(path[i])
                    lineOptions.width(12f)
                    lineOptions.color(ContextCompat.getColor(this, R.color.colorAccent))

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
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(directionsRequest)

    }


    private fun addMarkerToMaps() {
        //add pickup marker
        val loc = LatLng(pickup_lat!!, pickup_lng!!)

        var height = 75
        var width = 75
        val b = BitmapFactory.decodeResource(resources, R.drawable.ic_pickup)
        val marker = Bitmap.createScaledBitmap(b, width, height, false)
        val markerIcon = BitmapDescriptorFactory.fromBitmap(marker)

        mMap?.addMarker(
            MarkerOptions()
                .position(loc)
                .icon(markerIcon)
        )

        //add destination marker
        val loc2 = LatLng(destination_lat!!, destination_lng!!)

        height = 100
        width = 100
        val b2 = BitmapFactory.decodeResource(resources, R.drawable.ic_destination)
        val marker2 = Bitmap.createScaledBitmap(b2, width, height, false)
        val markerIcon2 = BitmapDescriptorFactory.fromBitmap(marker2)

        mMap?.addMarker(
            MarkerOptions()
                .position(loc2)
                .icon(markerIcon2)
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return false
    }
}
