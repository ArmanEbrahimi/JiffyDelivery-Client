package com.example.jiffydeliveryclient.model

import com.firebase.geofire.GeoLocation

class CourierGeoModel(
    var key:String? = null,
    var geoLocation: GeoLocation? = null,
    var courierInfoModel: CourierInfoModel? = null
) {
constructor(key: String?,geoLocation: GeoLocation?) : this() {
    this.key = key
    this.geoLocation = geoLocation
}

}
