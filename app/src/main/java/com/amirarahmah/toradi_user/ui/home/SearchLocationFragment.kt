package com.amirarahmah.toradi_user.ui.home


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amirarahmah.toradi_user.R
import com.amirarahmah.toradi_user.data.model.Location
import com.amirarahmah.toradi_user.ui.home.adapter.LocationAdapter
import kotlinx.android.synthetic.main.fragment_search_location.*
import kotlinx.android.synthetic.main.toolbar.*


class SearchLocationFragment : DialogFragment() {

    private val RESULT_MAPS: Int = 201

    private lateinit var mAdapter: LocationAdapter
    private val listLocation = arrayListOf<Location>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setWindowAnimations(R.style.DialogAnimation)
            dialog.window!!.setLayout(width, height)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            dialog!!.dismiss()
        }

        choose_from_map.setOnClickListener {
            val intent = Intent(context, PlacePickerActivity::class.java)
            startActivityForResult(intent, RESULT_MAPS)
        }

        setupRecyclerView()

        et_destination.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val keyword = et_destination.text.toString()
                doSearchLocation(keyword)
            }
            false
        }

        et_pickup.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val keyword = et_destination.text.toString()
                doSearchLocation(keyword)
            }
            false
        }
    }


    private fun doSearchLocation(keyword: String) {
        listLocation.add(Location("Jl Terusan Cikampek", "Penanggungsn, kec Klojen, Kota Malang"))
        listLocation.add(Location("Jl Terusan Cikampek", "Penanggungsn, kec Klojen, Kota Malang"))
        listLocation.add(Location("Jl Terusan Cikampek", "Penanggungsn, kec Klojen, Kota Malang"))

        rv_location.visibility = View.VISIBLE
        mAdapter.notifyDataSetChanged()
    }


    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rv_location.layoutManager = layoutManager
        rv_location.isNestedScrollingEnabled = false
        mAdapter = LocationAdapter(listLocation, context!!)
        rv_location.adapter = mAdapter
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val address = data!!.getStringExtra("address")
            val latitude = data.getDoubleExtra("latitude", 0.0)
            val longitude = data.getDoubleExtra("longitude", 0.0)
            val i = Intent()
                .putExtra("address", address)
                .putExtra("latitude", latitude)
                .putExtra("longitude", longitude)
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, i)
            dismiss()
        }
    }
}
