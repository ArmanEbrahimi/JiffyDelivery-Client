package com.example.jiffydeliveryclient.model

data class CourierInfoModel(
    val firstName:String,
    val lastName: String,
    val phoneNumber: String,
    val rate: Double,
    val avatar: String
){
    constructor():this("","","",0.0,"")
}
