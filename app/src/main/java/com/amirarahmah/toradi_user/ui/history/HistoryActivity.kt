package com.amirarahmah.toradi_user.ui.history

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amirarahmah.toradi_user.R
import com.amirarahmah.toradi_user.data.model.Order
import com.amirarahmah.toradi_user.data.model.Status
import com.amirarahmah.toradi_user.util.PreferenceHelper
import com.amirarahmah.toradi_user.util.PreferenceHelper.get
import com.amirarahmah.toradi_user.util.showSnackbarInfo
import kotlinx.android.synthetic.main.activity_history.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var mAdapter: HistoryAdapter
    private val listOrder = arrayListOf<Order>()

    private lateinit var viewModel: OrderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        supportActionBar?.title = "Riwayat"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProviders.of(this).get(OrderViewModel::class.java)

        viewModel.isLoading.observe(this, Observer {
            if (it == true) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
            }
        })

        setupRecyclerView()
        getOrderHistory()
    }


    private fun getOrderHistory() {
        val prefs = PreferenceHelper.defaultPrefs(this)
        val token = prefs["token", ""]

        viewModel.getOrderHistory(token!!)

        viewModel.listOrder.observe(this, Observer {
            when (it?.status) {
                Status.SUCCESS -> {
                    if (it.data != null) {
                        val responseList = it.data

                        listOrder.clear()
                        listOrder.addAll(responseList)

                        mAdapter.notifyDataSetChanged()
                    }
                }
                Status.ERROR -> {
                    this.showSnackbarInfo(""+it.message)
                }
            }
        })
    }


    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rv_order.layoutManager = layoutManager
        rv_order.isNestedScrollingEnabled = false
        mAdapter = HistoryAdapter(listOrder, this){}
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
