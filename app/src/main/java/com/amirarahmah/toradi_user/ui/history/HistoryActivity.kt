package com.amirarahmah.toradi_user.ui.history

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amirarahmah.toradi_user.R
import com.amirarahmah.toradi_user.data.model.Order
import com.amirarahmah.toradi_user.data.model.Prediction
import com.amirarahmah.toradi_user.ui.home.adapter.LocationAdapter
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.fragment_search_location.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var mAdapter: HistoryAdapter
    private val lisOrder = arrayListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        supportActionBar?.title = "Riwayat"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        getHistoryOrder()
    }


    private fun getHistoryOrder() {
        lisOrder.add(Order("Jl Veteran 11", "Order selesai", "2019-11-14"))
        lisOrder.add(Order("Jl Veteran 11", "Order selesai", "2019-11-14"))
        lisOrder.add(Order("Jl Veteran 11", "Order selesai", "2019-11-14"))
        lisOrder.add(Order("Jl Veteran 11", "Order selesai", "2019-11-14"))
        lisOrder.add(Order("Jl Veteran 11", "Order selesai", "2019-11-14"))
        lisOrder.add(Order("Jl Veteran 11", "Order selesai", "2019-11-14"))
    }


    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rv_order.layoutManager = layoutManager
        rv_order.isNestedScrollingEnabled = false
        mAdapter = HistoryAdapter(lisOrder, this){}
        rv_order.adapter = mAdapter
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
