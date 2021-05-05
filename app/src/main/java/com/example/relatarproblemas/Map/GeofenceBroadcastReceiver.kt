package com.example.relatarproblemas.Map

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.relatarproblemas.R
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()){
            val errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.errorCode)
            Log.e(context?.getString(R.string.broadcast_receiver_tag), errorMessage)
            return
        }

        when (geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
//                displayNotification(context, context!!.getString(R.string.enter_geofence))
                Toast.makeText(context, R.string.enter_geofence, Toast.LENGTH_SHORT).show()
                Log.d(context?.getString(R.string.broadcast_receiver_tag), context!!.getString(R.string.enter_geofence))
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
//                displayNotification(context, context!!.getString(R.string.exit_geofence))
                Toast.makeText(context, R.string.exit_geofence, Toast.LENGTH_SHORT).show()
                Log.d(context?.getString(R.string.broadcast_receiver_tag), context!!.getString(R.string.exit_geofence))
            }
            else -> {
                Log.d(context?.getString(R.string.broadcast_receiver_tag), context!!.getString(R.string.invalid))
            }
        }

    }

    private fun displayNotification(context: Context?, geofenceTransition: String) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val notification = NotificationCompat.Builder(context, context.getString(R.string.geofence_channel_id))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Geofence")
                .setContentText(geofenceTransition)
        notificationManager.notify(3, notification.build())

    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
            val channel = NotificationChannel(
                    R.string.geofence_channel_id.toString(),
                    R.string.geofence_channel_name.toString(),
                    NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

}