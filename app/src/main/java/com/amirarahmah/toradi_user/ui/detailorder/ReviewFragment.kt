package com.amirarahmah.toradi_user.ui.detailorder


import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.amirarahmah.toradi_user.R
import com.amirarahmah.toradi_user.data.source.remote.ApiService
import com.amirarahmah.toradi_user.util.PreferenceHelper
import com.amirarahmah.toradi_user.util.PreferenceHelper.get
import com.amirarahmah.toradi_user.util.showSnackbarInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_review.*

class ReviewFragment : DialogFragment() {

    private val apiService by lazy {
        ApiService.create()
    }

    private var orderId = 0

    private lateinit var loading: ProgressDialog

    interface OnReviewDone {
        fun doneReview(rating: Int)
    }

    lateinit var mListener: OnReviewDone

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
        arguments?.let {
            orderId = it.getInt("id")
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
        return inflater.inflate(R.layout.fragment_review, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loading = ProgressDialog(context)

        ic_close.setOnClickListener {
            dismiss()
        }

        btn_send.setOnClickListener {
            val rate = ratingBar.rating.toInt()
            var review = et_review.text.toString()
            if (rate == 0) {
                Toast.makeText(context, "Mohon berikan penilaian Anda", Toast.LENGTH_SHORT).show()
            } else {
                if(review.isBlank()){
                    review = " "
                }
                sendReview(rate, review)
            }
        }
    }

    private fun sendReview(rate: Int, review: String) {
        showLoading()
        val prefs = PreferenceHelper.defaultPrefs(context!!)
        val token = prefs["token", ""]
        val disposable = apiService.sendReview("Bearer $token", orderId, rate, review)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                loading.dismiss()
                mListener.doneReview(rate)
                dismiss()
            }, {
                loading.dismiss()
                Toast.makeText(context, "Tejadi kesalahan jaringan", Toast.LENGTH_SHORT).show()
            })
    }


    private fun showLoading() {
        loading.setMessage("Mohon menunggu..")
        loading.setCancelable(false)
        loading.show()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            this.mListener = activity as OnReviewDone
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnCompleteListener")
        }

    }

    companion object {

        fun newInstance(
            id: Int
        ): ReviewFragment {
            val fragment = ReviewFragment()
            val args = Bundle()
            args.putInt("id", id)
            fragment.arguments = args
            return fragment
        }
    }

}
