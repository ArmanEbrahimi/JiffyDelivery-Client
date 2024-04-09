package com.example.jiffydeliveryclient.utils

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.jiffydeliveryclient.R
import com.example.jiffydeliveryclient.model.AnimationModel
import com.example.jiffydeliveryclient.model.ClientModel
import com.example.jiffydeliveryclient.model.CourierGeoModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import java.util.Calendar

object Constants{
    fun buildName(firstName: String?, lastName: String?): String? {
        return StringBuilder(firstName).append(" ").append(lastName).toString()
    }

    val LAST_NAME: String = "LastName"
    val FIRST_NAME: String = "FirstName"
    val AVATAR_IMAGE: String = "AvatarImage"
    val ESTIMATED_TIME: String ="EstimatedTime"
    val REQUEST_ACCEPTED: String= "RequestAccepted"
    val COURIER_KEY: String="CourierKey"
    val DURATION: String = "duration"
    val ORDER_SIZE: String = "OrderSize"
    val ORDER_WEIGHT: String = "OrderWeight"
    val ORDER_INFO_REFERENCE :String = "OrderInfoRef"
    val REQUEST_COURIER_DECLINED: String? = "RequestDeclined"
    val COURIERS_LOCATION_REFERENCE = "CouriersLocation"
    val COURIER_INFO_REFERENCE: String = "CourierInfoRef"
    val CLIENT_LOCATION_REFERENCE= "ClientLocation"
    var currentClient: ClientModel? = null
    const val CLIENT_INFO_REF = "Clients"
    val TOKEN_REFERENCE: String = "Token"
    val couriersFound: HashMap<String,CourierGeoModel> = HashMap()
    val couriersSubscribe: MutableMap<String, AnimationModel> = HashMap()
    val NOTI_TITLE: String = "title"
    val NOTI_BODY: String = "body"
    val CLIENT_KEY: String = "ClientKey"
    val REQUEST_COURIER_TITLE: String = "RequestCourier"
    val PICKUP_LOCATION: String = "PickupLocation"
    val markerList: HashMap<String, Marker> = HashMap()







    fun setWelcomeMessage(textWelcome: TextView?) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hour >= 1 && hour <= 12) {
            textWelcome?.text = StringBuilder("Good morning")
        } else if (hour > 12 && hour <= 17) {
            textWelcome?.text = java.lang.StringBuilder("Good afternoon")
        } else {
            textWelcome?.text = StringBuilder("Good evening")
        }
    }
    //DECODE POLY
    fun decodePoly(encoded: String): MutableList<LatLng> {
        val poly: MutableList<LatLng> = ArrayList()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)
        }
        return poly
    }

    fun formatAddress(address: String): String {
        val commaIndex = address.indexOf(',')
        val secondCommaIndex = address.indexOf(',', commaIndex + 1)

        if (commaIndex != -1) {
            return address.substring(commaIndex + 1, secondCommaIndex).trim()
        }

        return address
    }

    fun formatDuration(duration: String): CharSequence {
        if (duration.contains("mins")) {
            return duration.substring(0,duration.length -1)
        }else {
            return duration
        }
    }
    fun buildWelcomeMessage(): String {
        return StringBuilder("Welcome, ")
            .append(currentClient?.firstName)
            .append(" ")
            .append(currentClient?.lastName)
            .toString()
    }
    fun valueAnimate(duration: Long, listener: ValueAnimator.AnimatorUpdateListener?): ValueAnimator {
        val va = ValueAnimator.ofFloat(0f,100f)
        va.duration = duration
        va.addUpdateListener(listener)
        va.repeatCount = ValueAnimator.INFINITE
        va.repeatMode = ValueAnimator.RESTART
        va.start()

        return va
    }
    fun getBearing(begin: LatLng, end: LatLng): Float {
        //You can copy this function by link at description
        val lat = Math.abs(begin.latitude - end.latitude)
        val lng = Math.abs(begin.longitude - end.longitude)
        if (begin.latitude < end.latitude && begin.longitude < end.longitude) return Math.toDegrees(
            Math.atan(lng / lat)
        )
            .toFloat() else if (begin.latitude >= end.latitude && begin.longitude < end.longitude) return (90 - Math.toDegrees(
            Math.atan(lng / lat)
        ) + 90).toFloat() else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude) return (Math.toDegrees(
            Math.atan(lng / lat)
        ) + 180).toFloat() else if (begin.latitude < end.latitude && begin.longitude >= end.longitude) return (90 - Math.toDegrees(
            Math.atan(lng / lat)
        ) + 270).toFloat()
        return (-1).toFloat()
    }
    @SuppressLint("UnspecifiedImmutableFlag")
    fun showNotification(
        context: Context,
        id: Int,
        title: String?,
        body: String?,
        intent: Intent?
    ) {
        var pendingIntent: PendingIntent? = null
        if (intent != null)
            pendingIntent =
                PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val NOTIFICATION_CHANNEL_ID = "com.example.jiffydeliveryclient"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, "Jiffy Delivery Client",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = "Jiffy Delivery Client"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)
        }


        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle(title)

        builder.setAutoCancel(false)
        builder.setContentText(body)
        builder.setPriority(NotificationCompat.PRIORITY_HIGH)
        builder.setDefaults(android.app.Notification.DEFAULT_VIBRATE)
        builder.setSmallIcon(R.drawable.basline_motor_cycle)
        builder.setLargeIcon(
            BitmapFactory.decodeResource(
                context.resources,
                R.drawable.basline_motor_cycle
            )
        )

        if (pendingIntent != null)
            builder.setContentIntent(pendingIntent)
        val notification = builder.build()
        notificationManager.notify(id, notification)
        Log.d("TITLE",title!!)
    }


}