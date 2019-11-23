package com.amirarahmah.toradi_user.ui.history

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amirarahmah.toradi_user.R
import com.amirarahmah.toradi_user.data.model.TestOrder
import com.amirarahmah.toradi_user.ui.detailorder.DetailOrderActivity
import kotlinx.android.synthetic.main.item_order.view.*

class HistoryAdapter(
    val listOrder: ArrayList<TestOrder>,
    val context: Context,
    val onClickListener: (String) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
        val view = LayoutInflater.from(p0.context)
            .inflate(R.layout.item_order, p0, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listOrder.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val order = listOrder[position]
        holder.tvDestination.text = order.destination
        holder.tvStatus.text = order.status
        holder.tvDate.text = order.date

        holder.container.setOnClickListener {
            val intent = Intent(context, DetailOrderActivity::class.java)
            context.startActivity(intent)
        }
    }


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container = itemView.container
        val tvDestination = itemView.tv_destination
        val tvStatus = itemView.tv_status
        val tvDate = itemView.tv_date
    }

}