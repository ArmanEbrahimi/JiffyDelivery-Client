package com.example.jiffydeliveryclient.interfaces

import com.example.jiffydeliveryclient.model.CourierGeoModel

interface FirebaseCourierInfoListener {
    fun onCourierInfoLoadSuccess(courierGeoModel : CourierGeoModel?)

}