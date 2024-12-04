package com.example.jiffydeliveryclient.utils

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.jiffydeliveryclient.R
import com.example.jiffydeliveryclient.model.CourierGeoModel
import com.example.jiffydeliveryclient.model.Order
import com.example.jiffydeliveryclient.remote.FCMNotification
import com.example.jiffydeliveryclient.remote.FCMService
import com.example.jiffydeliveryclient.remote.GetAccessToken
import com.example.jiffydeliveryclient.remote.Message
import com.example.jiffydeliveryclient.remote.Notification
import com.example.jiffydeliveryclient.remote.RetrofitFCM
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.HttpException

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
                Snackbar.make(view!!, "Information updated successfully!", Snackbar.LENGTH_LONG)
                    .show()
            }.addOnFailureListener { e ->
                Snackbar.make(view!!, e.message!!, Snackbar.LENGTH_LONG).show()
            }
    }

    fun updateToken(context: Context, token: String) {
        val tokenModel = TokenModel()
        tokenModel.token = token

        FirebaseDatabase.getInstance()
            .getReference(Constants.TOKEN_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser?.uid!!)
            .setValue(tokenModel)
            .addOnFailureListener { e ->
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
            .addOnSuccessListener { }
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
        Log.d("send request to driver", "i am called")
        //Get token
        FirebaseDatabase.getInstance()
            .getReference(Constants.TOKEN_REFERENCE)
            .child(foundCourier!!.key!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Log.d("tokenmodel", snapshot.getValue().toString())
                        //val tokenModel = snapshot.getValue(TokenModel::class.java)


                        val notificationData: MutableMap<String, String> = HashMap()
                        notificationData.put(Constants.NOTI_TITLE, Constants.REQUEST_COURIER_TITLE)
                        notificationData.put(Constants.NOTI_BODY, "This is the notification body")
                        notificationData.put(
                            Constants.CLIENT_KEY,
                            FirebaseAuth.getInstance().currentUser!!.uid
                        )
                        notificationData.put(
                            Constants.PICKUP_LOCATION,
                            StringBuilder()
                                .append(target.latitude)
                                .append(",")
                                .append(target.longitude)
                                .toString()
                        )
                        notificationData.put(Constants.ORDER_WEIGHT, order.weight)
                        notificationData.put(Constants.ORDER_SIZE, order.size)
                        notificationData.put(Constants.DURATION, duration)
                        notificationData.put(Constants.COURIER_KEY, foundCourier.key!!)

//
                        val token =
                            "e8vZBonoRHCVUiYcvsUBUW:APA91bGAURlO9PPcVhlSpsQK2G_9Kcco9OZ4xY3RvRkLX5WpWo1IXDZsu9sujJMJo64A01Nuh1b6iNeHaA84_BsFv-L0m04SgGoWosHgnnQIJrOx5TFeB46XlN0NpS_rySHLpzd-owaV"
//
                        val notification = Notification(Constants.REQUEST_COURIER_TITLE, "body")
                        val data : MutableMap<String,String> = HashMap()
                        data.put(Constants.NOTI_BODY, "This is the notification body")
                        data.put(
                            Constants.CLIENT_KEY,
                            FirebaseAuth.getInstance().currentUser!!.uid
                        )
                        data.put(
                            Constants.PICKUP_LOCATION,
                            StringBuilder()
                                .append(target.latitude)
                                .append(",")
                                .append(target.longitude)
                                .toString()
                        )
                        data.put(Constants.ORDER_WEIGHT, order.weight)
                        data.put(Constants.ORDER_SIZE, order.size)
                        data.put(Constants.DURATION, duration)
                        data.put(Constants.COURIER_KEY, foundCourier.key!!)
                        val message = Message(token, notification,data)
                        val fcmNotification = FCMNotification(message)


                        val accessToken = "Bearer " + GetAccessToken().getAccessToke()
                        Log.d("accesstoken", accessToken)
                        compositeDisposable.add(fcmService.sendNotification(
                            accessToken, fcmNotification
                        )
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ fcmResponse ->
                                if (fcmResponse.success == 0) {
                                    compositeDisposable.clear()
                                    Snackbar.make(
                                        main_layout!!,
                                        context.getString(R.string.send_request_to_courier_failed),
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }
                            },
                                { t: Throwable? ->
                                    compositeDisposable.clear()
                                    Log.d("wrong address", t!!.message.toString())
//                                    Snackbar.make(main_layout!!, t!!.message!!,Snackbar.LENGTH_LONG).show()
                                    if (t is HttpException) {
                                        val errorBody = t.response()?.errorBody()?.string()
                                        println("Error: $errorBody")
                                    } else {
                                        println("Error: ${t!!.message}")
                                    }
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