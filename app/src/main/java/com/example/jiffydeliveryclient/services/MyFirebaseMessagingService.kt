package com.example.jiffydeliveryclient.services

import android.util.Log
import com.example.jiffydeliveryclient.model.AcceptRequestFromCourier
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

    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Log.d("deleted","I am called")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

            val data = message.data

            if (data[Constants.NOTI_TITLE].equals(Constants.REQUEST_COURIER_DECLINED)) {
                EventBus.getDefault().postSticky(DeclineRequestFromCourier())
                Constants.showNotification(
                    this, kotlin.random.Random.nextInt(),
                    data[Constants.NOTI_TITLE],
                    data[Constants.NOTI_BODY],
                    null
                )

            }

        if (data[Constants.NOTI_TITLE].equals(Constants.REQUEST_ACCEPTED)){
            EventBus.getDefault().postSticky(AcceptRequestFromCourier(data[Constants.NOTI_TITLE],
                data[Constants.NOTI_BODY],
                data[Constants.COURIER_KEY],
                data[Constants.ESTIMATED_TIME],
                data[Constants.AVATAR_IMAGE],
                data[Constants.FIRST_NAME],
                data[Constants.LAST_NAME],
                data[Constants.ESTIMATED_TIME]))

        }

    }
}