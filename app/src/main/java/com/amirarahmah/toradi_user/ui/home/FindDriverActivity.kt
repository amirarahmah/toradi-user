package com.amirarahmah.toradi_user.ui.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amirarahmah.toradi_user.R

class FindDriverActivity : AppCompatActivity() {

    private var userId = 0

    private var pickup_lat: Double? = 0.0
    private var pickup_lng: Double? = 0.0
    private var pickup_address: String? = ""

    private var destination_lat: Double? = 0.0
    private var destination_lng: Double? = 0.0
    private var destination_address: String?  = ""

    private var price = 0
    private var distance = 0.0
    private var note: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_driver)

        pickup_lat = intent.getDoubleExtra("pickup_lat", 0.0)
        pickup_lng = intent.getDoubleExtra("pickup_lng", 0.0)
        pickup_address = intent.getStringExtra("pickup_address")
        destination_lat = intent.getDoubleExtra("destination_lat", 0.0)
        destination_lng = intent.getDoubleExtra("destination_lng", 0.0)
        destination_address = intent.getStringExtra("destination_address")
        price = intent.getIntExtra("price", 0)
        distance = intent.getDoubleExtra("distance", 0.0)
        note = intent.getStringExtra("note")
    }
}
