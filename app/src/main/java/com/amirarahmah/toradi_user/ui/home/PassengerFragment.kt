package com.amirarahmah.toradi_user.ui.home


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

import com.amirarahmah.toradi_user.R
import kotlinx.android.synthetic.main.fragment_passenger.*
import kotlinx.android.synthetic.main.toolbar.*


class PassengerFragment : DialogFragment(){

    private var condition = ""

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
        return inflater.inflate(R.layout.fragment_passenger, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            dialog!!.dismiss()
        }

        btn_send.setOnClickListener {
            getConditionData()
            val i = Intent()
                .putExtra("condition", condition)
                .putExtra("total", getTotalPassenger())
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, i)
            dismiss()
        }
    }


    private fun getTotalPassenger(): String {
        return if(rb_one.isChecked){
            "1"
        }else{
            "2"
        }
    }


    private fun getConditionData() {
        if(cb_autis.isChecked){
            condition += "Autis, "
        }
        if(cb_tunadaksa.isChecked){
            condition += "Tunadaksa, "
        }
        if(cb_tunagrahita.isChecked){
            condition += "Tunagrahita, "
        }
        if(cb_tunanetra.isChecked){
            condition += "Tunanetra, "
        }
        if(cb_tunarungu.isChecked){
            condition += "Tunarungu, "
        }
        if(cb_tunawicara.isChecked){
            condition += "Tunawicara, "
        }

        condition = condition.substring(0, condition.length - 2)

        if(rb_ya.isChecked){
            condition += ", Membawa kursi roda"
        }

        if(condition.isBlank()){
            condition = "-"
        }

    }

}
