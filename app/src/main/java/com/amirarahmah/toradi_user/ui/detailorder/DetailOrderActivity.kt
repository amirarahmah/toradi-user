package com.amirarahmah.toradi_user.ui.detailorder

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.amirarahmah.toradi_user.R
import com.amirarahmah.toradi_user.data.model.Status
import com.amirarahmah.toradi_user.util.Const
import com.amirarahmah.toradi_user.util.PreferenceHelper
import com.amirarahmah.toradi_user.util.PreferenceHelper.get
import com.amirarahmah.toradi_user.util.showSnackbarInfo
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

    private var token: String? = ""

    //for broadcastreceiver
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var mIntentFilter: IntentFilter

    private var orderId: Int? = 0

    /* MAPS Variable */
    private var mMap: GoogleMap? = null
    private var pickup_lat: Double? = 0.0
    private var pickup_lng: Double? = 0.0

    private var destination_lat: Double? = 0.0
    private var destination_lng: Double? = 0.0

    private var polyline: Polyline? = null
    /* MAPS Variable */

    private var pickup_address: String? = ""
    private var destination_address: String? = ""
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
    private var status_text = ""
    private var hasReviewed: Boolean = true

    private lateinit var viewModel: OrderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_order)

        supportActionBar?.title = "Detail Pesanan"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val extras = intent.extras
        orderId = extras!!.getInt("order_id", 0)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.location_map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        val prefs = PreferenceHelper.defaultPrefs(this)
        token = prefs["token", ""]

        setupBroadcastReceiver()

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

        setupViewModel()
        getDetailOrder()
    }


    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(this).get(OrderViewModel::class.java)

        viewModel.isLoading.observe(this, Observer {
            if (it == true) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
            }
        })

        viewModel.statusUpdated.observe(this, Observer {
            viewModel.getDetailOrder(token!!, orderId!!)
        })

        viewModel.order.observe(this, Observer {
            when (it?.status) {
                Status.SUCCESS -> {
                    val order = it.data

                    if (order != null) {
                        distance = order.distance
                        price = order.price
                        pickup_lat = order.pickup_lat
                        pickup_lng = order.pickup_lng
                        destination_lat = order.destination_lat
                        destination_lng = order.destination_lng
                        pickup_address = order.pickup_address
                        destination_address = order.destination_address
                        driver_id = order.driver_id
                        driver_name = order.driver_name
                        license_plat = order.license_plat
                        status = order.status
                        status_text = order.status_text
                        note = order.note

                        setLayout()
                        setClick()
                        addPolylinesToMaps(pickup_lat!!, pickup_lng!!, destination_lat!!, destination_lng!!)
                    }
                }
                Status.ERROR -> {
                    this.showSnackbarInfo("" + it.message)
                }
            }
        })
    }


    private fun getDetailOrder() {
        orderId?.let {
            viewModel.getDetailOrder(token!!, orderId!!)
        }
    }


    private fun setupBroadcastReceiver() {
        //to fetch order detail when order status changed
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                viewModel.getDetailOrder(token!!, orderId!!)
            }
        }

        mIntentFilter = IntentFilter(Const.STATUS_ORDER_UPDATED)
    }


    private fun setClick() {
        btn_review.setOnClickListener {
            val reviewFragment = ReviewFragment.newInstance(1)
            reviewFragment.show(supportFragmentManager, reviewFragment.tag)
        }

        btn_cancel.setOnClickListener {
            //konfirmasi apakah user benar-benar akan membatalkan pemesanan
            val ad = AlertDialog.Builder(this)
            ad.create()
            ad.setTitle("Batalkan Order")
            ad.setMessage("Apakah Anda yakin akan membatalkan pemesanan?")
            ad.setPositiveButton("Ya") { dialog, which ->
                viewModel.cancelOrder(token!!, orderId!!)
            }
            ad.setNegativeButton("Tidak") { dialog, which ->

            }

            ad.show()
        }
    }


    private fun setLayout(){
        if (driver_id == null || driver_id == 0) {
            container_driver.visibility = View.GONE
        }

        /* Driver data */
        tv_fullname.text = driver_name
        tv_license_plat.text = license_plat
        Glide.with(this)
            .load(R.drawable.no_profile_image)
            .into(image_driver)
        /* Driver data */

        /* Order data */
        tv_pickup.text = pickup_address
        tv_destination.text = destination_address

        val formatter = DecimalFormat("#,###,###")
        val priceText = "Rp ${formatter.format(price)}"
        tv_price.text = priceText

        tv_distance.text = "$distance km"
        tv_status.text = status_text

        when (status) {
            2 -> {
                btn_cancel.visibility = View.VISIBLE
                btn_cancel.isEnabled = true
                btn_review.visibility = View.GONE
                container.visibility = View.VISIBLE
            }
            3 -> {
                btn_cancel.isEnabled = false
                btn_review.visibility = View.GONE
                container.visibility = View.VISIBLE
            }
            4, 5, 6 -> {
                btn_cancel.visibility = View.GONE
                btn_review.visibility = View.VISIBLE
                container.visibility = View.GONE
            }
        }
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

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, mIntentFilter)
    }

    override fun onPause() {
        try {
            if (broadcastReceiver != null) {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
            }
        } catch (e: Exception) {

        }
        super.onPause()

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
