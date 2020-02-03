package com.amirarahmah.toradi_user.ui.detailorder

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.amirarahmah.toradi_user.R
import com.amirarahmah.toradi_user.data.model.Status
import com.amirarahmah.toradi_user.util.Const
import com.amirarahmah.toradi_user.util.PreferenceHelper
import com.amirarahmah.toradi_user.util.PreferenceHelper.get
import com.amirarahmah.toradi_user.util.showSnackbarInfo
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_detail_order.*
import java.text.DecimalFormat

class DetailOrderActivity2 : AppCompatActivity(), ReviewFragment.OnReviewDone {

    private var token: String? = ""

    //for broadcastreceiver
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var mIntentFilter: IntentFilter

    private var orderId: Int? = 0

    /* MAPS Variable */
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
    private var rating = 0

    private lateinit var viewModelDetail: DetailOrderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_order2)

        supportActionBar?.title = "Detail Pesanan"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val extras = intent.extras
        orderId = extras!!.getInt("order_id", 0)

        val prefs = PreferenceHelper.defaultPrefs(this)
        token = prefs["token", ""]

        setupBroadcastReceiver()
        setupViewModel()
        getDetailOrder()

    }


    private fun setupViewModel() {
        viewModelDetail = ViewModelProviders.of(this).get(DetailOrderViewModel::class.java)

        viewModelDetail.isLoading.observe(this, Observer {
            if (it == true) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
            }
        })

        viewModelDetail.statusUpdated.observe(this, Observer {
            viewModelDetail.getDetailOrder(token!!, orderId!!)
        })

        viewModelDetail.review.observe(this, Observer {
            when (it?.status) {
                Status.SUCCESS -> {
                    val review = it.data
                    hasReviewed = review != null
                    review?.let{
                        rating = review.rating
                    }

                }
                Status.ERROR -> {

                }
            }
        })

        viewModelDetail.order.observe(this, Observer {
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
            viewModelDetail.getDetailOrder(token!!, orderId!!)
        }
    }


    private fun setupBroadcastReceiver() {
        //to fetch order detail when order status changed
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                viewModelDetail.getDetailOrder(token!!, orderId!!)
            }
        }

        mIntentFilter = IntentFilter(Const.STATUS_ORDER_UPDATED)
    }


    private fun setClick() {
        btn_sms.setOnClickListener {
            val driverPhone = "+62$driver_phone"

            val url = "https://api.whatsapp.com/send?phone=$driverPhone"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        btn_call.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+62$driver_phone"))
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return@setOnClickListener
            }
            startActivity(intent)
        }

        btn_review.setOnClickListener {
            val reviewFragment = ReviewFragment.newInstance(orderId!!)
            reviewFragment.show(supportFragmentManager, reviewFragment.tag)
        }

        btn_cancel.setOnClickListener {
            //konfirmasi apakah user benar-benar akan membatalkan pemesanan
            val ad = AlertDialog.Builder(this)
            ad.create()
            ad.setTitle("Batalkan Order")
            ad.setMessage("Apakah Anda yakin akan membatalkan pemesanan?")
            ad.setPositiveButton("Ya") { dialog, which ->
                viewModelDetail.cancelOrder(token!!, orderId!!)
            }
            ad.setNegativeButton("Tidak") { dialog, which ->
                dialog.dismiss()
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
                container_rate.visibility = View.GONE
            }
            3 -> {
                btn_cancel.isEnabled = false
                btn_review.visibility = View.GONE
                container.visibility = View.VISIBLE
                container_rate.visibility = View.GONE
            }
            4, 5, 6 -> {
                if(driver_id == null || driver_id == 0 || hasReviewed){
                    btn_review.visibility = View.GONE
                    if(hasReviewed){
                        ratingBar.rating = rating.toFloat()
                        container_rate.visibility = View.VISIBLE
                    }
                }else{
                    btn_review.visibility = View.VISIBLE
                }
                btn_cancel.visibility = View.GONE
                container.visibility = View.GONE
            }
        }
    }


    override fun doneReview(rating: Int) {
        btn_review.visibility = View.GONE
        ratingBar.rating = rating.toFloat()
        container_rate.visibility = View.VISIBLE
        this.showSnackbarInfo("Berhasil memberi penilaian")
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
