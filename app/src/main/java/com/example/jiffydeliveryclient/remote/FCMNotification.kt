package com.example.jiffydeliveryclient.remote

data class FCMNotification(val message: Message)
data class Message(val token: String, val notification: Notification,val data: Map<String,String>)
data class Notification(
    val title: String,
    val body: String,
)
