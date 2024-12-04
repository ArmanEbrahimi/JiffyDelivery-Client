package com.example.jiffydeliveryclient.remote

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface FCMService {
    @Headers(
        "Content-Type:application/json",
    )

    @POST("v1/projects/jiffy-delivery-80151/messages:send")
    fun sendNotification(@Header("Authorization") token:String, @Body body: FCMNotification): Observable<FCMResponse>
}