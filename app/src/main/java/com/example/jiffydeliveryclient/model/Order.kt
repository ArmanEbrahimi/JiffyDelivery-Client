package com.example.jiffydeliveryclient.model

class Order(var weight:String, var size : String, var destination:String,var origin:String) {
    var courier:String? = "null"
    constructor(weight:String,size:String,destination:String,origin:String,courier:String):this(weight,size,destination,origin){
       this.courier = courier
    }
}