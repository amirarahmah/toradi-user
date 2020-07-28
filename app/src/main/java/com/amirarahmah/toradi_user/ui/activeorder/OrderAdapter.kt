package com.amirarahmah.toradi_user.ui.activeorder

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amirarahmah.toradi_user.R
import com.amirarahmah.toradi_user.data.model.Order
import com.amirarahmah.toradi_user.ui.detailorder.DetailOrderActivity
import kotlinx.android.synthetic.main.item_order.view.*

class OrderAdapter(
    val listOrder: ArrayList<Order>,
    val context: Context,
    val onClickListener: (String) -> Unit
) : RecyclerView.Adapter<OrderAdapter.MyViewHolder>() {

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
        holder.tvDestination.text = order.destination_address

        val statusText = when (order.status) {
            1 -> {
                "Sedang Mencari Pengemudi"
            }
            2 ->
               "Pengemudi sedang menuju Anda"
            3 ->
                "Dalam perjalanan"
            4 ->
                "Pesanan selesai"
            5 ->
                "Pesanan dibatalkan"
            6 ->
                "Pesanan dibatalkan oleh Driver"
            else -> {
                ""
            }
        }

        holder.tvStatus.text = statusText

        holder.tvDate.text = order.created_at.split("T")[0]

        holder.container.setOnClickListener {
            val intent = Intent(context, DetailOrderActivity::class.java)
            intent.putExtra("order_id", order.id)
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