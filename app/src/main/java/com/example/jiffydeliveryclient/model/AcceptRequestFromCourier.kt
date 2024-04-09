package com.example.jiffydeliveryclient.model

class AcceptRequestFromCourier(
    var notificationTitle: String?,
    var notificationBody: String?,
    var courierKey: String?,
    var estimatedTime: String?,
    var avatarImage: String?,
    var firtName: String?,
    var lastName: String?,
    var expectingTime: String?
) {
}