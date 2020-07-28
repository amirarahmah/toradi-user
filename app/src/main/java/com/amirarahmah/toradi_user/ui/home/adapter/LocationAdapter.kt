package com.amirarahmah.toradi_user.ui.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amirarahmah.toradi_user.R
import com.amirarahmah.toradi_user.data.model.Prediction
import kotlinx.android.synthetic.main.item_location.view.*

class LocationAdapter(
    val listLocation: ArrayList<Prediction>,
    val context: Context,
    val onClickListener: (String) -> Unit
) : RecyclerView.Adapter<LocationAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
        val view = LayoutInflater.from(p0.context)
            .inflate(R.layout.item_location, p0, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listLocation.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val loc = listLocation[position]
        holder.tvName.text = loc.structured_formatting.main_text
        holder.tvDetail.text = loc.structured_formatting.secondary_text

        holder.container.setOnClickListener {
            onClickListener(loc.description)
        }
    }


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container = itemView.container
        val tvName = itemView.tv_name
        val tvDetail = itemView.tv_detail
    }

}