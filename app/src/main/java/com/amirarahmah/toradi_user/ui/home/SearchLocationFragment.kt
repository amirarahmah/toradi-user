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
import com.amirarahmah.toradi_user.ui.home.adapter.LocationAdapter
import kotlinx.android.synthetic.main.fragment_search_location.*
import kotlinx.android.synthetic.main.toolbar.*
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amirarahmah.toradi_user.data.model.Prediction
import com.amirarahmah.toradi_user.data.model.Status


class SearchLocationFragment : DialogFragment() {

    private val RESULT_MAPS: Int = 201

    private lateinit var viewModel: LocationViewModel

    private var latitude = 0.0
    private var longitude = 0.0

    //address type
    //1. destination address
    //2. pickup address
    private var addressType = 1

    private var destination_address = ""
    private var pickup_address = ""

    private var keyword = ""

    private lateinit var mAdapter: LocationAdapter
    private val listLocation = arrayListOf<Prediction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
        arguments?.let {
            addressType = it.getInt("type")
            latitude = it.getDouble("latitude")
            longitude = it.getDouble("longitude")
            pickup_address = it.getString("pickup_address", "")
            destination_address = it.getString("destination_address", "")
        }
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
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(et_destination.windowToken, 0)
        }

        viewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)

        choose_from_map.setOnClickListener {
            val intent = Intent(context, PlacePickerActivity::class.java)
            intent.putExtra("type", addressType)
            startActivityForResult(intent, RESULT_MAPS)
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(et_destination.windowToken, 0)
        }

        setupRecyclerView()

        et_pickup.setText(pickup_address)
        et_destination.setText(destination_address)

        if(addressType == 1){
            et_destination.requestFocus()
            et_destination.selectAll()
        }else{
            et_pickup.requestFocus()
            et_pickup.selectAll()
        }
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

        et_destination.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val input = et_destination.text.toString()
                if(keyword != input){
                    addressType = 1
                    keyword = input
                    doSearchLocation(input)
                }
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(et_destination.windowToken, 0)
            }
            false
        }

        et_pickup.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val input = et_pickup.text.toString()
                if(keyword != input){
                    addressType = 2
                    keyword = input
                    doSearchLocation(input)
                }
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(et_pickup.windowToken, 0)
            }
            false
        }
    }


    private fun doSearchLocation(keyword: String) {
        viewModel.getPlaceSuggestion(keyword, latitude, longitude)

        viewModel.placeAutocomplete.observe(this, Observer {
            when (it?.status) {
                Status.SUCCESS -> {
                    rv_location.visibility = View.VISIBLE

                    listLocation.clear()

                    if (it.data != null) {
                        listLocation.addAll(it.data)
                    }

                    mAdapter.notifyDataSetChanged()
                }
                Status.ERROR -> {

                }
            }
        })

    }


    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rv_location.layoutManager = layoutManager
        rv_location.isNestedScrollingEnabled = false
        mAdapter = LocationAdapter(listLocation, context!!){address ->
            val i = Intent()
                .putExtra("address", address)
                .putExtra("address_type", addressType) //1 (destination address), 2 (pickup address)
                .putExtra("from", 1) //if address is from place autocomplete
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, i)
            dismiss()
        }
        rv_location.adapter = mAdapter
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val address = data!!.getStringExtra("address")
            val latitude = data.getDoubleExtra("latitude", 0.0)
            val longitude = data.getDoubleExtra("longitude", 0.0)
            val type = data.getIntExtra("type", 1)
            val i = Intent()
                .putExtra("address", address)
                .putExtra("latitude", latitude)
                .putExtra("longitude", longitude)
                .putExtra("address_type", type) //1 (destination address), 2 (pickup address)
                .putExtra("from", 2) //if address is from maps
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, i)
            dismiss()
        }
    }

    companion object {

        fun newInstance(
            type: Int,
            latitude: Double,
            longitude: Double,
            pickup_address: String,
            destination_address: String
        ): SearchLocationFragment {
            val fragment = SearchLocationFragment()
            val args = Bundle()
            args.putInt("type", type)
            args.putDouble("latitude", latitude)
            args.putDouble("longitude", longitude)
            args.putString("pickup_address", pickup_address)
            args.putString("destination_address", destination_address)
            fragment.arguments = args
            return fragment
        }
    }
}
