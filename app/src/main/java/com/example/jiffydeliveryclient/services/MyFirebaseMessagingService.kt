package com.example.jiffydeliveryclient.services

import com.example.jiffydeliveryclient.model.DeclineRequestFromCourier
import com.example.jiffydeliveryclient.utils.Constants
import com.example.jiffydeliveryclient.utils.UserUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.greenrobot.eventbus.EventBus

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (FirebaseAuth.getInstance().currentUser != null) {
            UserUtils.updateToken(this, token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (message != null) {
            val data = message.data
            if (data[Constants.NOTI_TITLE] != null){
                if (data[Constants.NOTI_TITLE].equals(Constants.REQUEST_COURIER_DECLINED)){
                    EventBus.getDefault().postSticky(DeclineRequestFromCourier())

                }else{
                    Constants.showNotification(
                        this, kotlin.random.Random.nextInt(),
                        data[Constants.NOTI_TITLE],
                        data[Constants.NOTI_BODY],
                        null
                    )

                }
            }

        }
    }
}