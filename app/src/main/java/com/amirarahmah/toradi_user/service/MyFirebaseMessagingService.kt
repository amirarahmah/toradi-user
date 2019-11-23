package com.amirarahmah.toradi_user.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.amirarahmah.toradi_user.R
import com.amirarahmah.toradi_user.ui.detailorder.DetailOrderActivity
import com.amirarahmah.toradi_user.util.Const
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d(TAG, "Refreshed token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "firebase notification received")

        Log.d(TAG, "Notification Message Body: " + message.data.toString())

        val id = message.data["order_id"]
        var status = message.data["status"]

        if (status == "2") {
            sendBroadcastToFindDriverActivity()
        }  else {
            sendBroadcastToPesananDetail()
        }

        status = when (status?.toInt()) {
            2 -> {
                "Pengemudi sedang menuju Anda"
            }
            3 -> {
                "Anda telah dijemput oleh Pengemudi"
            }
            4 -> {
                "Anda telah sampai"
            }
            5 -> {
                "Pesanan dibatalkan"
            }
            6 -> {
                "Pesanan dibatalkan oleh Pengemudi"
            }
            else -> {
                ""
            }
        }

        sendNotification(id, status)
    }


    private fun sendBroadcastToFindDriverActivity() {
        val intent = Intent()
        intent.action = Const.NOTIFICATION_DRIVER_FOUND
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun sendBroadcastToPesananDetail() {
        val intent = Intent()
        intent.action = Const.STATUS_ORDER_UPDATED
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun sendNotification(id: String?, status: String) {

        val intent = Intent(this, DetailOrderActivity::class.java)
        intent.putExtra("id", id?.toInt())
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Ojek Toradi")
            .setContentText(status)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Ojek Toradi",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}