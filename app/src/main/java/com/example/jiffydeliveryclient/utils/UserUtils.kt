package com.example.jiffydeliveryclient.utils

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.jiffydeliveryclient.R
import com.example.jiffydeliveryclient.model.CourierGeoModel
import com.example.jiffydeliveryclient.model.Order
import com.example.jiffydeliveryclient.remote.FCMSendData
import com.example.jiffydeliveryclient.remote.FCMService
import com.example.jiffydeliveryclient.remote.RetrofitFCM
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

object UserUtils {
    fun updateUser(
        view: View?,
        updateData: Map<String, Any>
    ) {
        FirebaseDatabase.getInstance()
            .getReference(Constants.CLIENT_INFO_REF)
            .child(FirebaseAuth.getInstance().currentUser?.uid!!)
            .updateChildren(updateData)
            .addOnSuccessListener {
                Snackbar.make(view!!,"Information updated successfully!", Snackbar.LENGTH_LONG).show()
            }.addOnFailureListener { e ->
                Snackbar.make(view!!,e.message!!, Snackbar.LENGTH_LONG).show()
            }
    }

    fun updateToken(context: Context, token: String) {
        val tokenModel = TokenModel()
        tokenModel.token = token

        FirebaseDatabase.getInstance()
            .getReference(Constants.TOKEN_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser?.uid!!)
            .setValue(tokenModel)
            .addOnFailureListener { e -> Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show() }
            .addOnSuccessListener {  }
    }
    fun sendRequestToDriver(
        context: Context,
        main_layout: View?,
        foundCourier: CourierGeoModel?,
        target: LatLng,
        order: Order,
        duration: String
    ) {
        val compositeDisposable = CompositeDisposable()
        val fcmService = RetrofitFCM.instance!!.create(FCMService::class.java)
        //Get token
        FirebaseDatabase.getInstance()
            .getReference(Constants.TOKEN_REFERENCE)
            .child(foundCourier!!.key!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Log.d("tokenmodel",snapshot.getValue().toString())
                        //val tokenModel = snapshot.getValue(TokenModel::class.java)


                        val notificationData: MutableMap<String, String> = HashMap()
                        notificationData.put(Constants.NOTI_TITLE, Constants.REQUEST_COURIER_TITLE)
                        notificationData.put(Constants.NOTI_BODY, "This is the notification body")
                        notificationData.put(Constants.CLIENT_KEY,FirebaseAuth.getInstance().currentUser!!.uid)
                        notificationData.put(
                            Constants.PICKUP_LOCATION,
                            StringBuilder()
                                .append(target.latitude)
                                .append(",")
                                .append(target.longitude)
                                .toString()
                        )
                        notificationData.put(Constants.ORDER_WEIGHT,order.weight)
                        notificationData.put(Constants.ORDER_SIZE,order.size)
                        notificationData.put(Constants.DURATION,duration)

                        val fcmSendData = FCMSendData(snapshot.getValue().toString(), notificationData,order)

                        compositeDisposable.add(fcmService.sendNotification(fcmSendData)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ fcmResponse ->
                                if (fcmResponse.success == 0) {
                                    compositeDisposable.clear()
                                    Snackbar.make(main_layout!!,context.getString(R.string.send_request_to_courier_failed),
                                        Snackbar.LENGTH_LONG).show()
                                }
                            },
                                { t: Throwable? ->
                                    compositeDisposable.clear()
                                    Snackbar.make(main_layout!!, t!!.message!!,Snackbar.LENGTH_LONG).show()
                                }
                            ))

                    } else {
                        Snackbar.make(
                            main_layout!!,
                            context.getString(R.string.token_not_found),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Snackbar.make(main_layout!!, error.message, Snackbar.LENGTH_LONG).show()
                }

            })
    }
}