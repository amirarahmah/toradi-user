package com.amirarahmah.toradi_user.util

import android.app.Activity
import androidx.core.content.ContextCompat
import com.amirarahmah.toradi_user.R
import de.mateware.snacky.Snacky


fun Activity.showSnackbarInfo(message : String){
    Snacky.builder()
        .setActivity(this)
        .setText(message)
        .setDuration(Snacky.LENGTH_SHORT)
        .setBackgroundColor(ContextCompat.getColor(this, R.color.grey_transparent))
        .build()
        .show()
}