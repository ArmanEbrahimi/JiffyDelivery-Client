package com.example.jiffydeliveryclient.model

class ClientModel (
    var firstName: String,
    var lastName: String,
    var phoneNumber : String,
    var avatar: String="",
    var rating : Double = 0.0
){
    constructor():this("","","","",0.0)
}