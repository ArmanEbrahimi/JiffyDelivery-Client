package com.example.jiffydeliveryclient.remote

import com.example.jiffydeliveryclient.model.Order

class FCMSendData(var to: String, var data: Map<String, String>,var order: Order)