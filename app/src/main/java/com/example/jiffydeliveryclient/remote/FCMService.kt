package com.example.jiffydeliveryclient.remote

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FCMService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAc0E7OiU:APA91bEjKvCq4YGLpRMPp4_fsxp30QrcEfDPDEMiIUfK76YRJjZdxwJrn2qt_HeFvwAPFoE9UxWfMdAb2lKjpqNvmUpfVthvyo1X8ZpiRKpVBp7OeAyQhLlOD0uG0KuU6onslWoPlwOz"
    )

    @POST("fcm/send")
    fun sendNotification(@Body body: FCMSendData?): Observable<FCMResponse>
}