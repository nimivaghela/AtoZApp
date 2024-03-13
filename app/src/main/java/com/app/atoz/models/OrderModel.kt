package com.app.atoz.models

data class OrderModel(
    val orderId: String,
    val orderName: String,
    val orderAmount: String,
    val orderStatus: String,
    val orderDate: String
)