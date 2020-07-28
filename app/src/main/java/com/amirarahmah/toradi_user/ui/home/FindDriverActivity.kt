package com.amirarahmah.toradi_user.ui.home

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.amirarahmah.toradi_user.R
import com.amirarahmah.toradi_user.data.model.Status
import com.amirarahmah.toradi_user.ui.detailorder.DetailOrderActivity
import com.amirarahmah.toradi_user.util.Const
import com.amirarahmah.toradi_user.util.PreferenceHelper
import com.amirarahmah.toradi_user.util.PreferenceHelper.get
import com.amirarahmah.toradi_user.util.showSnackbarInfo
import kotlinx.android.synthetic.main.activity_find_driver.*

class FindDriverActivity : AppCompatActivity() {

    //for broadcast receiver
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var mIntentFilter: IntentFilter
    private lateinit var broadcastReceiver2: BroadcastReceiver
    private lateinit var mIntentFilter2: IntentFilter

    private lateinit var viewModel: FindDriverViewModel

    private var pickup_lat: Double? = 0.0
    private var pickup_lng: Double? = 0.0
    private var pickup_address: String? = ""

    private var destination_lat: Double? = 0.0
    private var destination_lng: Double? = 0.0
    private var destination_address: String? = ""

    private var price = 0
    private var distance = 0.0
    private var note: String? = ""

    private var token: String? = ""
    private var order_id: Int = 0

    private lateinit var loading: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_driver)

        viewModel = ViewModelProviders.of(this).get(FindDriverViewModel::class.java)

        pickup_lat = intent.getDoubleExtra("pickup_lat", 0.0)
        pickup_lng = intent.getDoubleExtra("pickup_lng", 0.0)
        pickup_address = intent.getStringExtra("pickup_address")
        destination_lat = intent.getDoubleExtra("destination_lat", 0.0)
        destination_lng = intent.getDoubleExtra("destination_lng", 0.0)
        destination_address = intent.getStringExtra("destination_address")
        price = intent.getIntExtra("price", 0)
        distance = intent.getDoubleExtra("distance", 0.0)
        note = intent.getStringExtra("note")

        loading = ProgressDialog(this)

        setupBroadcastReceiver()
        sendOrderOjek()

        ripple_bg.startRippleAnimation()

        viewModel.isLoading.observe(this, Observer {
            if(it == true){
                showLoading()
            }else{
                loading.dismiss()
            }
        })

    }

    private fun sendOrderOjek() {
        val prefs = PreferenceHelper.defaultPrefs(this)
        token = prefs["token", ""]
        viewModel.sendOrderOjek(
            token, pickup_lat, pickup_lng, pickup_address, destination_lat,
            destination_lng, destination_address, price, distance, note
        )

        viewModel.order.observe(this, Observer {
            when (it?.status) {
                Status.SUCCESS -> {
                    if (it.data != null) {
                        if (it.data.data != null)
                            order_id = it.data.data.id

                        if (it.data.message == resources.getString(R.string.driver_not_found)) {
                            Toast.makeText(
                                this,
                                "Maaf Driver tidak ditemukan, Mohon coba kembali",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            navigateToMainActivity()
                        }
                        setClick()
                    }
                }
                Status.ERROR -> {
                    ripple_bg.stopRippleAnimation()
                    this.showSnackbarInfo("" + it.message)
                }
            }
        })

    }


    private fun setClick() {
        btn_cancel.setOnClickListener {
            //konfirmasi apakah user benar-benar akan membatalkan pemesanan
            val ad = AlertDialog.Builder(this@FindDriverActivity)
            ad.create()
            ad.setTitle("Batalkan Order")
            ad.setMessage("Apakah Anda yakin akan membatalkan pemesanan?")
            ad.setPositiveButton("Ya") { dialog, which ->
                cancelOrder()
            }
            ad.setNegativeButton("Tidak") { dialog, which ->
                dialog.dismiss()
            }

            ad.show()
        }
    }

    private fun showLoading(){
        loading.setMessage("Mohon menunggu..")
        loading.setCancelable(false)
        loading.show()
    }

    private fun setupBroadcastReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                navigateToDetailPesanan()
            }
        }

        mIntentFilter = IntentFilter(Const.NOTIFICATION_DRIVER_FOUND)

        //to start MainActivity when all driver reject order
        broadcastReceiver2 = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                navigateToMainActivity()
            }
        }
        mIntentFilter2 = IntentFilter(Const.ORDER_CANCELLED)
    }


    private fun navigateToDetailPesanan() {
        val intent = Intent(this@FindDriverActivity, DetailOrderActivity::class.java)
        intent.putExtra("order_id", order_id)
        intent.putExtra("pickup_lat", pickup_lat)
        intent.putExtra("pickup_lng", pickup_lng)
        intent.putExtra("destination_lat", destination_lat)
        intent.putExtra("destination_lng", destination_lng)
        startActivity(intent)
        finish()
    }


    private fun navigateToMainActivity() {
        val i = Intent(this@FindDriverActivity, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
        finish()
    }


    private fun cancelOrder() {
        viewModel.cancelOrder(token!!, order_id)
        viewModel.orderCanceled.observe(this, Observer {
            when (it?.status) {
                Status.SUCCESS -> {
                    navigateToMainActivity()
                }
                Status.ERROR -> {
                    this.showSnackbarInfo("" + it.message)
                }
            }
        })
    }


    private fun getDetailOrder() {
        val prefs = PreferenceHelper.defaultPrefs(this)
        token = prefs["token", ""]
        if(order_id != 0){
            viewModel.getDetailOrder(token!!, order_id)
        }
        viewModel.detailOrder.observe(this, Observer {
            when (it?.status) {
                Status.SUCCESS -> {
                    if(it.data != null){
                        val order = it.data
                        if(order.status == 6 || order.status == 7){
                            navigateToMainActivity()
                        }else if(order.status == 2){
                            navigateToDetailPesanan()
                        }
                    }
                }
                Status.ERROR -> {
                    this.showSnackbarInfo("" + it.message)
                }
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateToMainActivity()
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, mIntentFilter)
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver2, mIntentFilter2)
        getDetailOrder()
    }

    override fun onPause() {
        try {
            if (broadcastReceiver != null) {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
                LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver2)
            }
        } catch (e: Exception) {

        }
        super.onPause()

    }
}
