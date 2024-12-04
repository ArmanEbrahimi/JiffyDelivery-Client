package com.example.jiffydeliveryclient.remote

import com.example.jiffydeliveryclient.model.Order
import org.json.JSONObject

class FCMSendData(var to: String, var data: Map<String, String>,var order: Order)